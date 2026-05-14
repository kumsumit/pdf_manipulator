package com.deepanshuchaudhary.pdf_manipulator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.createBitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfAnnotation
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfFormField
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.RadioCheckField
import com.itextpdf.text.pdf.TextField
import com.itextpdf.text.pdf.parser.PdfReaderContentParser
import com.itextpdf.text.pdf.parser.RenderListener
import com.itextpdf.text.pdf.parser.TextRenderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PdfSecurityFormsOperations {
    private val utils = Utils()

    suspend fun redactRegions(context: Context, pdfPath: String, redactions: List<Map<String, Any>>): String =
        withContext(Dispatchers.IO) {
            val regions = redactions.flatMap { item ->
                val pages = (item["pages"] as? List<Int>) ?: listOf((item["page"] as? Number)?.toInt() ?: 1)
                pages.mapNotNull { page -> rectFromMap(item)?.let { page to it } }
            }
            rasterRedact(context, pdfPath, regions)
        }

    suspend fun redactSearch(context: Context, pdfPath: String, terms: List<String>, caseSensitive: Boolean): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val regions = mutableListOf<Pair<Int, Rectangle>>()
            for (page in 1..reader.numberOfPages) {
                textChunks(reader, page).forEach { chunk ->
                    val haystack = if (caseSensitive) chunk.text else chunk.text.lowercase()
                    terms.forEach { term ->
                        val needle = if (caseSensitive) term else term.lowercase()
                        if (needle.isNotBlank() && haystack.contains(needle)) regions.add(page to chunk.rect)
                    }
                }
            }
            reader.close()
            rasterRedact(context, pdfPath, regions)
        }

    suspend fun redactPatterns(context: Context, pdfPath: String, patternNames: List<String>): String =
        withContext(Dispatchers.IO) {
            val patterns = patternNames.flatMap { patternSet(it) }.ifEmpty {
                patternSet("emails") + patternSet("phones") + patternSet("ssn") + patternSet("accounts")
            }
            val reader = readerFor(context, pdfPath)
            val regions = mutableListOf<Pair<Int, Rectangle>>()
            for (page in 1..reader.numberOfPages) {
                textChunks(reader, page).forEach { chunk ->
                    if (patterns.any { it.containsMatchIn(chunk.text) }) regions.add(page to chunk.rect)
                }
            }
            reader.close()
            rasterRedact(context, pdfPath, regions)
        }

    suspend fun sanitize(context: Context, pdfPath: String, options: Map<String, Any>): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "sanitized")
        val stamper = PdfStamper(reader, FileOutputStream(output))
        if (options["removeMetadata"] != false) {
            stamper.moreInfo = emptyMap()
            reader.catalog.remove(PdfName.METADATA)
        }
        if (options["removeJavaScript"] != false) {
            reader.catalog.remove(PdfName.OPENACTION)
            reader.catalog.remove(PdfName.AA)
            reader.catalog.getAsDict(PdfName.NAMES)?.remove(PdfName.JAVASCRIPT)
        }
        if (options["removeEmbeddedFiles"] != false) {
            reader.catalog.getAsDict(PdfName.NAMES)?.remove(PdfName.EMBEDDEDFILES)
        }
        for (page in 1..reader.numberOfPages) {
            val pageDict = reader.getPageN(page)
            if (options["removeComments"] != false) pageDict.remove(PdfName.ANNOTS)
            if (options["removeThumbnails"] != false) pageDict.remove(PdfName.THUMB)
        }
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun ocrToSearchablePdf(context: Context, pdfPath: String, pages: List<Int>?, options: Map<String, Any>): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "searchable_ocr")
            val stamper = PdfStamper(reader, FileOutputStream(output))
            val ocrTexts = renderAndOcr(context, pdfPath, pages, options)
            val font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
            ocrTexts.forEach { (page, text) ->
                val canvas = stamper.getOverContent(page)
                canvas.beginText()
                canvas.setTextRenderingMode(com.itextpdf.text.pdf.PdfContentByte.TEXT_RENDER_MODE_INVISIBLE)
                canvas.setFontAndSize(font, 8f)
                canvas.showTextAligned(0, text.take(32000), 24f, 24f, 0f)
                canvas.endText()
            }
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun createFormFields(context: Context, pdfPath: String, fields: List<Map<String, Any>>): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "form_fields")
            val stamper = PdfStamper(reader, FileOutputStream(output))
            fields.forEach { field ->
                val page = (field["page"] as? Number)?.toInt() ?: 1
                createField(stamper, field)?.let { stamper.addAnnotation(it, page) }
            }
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun editFormFields(context: Context, pdfPath: String, values: Map<String, Any>, removeFields: List<String>): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "edit_form")
            val stamper = PdfStamper(reader, FileOutputStream(output))
            values.forEach { (name, value) -> stamper.acroFields.setField(name, value.toString()) }
            removeFields.forEach { stamper.acroFields.removeField(it) }
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun xfaInfo(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val xfa = reader.acroFields.xfa
        val result = mapOf("hasXfa" to xfa.isXfaPresent, "isDynamicXfa" to false)
        reader.close()
        result
    }

    suspend fun removeXfa(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "remove_xfa")
        val stamper = PdfStamper(reader, FileOutputStream(output))
        reader.catalog.getAsDict(PdfName.ACROFORM)?.remove(PdfName.XFA)
        stamper.close()
        reader.close()
        output.absolutePath
    }

    private fun rasterRedact(context: Context, pdfPath: String, regions: List<Pair<Int, Rectangle>>): String {
        val pdfUri = utils.getURI(pdfPath)
        val descriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IllegalArgumentException("Cannot open PDF file")
        val renderer = PdfRenderer(descriptor)
        val output = File(File(context.cacheDir, "pdf_manipulator").apply { mkdirs() }, "redacted_${System.currentTimeMillis()}.pdf")
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(output))
        document.open()
        val byPage = regions.groupBy { it.first }
        for (index in 0 until renderer.pageCount) {
            val pageNumber = index + 1
            val page = renderer.openPage(index)
            val bitmap = createBitmap(page.width * 2, page.height * 2)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.scale(2f, 2f)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            canvas.setMatrix(null)
            val paint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            byPage[pageNumber].orEmpty().forEach { (_, rect) ->
                val left = rect.left / page.width * bitmap.width
                val right = rect.right / page.width * bitmap.width
                val top = bitmap.height - (rect.top / page.height * bitmap.height)
                val bottom = bitmap.height - (rect.bottom / page.height * bitmap.height)
                canvas.drawRect(RectF(left, top, right, bottom), paint)
            }
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val image = Image.getInstance(bytes.toByteArray())
            document.setPageSize(Rectangle(image.scaledWidth, image.scaledHeight))
            document.newPage()
            image.setAbsolutePosition(0f, 0f)
            document.add(image)
            page.close()
            bitmap.recycle()
        }
        document.close()
        renderer.close()
        descriptor.close()
        return output.absolutePath
    }

    private fun renderAndOcr(context: Context, pdfPath: String, pages: List<Int>?, options: Map<String, Any>): Map<Int, String> {
        val descriptor = context.contentResolver.openFileDescriptor(utils.getURI(pdfPath), "r")
            ?: throw IllegalArgumentException("Cannot open PDF file")
        val renderer = PdfRenderer(descriptor)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val selected = pages?.takeIf { it.isNotEmpty() } ?: (1..renderer.pageCount).toList()
        val results = mutableMapOf<Int, String>()
        selected.forEach { pageNumber ->
            val page = renderer.openPage(pageNumber - 1)
            val bitmap = createBitmap(page.width, page.height)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val cleaned = cleanupBitmap(bitmap, options)
            results[pageNumber] = Tasks.await(recognizer.process(InputImage.fromBitmap(cleaned, 0))).text
            page.close()
            bitmap.recycle()
            if (cleaned !== bitmap) cleaned.recycle()
        }
        renderer.close()
        descriptor.close()
        return results
    }

    private fun cleanupBitmap(bitmap: Bitmap, options: Map<String, Any>): Bitmap {
        val output = createBitmap(bitmap.width, bitmap.height)
        val matrix = ColorMatrix()
        if (options["grayscale"] == true) matrix.setSaturation(0f)
        val contrast = (options["contrast"] as? Number)?.toFloat() ?: 1f
        val brightness = (options["brightness"] as? Number)?.toFloat() ?: 0f
        matrix.postConcat(ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
        Canvas(output).drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    private fun createField(stamper: PdfStamper, data: Map<String, Any>): PdfFormField? {
        val name = data["name"] as? String ?: return null
        val rect = rectFromMap(data) ?: return null
        return when (data["type"] as? String) {
            "checkbox" -> RadioCheckField(stamper.writer, rect, name, data["value"]?.toString() ?: "Yes").checkField
            "radio" -> RadioCheckField(stamper.writer, rect, name, data["value"]?.toString() ?: "Yes").radioField
            "dropdown" -> TextField(stamper.writer, rect, name).apply {
                choices = ((data["options"] as? List<*>) ?: emptyList<Any>()).map { it.toString() }.toTypedArray()
                choiceExports = choices
            }.comboField
            "signature" -> PdfFormField.createSignature(stamper.writer).apply {
                setFieldName(name)
                setWidget(rect, PdfAnnotation.HIGHLIGHT_INVERT)
            }
            else -> TextField(stamper.writer, rect, name).apply {
                text = data["value"]?.toString() ?: ""
                fontSize = (data["fontSize"] as? Number)?.toFloat() ?: 12f
            }.textField
        }
    }

    private fun textChunks(reader: PdfReader, page: Int): List<TextChunk> {
        val listener = ChunkListener()
        PdfReaderContentParser(reader).processContent(page, listener)
        return listener.chunks
    }

    private class ChunkListener : RenderListener {
        val chunks = mutableListOf<TextChunk>()
        override fun beginTextBlock() = Unit
        override fun endTextBlock() = Unit
        override fun renderImage(renderInfo: com.itextpdf.text.pdf.parser.ImageRenderInfo?) = Unit
        override fun renderText(renderInfo: TextRenderInfo) {
            val baseline = renderInfo.baseline
            val ascent = renderInfo.ascentLine
            val descent = renderInfo.descentLine
            val left = minOf(baseline.startPoint[0], baseline.endPoint[0])
            val right = maxOf(baseline.startPoint[0], baseline.endPoint[0])
            val top = maxOf(ascent.startPoint[1], ascent.endPoint[1])
            val bottom = minOf(descent.startPoint[1], descent.endPoint[1])
            chunks.add(TextChunk(renderInfo.text, Rectangle(left, bottom, right, top)))
        }
    }

    private data class TextChunk(val text: String, val rect: Rectangle)

    private fun rectFromMap(data: Map<String, Any>): Rectangle? {
        val rect = data["rect"] as? List<*>
        return if (rect != null && rect.size >= 4) {
            Rectangle(
                (rect[0] as Number).toFloat(),
                (rect[1] as Number).toFloat(),
                (rect[2] as Number).toFloat(),
                (rect[3] as Number).toFloat()
            )
        } else {
            Rectangle(
                (data["left"] as? Number)?.toFloat() ?: return null,
                (data["bottom"] as? Number)?.toFloat() ?: return null,
                (data["right"] as? Number)?.toFloat() ?: return null,
                (data["top"] as? Number)?.toFloat() ?: return null
            )
        }
    }

    private fun patternSet(name: String): List<Regex> = when (name) {
        "emails", "email" -> listOf(Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE))
        "phones", "phone" -> listOf(Regex("\\+?\\d[\\d\\s().-]{7,}\\d"))
        "ssn", "ssns" -> listOf(Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"))
        "accounts", "accountNumbers" -> listOf(Regex("\\b\\d{9,18}\\b"))
        else -> listOf(runCatching { Regex(name) }.getOrDefault(Regex("$^")))
    }

    private fun readerFor(context: Context, pdfPath: String): PdfReader =
        PdfReader(context.contentResolver.openInputStream(utils.getURI(pdfPath)))
}
