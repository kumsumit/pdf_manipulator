import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.EventType
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo
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
        // JPEG: FF D8 FF
        imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte() && imageBytes[2] == 0xFF.toByte() -> ImageFormat.JPEG
        // PNG: 89 50 4E 47
        imageBytes[0] == 0x89.toByte() && imageBytes[1] == 0x50.toByte() && imageBytes[2] == 0x4E.toByte() && imageBytes[3] == 0x47.toByte() -> ImageFormat.PNG
        // GIF: 47 49 46
        imageBytes[0] == 0x47.toByte() && imageBytes[1] == 0x49.toByte() && imageBytes[2] == 0x46.toByte() -> ImageFormat.GIF
        // BMP: 42 4D
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
        ImageFormat.UNKNOWN -> "png" // Default to PNG for unknown formats
    }
}

suspend fun extractImagesFromPdf(pdfBytes: ByteArray, outputDir: String): List<String> {
    val extractedImagePaths = mutableListOf<String>()

    withContext(Dispatchers.IO) {
        val outputDirectory = File(outputDir)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        val pdfDocument = PdfDocument(PdfReader(ByteArrayInputStream(pdfBytes)))
        val numberOfPages = pdfDocument.numberOfPages
        var imageCounter = 0

        for (page in 1..numberOfPages) {
            val strategy = object : ITextExtractionStrategy, IEventListener {

                override fun getResultantText(): String = ""

                override fun eventOccurred(data: IEventData?, type: EventType?) {
                    if (type == EventType.RENDER_IMAGE) {
                        try {
                            val imageRenderInfo = data as ImageRenderInfo
                            val imageObject = imageRenderInfo.image
                            if (imageObject != null) {
                                val imageBytes = imageObject.imageAsBytes
                                if (imageBytes.isNotEmpty()) {
                                    imageCounter++
                                    val format = detectImageFormat(imageBytes)
                                    val extension = getFileExtension(format)
                                    val fileName = "image_${page}_${imageCounter}.$extension"
                                    val imageFile = File(outputDirectory, fileName)
                                    FileOutputStream(imageFile).use { outputStream ->
                                        outputStream.write(imageBytes)
                                    }
                                    extractedImagePaths.add(imageFile.absolutePath)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun getSupportedEvents(): MutableSet<EventType> {
                    return mutableSetOf(EventType.RENDER_IMAGE)
                }
            }

            val processor = PdfCanvasProcessor(strategy)
            processor.processPageContent(pdfDocument.getPage(page))
        }

        pdfDocument.close()
    }

    return extractedImagePaths
}

        val pdfDocument = PdfDocument(PdfReader(ByteArrayInputStream(pdfBytes)))
        val numberOfPages = pdfDocument.numberOfPages
        var imageCounter = 0

        for (page in 1..numberOfPages) {
            val strategy = object : ITextExtractionStrategy, IEventListener {

                override fun getResultantText(): String = ""

                override fun eventOccurred(data: IEventData?, type: EventType?) {
                    if (type == EventType.RENDER_IMAGE) {
                        try {
                            val imageRenderInfo = data as ImageRenderInfo
                            val imageObject = imageRenderInfo.image
                            if (imageObject != null) {
                                val imageBytes = imageObject.imageAsBytes
                                if (imageBytes.isNotEmpty()) {
                                    imageCounter++
                                    val fileName = "image_${page}_${imageCounter}.png"
                                    val imageFile = File(outputDirectory, fileName)
                                    val outputStream = FileOutputStream(imageFile)
                                    outputStream.write(imageBytes)
                                    outputStream.close()
                                    extractedImagePaths.add(imageFile.absolutePath)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun getSupportedEvents(): MutableSet<EventType> {
                    return mutableSetOf(EventType.RENDER_IMAGE)
                }
            }

            val processor = PdfCanvasProcessor(strategy)
            processor.processPageContent(pdfDocument.getPage(page))
        }

        pdfDocument.close()
    }

    return extractedImagePaths
}
                }

                override fun getSupportedEvents(): MutableSet<EventType> {
                    TODO("Not yet implemented")
                }
            }

            val processor = PdfCanvasProcessor(strategy)
            processor.processPageContent(pdfDocument.getPage(page))
        }

        pdfDocument.close()
    }

    return extractedImagesBytes
}