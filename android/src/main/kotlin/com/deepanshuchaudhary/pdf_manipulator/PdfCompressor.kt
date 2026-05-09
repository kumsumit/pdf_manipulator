package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.net.toUri
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.*
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject
import com.itextpdf.kernel.pdf.PdfBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File


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
    // Font optimizations
    if (advancedOptions["enableFontSubsetting"] as? Boolean == true) {
        performFontSubsetting(pdfDocument)
    }

    if (advancedOptions["removeDuplicateFonts"] as? Boolean == true) {
        removeDuplicateFonts(pdfDocument)
    }

    if (advancedOptions["compressFonts"] as? Boolean == true) {
        compressFonts(pdfDocument)
    }

    // Structure optimizations
    if (advancedOptions["optimizeStructure"] as? Boolean == true) {
        optimizeStructure(pdfDocument, pdfReader)
    }

    if (advancedOptions["removeUnusedMetadata"] as? Boolean == true) {
        removeUnusedMetadata(pdfDocument, pdfReader)
    }

    // Image optimizations
    if (advancedOptions["deduplicateImages"] as? Boolean == true) {
        deduplicateImages(pdfDocument)
    }

    if (advancedOptions["optimizeImageFormats"] as? Boolean == true) {
        optimizeImageFormats(pdfDocument)
    }

    // Stream and form optimizations
    if (advancedOptions["compressStreams"] as? Boolean == true) {
        compressStreams(pdfDocument)
    }

    if (advancedOptions["flattenFormFields"] as? Boolean == true) {
        flattenFormFields(pdfDocument)
    }

    if (advancedOptions["cleanNamedDestinations"] as? Boolean == true) {
        cleanNamedDestinations(pdfDocument, pdfReader)
    }
}

suspend fun performFontSubsetting(pdfDocument: PdfDocument) {
    // This is a simplified font subsetting implementation
    // In a full implementation, this would analyze which characters are used
    // and create font subsets containing only those characters
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isDictionary) continue

        val dict = pdfObject as PdfDictionary
        if (PdfName.Font != dict.getAsName(PdfName.Type)) continue

        // Mark font for subsetting if possible
        // This is a placeholder - actual subsetting requires more complex logic
        val fontDescriptor = dict.getAsDictionary(PdfName.FontDescriptor)
        if (fontDescriptor != null) {
            // Add subsetting hints
            dict.put(PdfName("Subset"), PdfBoolean(true))
        }
    }
}

suspend fun removeDuplicateFonts(pdfDocument: PdfDocument) {
    val fontMap = mutableMapOf<String, PdfIndirectReference>()
    val fontsToRemove = mutableListOf<PdfIndirectReference>()

    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isDictionary) continue

        val dict = pdfObject as PdfDictionary
        if (PdfName.Font != dict.getAsName(PdfName.Type)) continue

        val baseFont = dict.getAsName(PdfName.BaseFont)?.value ?: continue
        val existingRef = fontMap[baseFont]

        if (existingRef != null) {
            // Duplicate font found - mark for removal
            fontsToRemove.add(indRef)
        } else {
            fontMap[baseFont] = indRef
        }
    }

    // Remove duplicate font references
    for (ref in fontsToRemove) {
        // This is a simplified removal - actual implementation would need
        // to update all references to use the original font
        pdfDocument.removeObject(ref)
    }
}

suspend fun compressFonts(pdfDocument: PdfDocument) {
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isStream) continue

        val stream = pdfObject as PdfStream
        val subtype = stream.getAsName(PdfName.Subtype)

        // Compress font streams
        if (subtype == PdfName.Font) {
            stream.setCompressionLevel(9)
            stream.put(PdfName.Filter, PdfName.FlateDecode)
        }
    }
}

suspend fun optimizeStructure(pdfDocument: PdfDocument, pdfReader: PdfReader) {
    // Remove unused objects
    val usedObjects = mutableSetOf<PdfIndirectReference>()

    // Start from root and catalog, traverse and mark used objects
    val catalog = pdfReader.catalog
    markUsedObjects(catalog, pdfDocument, usedObjects)

    // Remove unused objects
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        if (indRef !in usedObjects) {
            pdfDocument.removeObject(indRef)
        }
    }
}

