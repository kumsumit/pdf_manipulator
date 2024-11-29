import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.EventType
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

suspend fun extractImagesFromPdf(pdfBytes: ByteArray): List<ByteArray> {
    val extractedImagesBytes = mutableListOf<ByteArray>()

    withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument(PdfReader(ByteArrayInputStream(pdfBytes)))
        val numberOfPages = pdfDocument.numberOfPages

        for (page in 1..numberOfPages) {
            val strategy = object : ITextExtractionStrategy, IEventListener {

                override fun getResultantText(): String = ""
                override fun eventOccurred(data: IEventData?, type: EventType?) {
                    TODO("Not yet implemented")
                    if (type == EventType.RENDER_IMAGE) {
                        val imageRenderInfo = data as ImageRenderInfo
                        extractedImagesBytes.add(imageRenderInfo.image.imageBytes)
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