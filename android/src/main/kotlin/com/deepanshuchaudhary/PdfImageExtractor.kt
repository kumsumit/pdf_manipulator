package com.deepanshuchaudhary.pdf_manipulator

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.EventType
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

enum class ImageFormat {
    JPEG, PNG, GIF, BMP, UNKNOWN
}

fun detectImageFormat(imageBytes: ByteArray): ImageFormat {
    if (imageBytes.size < 4) return ImageFormat.UNKNOWN

    return when {
        imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte() && imageBytes[2] == 0xFF.toByte() -> ImageFormat.JPEG
        imageBytes[0] == 0x89.toByte() && imageBytes[1] == 0x50.toByte() && imageBytes[2] == 0x4E.toByte() && imageBytes[3] == 0x47.toByte() -> ImageFormat.PNG
        imageBytes[0] == 0x47.toByte() && imageBytes[1] == 0x49.toByte() && imageBytes[2] == 0x46.toByte() -> ImageFormat.GIF
        imageBytes[0] == 0x42.toByte() && imageBytes[1] == 0x4D.toByte() -> ImageFormat.BMP
        else -> ImageFormat.UNKNOWN
    }
}

fun getFileExtension(format: ImageFormat): String {
    return when (format) {
        ImageFormat.JPEG -> "jpg"
        ImageFormat.PNG -> "png"
        ImageFormat.GIF -> "gif"
        ImageFormat.BMP -> "bmp"
        ImageFormat.UNKNOWN -> "bin"
    }
}

suspend fun extractImagesFromPdf(pdfBytes: ByteArray, outputDir: String): List<String> {
    val extractedImagePaths = mutableListOf<String>()

    withContext(Dispatchers.IO) {
        val outputDirectory = File(outputDir)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        PdfDocument(PdfReader(ByteArrayInputStream(pdfBytes))).use { pdfDocument ->
            var imageCounter = 0

            for (page in 1..pdfDocument.numberOfPages) {
                val strategy = object : ITextExtractionStrategy, IEventListener {
                    override fun getResultantText(): String = ""

                    override fun eventOccurred(data: IEventData?, type: EventType?) {
                        if (type != EventType.RENDER_IMAGE || data !is ImageRenderInfo) return

                        val imageBytes = data.image?.imageBytes ?: return
                        if (imageBytes.isEmpty()) return

                        imageCounter++
                        val extension = getFileExtension(detectImageFormat(imageBytes))
                        val imageFile = File(outputDirectory, "image_${page}_${imageCounter}.$extension")
                        FileOutputStream(imageFile).use { outputStream ->
                            outputStream.write(imageBytes)
                        }
                        extractedImagePaths.add(imageFile.absolutePath)
                    }

                    override fun getSupportedEvents(): MutableSet<EventType> {
                        return mutableSetOf(EventType.RENDER_IMAGE)
                    }
                }

                PdfCanvasProcessor(strategy).processPageContent(pdfDocument.getPage(page))
            }
        }
    }

    return extractedImagePaths
}
