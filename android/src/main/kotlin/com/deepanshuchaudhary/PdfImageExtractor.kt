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