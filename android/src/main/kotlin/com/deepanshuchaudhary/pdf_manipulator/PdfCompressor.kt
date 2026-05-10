package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.system.Os
import android.util.Log
import androidx.core.net.toUri
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.*
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Runtime
import kotlin.math.min


/**
 * Memory management utility for efficient PDF processing
 */
class MemoryManager {
    private val runtime = Runtime.getRuntime()
    private val maxMemory = runtime.maxMemory()

    companion object {
        const val CHUNK_SIZE = 10 // Process 10 images at a time
        const val MEMORY_THRESHOLD = 0.8 // 80% of available memory
        const val MIN_FREE_MEMORY_MB = 50 // Minimum free memory in MB
    }

    /**
     * Check if memory usage is within safe limits
     */
    fun isMemorySafe(): Boolean {
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsageRatio = usedMemory.toDouble() / maxMemory.toDouble()
        val freeMemoryMB = runtime.freeMemory() / (1024 * 1024)

        return memoryUsageRatio < MEMORY_THRESHOLD && freeMemoryMB > MIN_FREE_MEMORY_MB
    }

    /**
     * Force garbage collection if memory is running low
     */
    fun forceGarbageCollectionIfNeeded() {
        if (!isMemorySafe()) {
            Log.w("MemoryManager", "Memory usage high, forcing garbage collection")
            System.gc()
            System.runFinalization()
            Thread.sleep(100) // Give GC time to work
        }
    }

    /**
     * Get optimal chunk size based on available memory
     */
    fun getOptimalChunkSize(totalItems: Int): Int {
        val freeMemoryMB = runtime.freeMemory() / (1024 * 1024)
        val estimatedChunkSize = (freeMemoryMB / 10).toInt() // Rough estimate: 10MB per item

        return min(min(estimatedChunkSize, totalItems), CHUNK_SIZE).coerceAtLeast(1)
    }

    /**
     * Safely recycle bitmap and force cleanup
     */
    fun safeRecycleBitmap(bitmap: Bitmap?) {
        try {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.w("MemoryManager", "Error recycling bitmap: ${e.message}")
        }
    }

    /**
     * Monitor memory during processing
     */
    fun logMemoryUsage(tag: String) {
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val usedMemoryMB = usedMemory / (1024 * 1024)
        val maxMemoryMB = maxMemory / (1024 * 1024)
        val usagePercent = (usedMemory.toDouble() / maxMemory.toDouble() * 100).toInt()

        Log.d("MemoryManager", "$tag - Memory: ${usedMemoryMB}MB/${maxMemoryMB}MB (${usagePercent}%)")
    }
}