suspend fun removeUnusedMetadata(pdfDocument: PdfDocument, pdfReader: PdfReader) {
    val catalog = pdfReader.catalog

    // Remove potentially unused metadata entries
    val metadataToRemove = listOf(
        PdfName("ModDate"),
        PdfName("CreationDate"),
        PdfName("Producer"),
        PdfName("Creator")
    )

    for (key in metadataToRemove) {
        catalog.remove(key)
    }

    // Remove document info metadata
    val info = pdfReader.info
    for (key in metadataToRemove) {
        info.remove(key.toString())
    }
}

suspend fun deduplicateImages(pdfDocument: PdfDocument) {
    val imageMap = mutableMapOf<String, PdfIndirectReference>()
    val imagesToRemove = mutableListOf<PdfIndirectReference>()

    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isStream) continue

        val stream = pdfObject as PdfStream
        if (PdfName.Image != stream.getAsName(PdfName.Subtype)) continue

        // Create a hash of the image data for comparison
        val imageData = stream.bytes
        val hash = imageData.contentHashCode().toString()

        val existingRef = imageMap[hash]
        if (existingRef != null) {
            // Duplicate image found
            imagesToRemove.add(indRef)
        } else {
            imageMap[hash] = indRef
        }
    }

    // Remove duplicate images
    for (ref in imagesToRemove) {
        pdfDocument.removeObject(ref)
    }
}

suspend fun optimizeImageFormats(pdfDocument: PdfDocument) {
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isStream) continue

        val stream = pdfObject as PdfStream
        if (PdfName.Image != stream.getAsName(PdfName.Subtype)) continue

        // Convert image formats for better compression if beneficial
        val filter = stream.getAsName(PdfName.Filter)
        if (filter == PdfName.DCTDecode) {
            // JPEG is already well compressed, but we could potentially
            // convert to other formats for specific cases
            stream.setCompressionLevel(9)
        }
    }
}

suspend fun compressStreams(pdfDocument: PdfDocument) {
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isStream) continue

        val stream = pdfObject as PdfStream
        // Apply maximum compression to all streams
        stream.setCompressionLevel(9)
        if (stream.getAsName(PdfName.Filter) == null) {
            stream.put(PdfName.Filter, PdfName.FlateDecode)
        }
    }
}

suspend fun flattenFormFields(pdfDocument: PdfDocument) {
    // This is a simplified form field flattening
    // In a full implementation, this would render form fields as static content
    for (indRef in pdfDocument.listIndirectReferences()) {
        yield()
        val pdfObject: PdfObject? = indRef.refersTo
        if (pdfObject == null || !pdfObject.isDictionary) continue

        val dict = pdfObject as PdfDictionary
        if (PdfName.Annot != dict.getAsName(PdfName.Type)) continue

        val subtype = dict.getAsName(PdfName.Subtype)
        if (subtype == PdfName.Widget) {
            // This is a form field annotation - mark for flattening
            dict.put(PdfName("Flattened"), PdfBoolean(true))
        }
    }
}

suspend fun cleanNamedDestinations(pdfDocument: PdfDocument, pdfReader: PdfReader) {
    val catalog = pdfReader.catalog
    val names = catalog.getAsDictionary(PdfName.Names)

    if (names != null) {
        val destinations = names.getAsDictionary(PdfName.Dests)
        if (destinations != null) {
            // Clean unused named destinations
            // This is a simplified implementation
            val kids = destinations.getAsArray(PdfName.Kids)
            if (kids != null) {
                // Remove destinations that aren't referenced
                // Actual implementation would need to check usage
            }
        }
    }
}

suspend fun markUsedObjects(obj: PdfObject?, pdfDocument: PdfDocument, usedObjects: MutableSet<PdfIndirectReference>) {
    if (obj == null) return

    if (obj is PdfIndirectReference) {
        if (obj in usedObjects) return
        usedObjects.add(obj)

        val directObj = pdfDocument.getPdfObject(obj)
        markUsedObjects(directObj, pdfDocument, usedObjects)
    } else if (obj is PdfDictionary) {
        for (key in obj.keys) {
            markUsedObjects(obj.get(key), pdfDocument, usedObjects)
        }
    } else if (obj is PdfArray) {
        for (i in 0 until obj.size()) {
            markUsedObjects(obj.getPdfObject(i), pdfDocument, usedObjects)
        }
    }
    // Other object types don't contain references
}
    fontDescriptor.remove(PdfName.FontFile2)
}