fun calculateInSampleSize(originalWidth: Int, originalHeight: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1

    if (originalHeight > reqHeight || originalWidth > reqWidth) {
        val halfHeight = originalHeight / 2
        val halfWidth = originalWidth / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

// For compressing pdf with progress reporting.
suspend fun getCompressedPDFPathWithProgress(
    sourceFilePath: String,
    imageQuality: Int,
    imageScale: Double,
    unEmbedFonts: Boolean,
    advancedOptions: Map<String, Any>?,
    context: Activity,
    operationId: String,
    methodChannel: MethodChannel,
): String? {

    val resultPDFPath: String?

    withContext(Dispatchers.IO) {

        val utils = Utils()

        val begin = System.nanoTime()

        val contentResolver: ContentResolver = context.contentResolver

        val uri = Utils().getURI(sourceFilePath)

        val pdfReaderFile: File = File.createTempFile("readerTempFile", ".pdf")

        // Use streaming copy for memory efficiency
        streamingCopyFile(uri, pdfReaderFile.toUri(), contentResolver)

        val pdfReader = PdfReader(pdfReaderFile).setUnethicalReading(true)
        pdfReader.setMemorySavingMode(true)

        val pdfWriterFile: File = File.createTempFile("writerTempFile", ".pdf")

        val pdfWriter = PdfWriter(pdfWriterFile)

        pdfWriter.setSmartMode(true)
        pdfWriter.compressionLevel = 9

        val pdfDocument = PdfDocument(pdfReader, pdfWriter)

        // Initialize memory manager and log initial memory usage
        val memoryManager = MemoryManager()
        memoryManager.logMemoryUsage("Compression start")

        // Report initial progress
        methodChannel.invokeMethod("onProgress", mapOf(
            "operationId" to operationId,
            "progress" to 0.1,
            "message" to "Initializing compression..."
        ))

        suspend fun reduceImagesSize(scale: Double, quality: Int) {
            val factor = scale.toFloat()
            val allIndirectReferences = pdfDocument.listIndirectReferences()
            val totalImages = allIndirectReferences.size
            var processedImages = 0

            val memoryManager = MemoryManager()
            val chunkSize = memoryManager.getOptimalChunkSize(totalImages)

            Log.d("MemoryManager", "Processing $totalImages images in chunks of $chunkSize")

            // Process images in chunks to manage memory usage
            allIndirectReferences.chunked(chunkSize).forEach { chunk ->
                memoryManager.logMemoryUsage("Before processing chunk")

                for (indRef in chunk) {
                    yield()

                    // Check memory before processing each image
                    memoryManager.forceGarbageCollectionIfNeeded()

                    // Get a direct object and try to resolve indirect chain.
                    // Note: If chain of references has length of more than 32,
                    // this method return 31st reference in chain.
                    val pdfObject: PdfObject? = indRef.refersTo
                    if ((pdfObject == null) || !pdfObject.isStream) {
                        continue
                    }

                    val stream: PdfStream = pdfObject as PdfStream

                    if (PdfName.Image != stream.getAsName(PdfName.Subtype)) {
                        continue
                    }
                    if (PdfName.DCTDecode != stream.getAsName(PdfName.Filter)) {
                        continue
                    }

                    try {
                        val image = PdfImageXObject(stream)
                        val width = (image.width * factor).toInt()
                        val height = (image.height * factor).toInt()
                        if (width <= 0 || height <= 0) {
                            continue
                        }

                        val options: BitmapFactory.Options = BitmapFactory.Options()
                        options.inMutable = true
                        options.inPreferredConfig = Bitmap.Config.RGB_565
                        options.inSampleSize = calculateInSampleSize(image.width.toInt(), image.height.toInt(), width, height)
                        options.outWidth = width
                        options.outHeight = height

                        val bmp = BitmapFactory.decodeByteArray(
                            image.imageBytes, 0, image.imageBytes.size, options
                        )

                        if (bmp == null) {
                            Log.w("MemoryManager", "Failed to decode bitmap for image, skipping")
                            continue
                        }

                        val matrix = Matrix()
                        matrix.postTranslate((-0).toFloat(), (-0).toFloat())

                        if (factor != 1.0f) matrix.postScale(factor, factor)

                        var scaledBitmap: Bitmap? = null
                        try {
                            scaledBitmap = Bitmap.createBitmap(
                                bmp, 0, 0, bmp.width - 1, bmp.height - 1, matrix, true
                            )

                            val scaledBitmapStream = ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, scaledBitmapStream)

                            resetImageStream(
                                stream,
                                scaledBitmapStream.toByteArray(),
                                image.width.toInt(),
                                image.height.toInt()
                            )
                            scaledBitmapStream.close()
                        } finally {
                            // Ensure bitmaps are always recycled
                            memoryManager.safeRecycleBitmap(scaledBitmap)
                            memoryManager.safeRecycleBitmap(bmp)
                        }

                    } catch (e: OutOfMemoryError) {
                        Log.e("MemoryManager", "Out of memory processing image, forcing GC and continuing", e)
                        memoryManager.forceGarbageCollectionIfNeeded()
                        continue
                    } catch (e: Exception) {
                        Log.w("MemoryManager", "Error processing image: ${e.message}")
                        continue
                    }
                }

                processedImages += chunk.size
                val progress = 0.1 + (processedImages.toDouble() / totalImages.toDouble()) * 0.6
                methodChannel.invokeMethod("onProgress", mapOf(
                    "operationId" to operationId,
                    "progress" to progress,
                    "message" to "Processing image $processedImages of $totalImages..."
                ))

                // Force cleanup after each chunk
                memoryManager.forceGarbageCollectionIfNeeded()
                memoryManager.logMemoryUsage("After processing chunk")
            }
        }

        /**
         * Calculate optimal inSampleSize for bitmap decoding to save memory
         */
        fun calculateInSampleSize(originalWidth: Int, originalHeight: Int, reqWidth: Int, reqHeight: Int): Int {
            var inSampleSize = 1

            if (originalHeight > reqHeight || originalWidth > reqWidth) {
                val halfHeight = originalHeight / 2
                val halfWidth = originalWidth / 2

                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }

        try {
            reduceImagesSize(imageScale, imageQuality)

            memoryManager.logMemoryUsage("After image processing")
            memoryManager.forceGarbageCollectionIfNeeded()

            // Report progress after image processing
            methodChannel.invokeMethod("onProgress", mapOf(
                "operationId" to operationId,
                "progress" to 0.7,
                "message" to "Processing fonts..."
            ))

            suspend fun removeFont() {
                val totalObjects = pdfDocument.numberOfPdfObjects
                var processedObjects = 0

                val chunkSize = memoryManager.getOptimalChunkSize(totalObjects)

                Log.d("MemoryManager", "Processing $totalObjects font objects in chunks of $chunkSize")

                // Process objects in chunks
                (0 until totalObjects).chunked(chunkSize).forEach { chunk ->
                    memoryManager.logMemoryUsage("Before processing font chunk")

                    for (i in chunk) {
                        yield()

                        // Check memory before processing each object
                        memoryManager.forceGarbageCollectionIfNeeded()

                        try {
                            val obj: PdfObject? = pdfDocument.getPdfObject(i)

                            // Skip all objects that aren't a dictionary
                            if ((obj == null) || !obj.isDictionary) {
                                continue
                            }

                            // Process all dictionaries
                            unEmbedTTF((obj as PdfDictionary))
                        } catch (e: Exception) {
                            Log.w("MemoryManager", "Error processing font object $i: ${e.message}")
                            continue
                        }
                    }

                    processedObjects += chunk.size
                    val progress = 0.7 + (processedObjects.toDouble() / totalObjects.toDouble()) * 0.2
                    methodChannel.invokeMethod("onProgress", mapOf(
                        "operationId" to operationId,
                        "progress" to progress,
                        "message" to "Processing fonts... $processedObjects/$totalObjects"
                    ))

                    // Force cleanup after each chunk
                    memoryManager.forceGarbageCollectionIfNeeded()
                }
            }

            if (unEmbedFonts) {
                removeFont()
            }

            memoryManager.logMemoryUsage("After font processing")
            memoryManager.forceGarbageCollectionIfNeeded()

        } catch (e: OutOfMemoryError) {
            Log.e("MemoryManager", "Out of memory during compression, attempting recovery", e)
            memoryManager.forceGarbageCollectionIfNeeded()

            // Try to continue with minimal processing
            methodChannel.invokeMethod("onProgress", mapOf(
                "operationId" to operationId,
                "progress" to 0.8,
                "message" to "Memory constrained - applying basic compression..."
            ))
        }

        // Report progress before advanced compression
        methodChannel.invokeMethod("onProgress", mapOf(
            "operationId" to operationId,
            "progress" to 0.9,
            "message" to "Applying advanced compression..."
        ))

        // Apply advanced compression options
        if (advancedOptions != null) {
            applyAdvancedCompression(pdfDocument, pdfReader, advancedOptions)
        }

        // Report finalizing progress
        methodChannel.invokeMethod("onProgress", mapOf(
            "operationId" to operationId,
            "progress" to 0.95,
            "message" to "Finalizing compression..."
        ))

        pdfDocument.close()

        pdfReader.close()
        pdfWriter.close()

        utils.safeDeleteTempFiles(listOfTempFiles = listOf(pdfReaderFile))

        val end = System.nanoTime()
        println("Elapsed time in nanoseconds: ${end - begin}")

        // Report completion
        methodChannel.invokeMethod("onProgress", mapOf(
            "operationId" to operationId,
            "progress" to 1.0,
            "message" to "Compression completed successfully"
        ))

        memoryManager.logMemoryUsage("Compression end")
        resultPDFPath = pdfWriterFile.path
    }

    return resultPDFPath
}

/**
 * Streaming file copy to minimize memory usage for large files
 */
suspend fun streamingCopyFile(
    sourceUri: android.net.Uri,
    destUri: android.net.Uri,
    contentResolver: ContentResolver
) = withContext(Dispatchers.IO) {
    val bufferSize = 8192 // 8KB buffer
    val buffer = ByteArray(bufferSize)

    contentResolver.openInputStream(sourceUri)?.use { input ->
        contentResolver.openOutputStream(destUri)?.use { output ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.flush()
        }
    }
}

// For compressing pdf.
suspend fun getCompressedPDFPath(
    sourceFilePath: String,
    imageQuality: Int,
    imageScale: Double,
    unEmbedFonts: Boolean,
    advancedOptions: Map<String, Any>?,
    context: Activity,
): String? {

    val resultPDFPath: String?

    withContext(Dispatchers.IO) {

        val utils = Utils()

        val begin = System.nanoTime()

        val contentResolver: ContentResolver = context.contentResolver

        val uri = Utils().getURI(sourceFilePath)

        val pdfReaderFile: File = File.createTempFile("readerTempFile", ".pdf")
        utils.copyDataFromSourceToDestDocument(
            sourceFileUri = uri,
            destinationFileUri = pdfReaderFile.toUri(),
            contentResolver = contentResolver
        )

        val pdfReader = PdfReader(pdfReaderFile).setUnethicalReading(true)
        pdfReader.setMemorySavingMode(true)

        val pdfWriterFile: File = File.createTempFile("writerTempFile", ".pdf")

        val pdfWriter = PdfWriter(pdfWriterFile)

        pdfWriter.setSmartMode(true)
        pdfWriter.compressionLevel = 9

        val pdfDocument = PdfDocument(pdfReader, pdfWriter)

        suspend fun reduceImagesSize(scale: Double, quality: Int) {
            val factor = scale.toFloat()
            for (indRef in pdfDocument.listIndirectReferences()) {
                yield()

                // Get a direct object and try to resolve indirect chain.
                // Note: If chain of references has length of more than 32,
                // this method return 31st reference in chain.
                val pdfObject: PdfObject? = indRef.refersTo
                if ((pdfObject == null) || !pdfObject.isStream) {
                    continue
                }

                val stream: PdfStream = pdfObject as PdfStream

                if (PdfName.Image != stream.getAsName(PdfName.Subtype)) {
                    continue
                }
                if (PdfName.DCTDecode != stream.getAsName(PdfName.Filter)) {
                    continue
                }
                val image = PdfImageXObject(stream)
                val width = (image.width * factor).toInt()
                val height = (image.height * factor).toInt()
                if (width <= 0 || height <= 0) {
                    continue
                }

                val options: BitmapFactory.Options = BitmapFactory.Options()
                options.inMutable = true
                options.inPreferredConfig = Bitmap.Config.RGB_565
                options.outWidth = width
                options.outHeight = height

                val bmp = BitmapFactory.decodeByteArray(
                    image.imageBytes, 0, image.imageBytes.size, options
                )

                val matrix = Matrix()
                matrix.postTranslate((-0).toFloat(), (-0).toFloat())

                if (factor != 1.0f) matrix.postScale(factor, factor)

                val scaledBitmap = Bitmap.createBitmap(
                    bmp, 0, 0, bmp.width - 1, bmp.height - 1, matrix, true
                )
                bmp.recycle()
                val scaledBitmapStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, scaledBitmapStream)

                scaledBitmap.recycle()
                resetImageStream(
                    stream,
                    scaledBitmapStream.toByteArray(),
                    image.width.toInt(),
                    image.height.toInt()
                )
                scaledBitmapStream.close()
            }
        }

        reduceImagesSize(imageScale, imageQuality)

        suspend fun removeFont() {
            for (i in 0 until pdfDocument.numberOfPdfObjects) {
                yield()
                val obj: PdfObject? = pdfDocument.getPdfObject(i)

                // Skip all objects that aren't a dictionary
                if ((obj == null) || !obj.isDictionary) {
                    continue
                }

                // Process all dictionaries
                unEmbedTTF((obj as PdfDictionary))
            }
        }

        if (unEmbedFonts) {
            removeFont()
        }

        // Apply advanced compression options
        if (advancedOptions != null) {
            applyAdvancedCompression(pdfDocument, pdfReader, advancedOptions)
        }

        pdfDocument.close()

        pdfReader.close()
        pdfWriter.close()

        utils.deleteTempFiles(listOfTempFiles = listOf(pdfReaderFile))

        val end = System.nanoTime()
        println("Elapsed time in nanoseconds: ${end - begin}")

        resultPDFPath = pdfWriterFile.path
    }

    return resultPDFPath
}

fun resetImageStream(
    stream: PdfStream, imgBytes: ByteArray, imgWidth: Int, imgHeight: Int
) {
//    stream.clear()
    if (stream.bytes.size > imgBytes.size) {
        stream.setData(imgBytes)
    } else {
        stream.setData(stream.bytes)
    }
    stream.put(PdfName.Type, PdfName.XObject)
    stream.put(PdfName.Subtype, PdfName.Image)
    stream.put(PdfName.Filter, PdfName.DCTDecode)
    stream.put(PdfName.Width, PdfNumber(imgWidth))
    stream.put(PdfName.Height, PdfNumber(imgHeight))
    stream.put(PdfName.BitsPerComponent, PdfNumber(8))
    stream.put(PdfName.ColorSpace, PdfName.DeviceRGB)
}

fun unEmbedTTF(dict: PdfDictionary) {

    // Ignore all dictionaries that aren't font dictionaries
    if (PdfName.Font != dict.getAsName(PdfName.Type)) {
        return
    }

    // Only TTF fonts should be removed
    if (dict.getAsDictionary(PdfName.FontFile2) != null) {
        return
    }

    // Check if a subset was used (in which case we remove the prefix)
    var baseFont = dict.getAsName(PdfName.BaseFont)

    if (baseFont.value.toByteArray().size >= 7 && baseFont.value.toByteArray()[6] == '+'.code.toByte()) {
        baseFont = PdfName(baseFont.value.substring(7))
        println(baseFont)
        dict.put(PdfName.BaseFont, baseFont)
    }

    // Check if there's a font descriptor
    val fontDescriptor = dict.getAsDictionary(PdfName.FontDescriptor) ?: return

    // Replace the font name and remove the font file
    fontDescriptor.put(PdfName.FontName, baseFont)

    // Remove the font file reference
    fontDescriptor.remove(PdfName.FontFile2)
}

// Advanced compression options implementation
suspend fun applyAdvancedCompression(
    pdfDocument: PdfDocument,
    pdfReader: PdfReader,
    advancedOptions: Map<String, Any>
) {
    if (advancedOptions["removeUnusedMetadata"] as? Boolean == true) {
        removeDocumentMetadata(pdfDocument)
    }

    if (advancedOptions["cleanNamedDestinations"] as? Boolean == true) {
        cleanNamedDestinations(pdfDocument)
    }

    if (advancedOptions["compressStreams"] as? Boolean == true ||
        advancedOptions["compressFonts"] as? Boolean == true ||
        advancedOptions["optimizeStructure"] as? Boolean == true
    ) {
        recompressStreams(pdfDocument)
    }

    if (advancedOptions["enableFontSubsetting"] as? Boolean == true ||
        advancedOptions["removeDuplicateFonts"] as? Boolean == true ||
        advancedOptions["deduplicateImages"] as? Boolean == true ||
        advancedOptions["optimizeImageFormats"] as? Boolean == true ||
        advancedOptions["flattenFormFields"] as? Boolean == true
    ) {
        Log.i(
            "PdfCompressor",
            "Some advanced compression options require deep PDF resource rewriting and were skipped safely"
        )
    }
}

private fun removeDocumentMetadata(pdfDocument: PdfDocument) {
    val emptyInfo = mapOf(
        "Title" to "",
        "Author" to "",
        "Subject" to "",
        "Keywords" to "",
        "Creator" to "",
        "Producer" to "",
        "CreationDate" to "",
        "ModDate" to ""
    )
    pdfDocument.documentInfo.setMoreInfo(emptyInfo)
    pdfDocument.catalog.remove(PdfName.Metadata)
}

private fun cleanNamedDestinations(pdfDocument: PdfDocument) {
    val catalogDictionary = pdfDocument.catalog.pdfObject
    val names = catalogDictionary.getAsDictionary(PdfName.Names) ?: return
    names.remove(PdfName.Dests)
    if (names.size() == 0) {
        catalogDictionary.remove(PdfName.Names)
    }
}

private suspend fun recompressStreams(pdfDocument: PdfDocument) {
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isStream) continue

        val stream = pdfObject as PdfStream
        stream.setCompressionLevel(9)
        if (stream.getAsName(PdfName.Filter) == null) {
            stream.put(PdfName.Filter, PdfName.FlateDecode)
        }
    }
}
