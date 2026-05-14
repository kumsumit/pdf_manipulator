package com.deepanshuchaudhary.pdf_manipulator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.parser.PdfImageObject
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object PdfConversionOperations {
    private val utils = Utils()

    suspend fun pdfToWord(context: Context, pdfPath: String, pages: List<Int>?): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val file = outputFile(context, "pdf_to_word", "docx")
        val paragraphs = mutableListOf<String>()
        selectedPages(pages, reader.numberOfPages).forEach { page ->
            paragraphs.add("Page $page")
            paragraphs.addAll(PdfTextExtractor.getTextFromPage(reader, page).lineSequence())
        }
        if (!writeDocxWithPoi(file, paragraphs)) {
            writeDocx(file, paragraphs)
        }
        reader.close()
        file.absolutePath
    }

    suspend fun pdfToExcel(context: Context, pdfPath: String, pages: List<Int>?): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val file = outputFile(context, "pdf_to_excel", "xlsx")
        val rows = mutableListOf(listOf("Page", "Text"))
        selectedPages(pages, reader.numberOfPages).forEach { page ->
            PdfTextExtractor.getTextFromPage(reader, page).lineSequence().forEach { line ->
                rows.add(listOf(page.toString(), line))
            }
        }
        if (!writeXlsxWithPoi(file, rows)) {
            writeXlsx(file, rows)
        }
        reader.close()
        file.absolutePath
    }

    suspend fun pdfToPowerPoint(context: Context, pdfPath: String, pages: List<Int>?): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val file = outputFile(context, "pdf_to_powerpoint", "pptx")
        val slides = selectedPages(pages, reader.numberOfPages).map { page ->
            "Page $page" to PdfTextExtractor.getTextFromPage(reader, page).take(3500)
        }
        if (!writePptxWithPoi(file, slides)) {
            writePptx(file, slides)
        }
        reader.close()
        file.absolutePath
    }

    suspend fun pdfToHtml(context: Context, pdfPath: String, pages: List<Int>?): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val file = outputFile(context, "pdf_to_html", "html")
        val html = buildString {
            append("<!doctype html><html><head><meta charset=\"utf-8\"><title>PDF Export</title></head><body>")
            selectedPages(pages, reader.numberOfPages).forEach { page ->
                append("<section data-page=\"").append(page).append("\"><h2>Page ").append(page).append("</h2><pre>")
                append(escapeHtml(PdfTextExtractor.getTextFromPage(reader, page)))
                append("</pre></section>")
            }
            append("</body></html>")
        }
        file.writeText(html)
        reader.close()
        file.absolutePath
    }

    suspend fun pdfToTextFile(context: Context, pdfPath: String, pages: List<Int>?): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val file = outputFile(context, "pdf_to_text", "txt")
        file.writeText(selectedPages(pages, reader.numberOfPages).joinToString("\n\n") { page ->
            "Page $page\n${PdfTextExtractor.getTextFromPage(reader, page)}"
        })
        reader.close()
        file.absolutePath
    }

    suspend fun documentToPdf(context: Context, documentPath: String): String = withContext(Dispatchers.IO) {
        textToPdf(context, extractDocumentText(context, documentPath), "document_to_pdf")
    }

    suspend fun textToPdf(context: Context, text: String): String = withContext(Dispatchers.IO) {
        textToPdf(context, text, "text_to_pdf")
    }

    suspend fun scannerImagesToPdf(context: Context, imagePaths: List<String>, options: Map<String, Any>): String = withContext(Dispatchers.IO) {
        val cleaned = imagePaths.mapIndexed { index, path -> cleanupImage(context, path, options, index) }
        try {
            val file = outputFile(context, "scanner_images", "pdf")
            val first = Image.getInstance(cleaned.first().absolutePath)
            val document = Document(com.itextpdf.text.Rectangle(first.scaledWidth, first.scaledHeight))
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            cleaned.forEach { imageFile ->
                val image = Image.getInstance(imageFile.absolutePath)
                document.setPageSize(com.itextpdf.text.Rectangle(image.scaledWidth, image.scaledHeight))
                document.newPage()
                image.setAbsolutePosition(0f, 0f)
                document.add(image)
            }
            document.close()
            file.absolutePath
        } finally {
            cleaned.forEach { it.delete() }
        }
    }

    suspend fun pdfAValidation(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val catalog = reader.catalog
        val hasMetadata = catalog.get(PdfName.METADATA) != null
        val hasOutputIntents = catalog.getAsArray(PdfName.OUTPUTINTENTS) != null
        val version = reader.pdfVersion.toString()
        reader.close()
        mapOf(
            "isLikelyPdfA" to (hasMetadata && hasOutputIntents),
            "hasMetadata" to hasMetadata,
            "hasOutputIntents" to hasOutputIntents,
            "pdfVersion" to version,
            "notes" to "Structural preflight only. Full PDF/A validation requires ICC/output-intent and conformance rule checks."
        )
    }

    suspend fun pdfAConversion(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "pdfa_archive_copy")
        val stamper = PdfStamper(reader, FileOutputStream(output))
        stamper.moreInfo = mapOf(
            "Title" to "PDF/A archival copy",
            "Producer" to "pdf_manipulator",
            "ModDate" to Instant.now().toString()
        )
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun exportEmbeddedImages(context: Context, pdfPath: String, outputDir: String, pages: List<Int>?, format: String): Map<String, Any> =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val dir = File(outputDir).apply { mkdirs() }
            val exported = mutableListOf<Map<String, Any>>()
            var imageIndex = 0
            selectedPages(pages, reader.numberOfPages).forEach { page ->
                val resources = reader.getPageN(page).getAsDict(PdfName.RESOURCES)
                val xObjects = resources?.getAsDict(PdfName.XOBJECT) ?: return@forEach
                xObjects.keys.forEach { key ->
                    val stream = PdfReader.getPdfObject(xObjects.get(key)) as? PRStream ?: return@forEach
                    if (stream.getAsName(PdfName.SUBTYPE) != PdfName.IMAGE) return@forEach
                    val image = PdfImageObject(stream)
                    val extension = normalizedImageExtension(format, image.fileType)
                    val file = File(dir, "page_${page}_image_${++imageIndex}.$extension")
                    file.writeBytes(transcodedImageBytes(image.imageAsBytes, format))
                    exported.add(
                        mapOf(
                            "path" to file.absolutePath,
                            "page" to page,
                            "width" to (stream.getAsNumber(PdfName.WIDTH)?.intValue() ?: 0),
                            "height" to (stream.getAsNumber(PdfName.HEIGHT)?.intValue() ?: 0),
                            "colorSpace" to (stream.get(PdfName.COLORSPACE)?.toString() ?: ""),
                            "bitsPerComponent" to (stream.getAsNumber(PdfName.BITSPERCOMPONENT)?.intValue() ?: 0),
                            "filter" to (stream.get(PdfName.FILTER)?.toString() ?: ""),
                            "format" to extension
                        )
                    )
                }
            }
            reader.close()
            mapOf("images" to exported)
        }

    private fun writeDocx(file: File, paragraphs: List<String>) {
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.text("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>""")
            zip.text("_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>""")
            zip.text("word/document.xml", """<?xml version="1.0" encoding="UTF-8"?><w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>${paragraphs.joinToString("") { "<w:p><w:r><w:t>${escapeXml(it)}</w:t></w:r></w:p>" }}<w:sectPr/></w:body></w:document>""")
        }
    }

    private fun writeXlsx(file: File, rows: List<List<String>>) {
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.text("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
            zip.text("_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            zip.text("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
            zip.text("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="PDF Text" sheetId="1" r:id="rId1"/></sheets></workbook>""")
            val sheetData = rows.mapIndexed { rowIndex, row ->
                "<row r=\"${rowIndex + 1}\">${row.mapIndexed { columnIndex, value -> "<c r=\"${columnName(columnIndex)}${rowIndex + 1}\" t=\"inlineStr\"><is><t>${escapeXml(value)}</t></is></c>" }.joinToString("")}</row>"
            }.joinToString("")
            zip.text("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$sheetData</sheetData></worksheet>""")
        }
    }

    private fun writePptx(file: File, slides: List<Pair<String, String>>) {
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.text("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>${slides.indices.joinToString("") { "<Override PartName=\"/ppt/slides/slide${it + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>" }}</Types>""")
            zip.text("_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/></Relationships>""")
            zip.text("ppt/presentation.xml", """<?xml version="1.0" encoding="UTF-8"?><p:presentation xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><p:sldIdLst>${slides.indices.joinToString("") { "<p:sldId id=\"${256 + it}\" r:id=\"rId${it + 1}\"/>" }}</p:sldIdLst><p:sldSz cx="9144000" cy="6858000"/></p:presentation>""")
            zip.text("ppt/_rels/presentation.xml.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">${slides.indices.joinToString("") { "<Relationship Id=\"rId${it + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide${it + 1}.xml\"/>" }}</Relationships>""")
            slides.forEachIndexed { index, slide ->
                zip.text("ppt/slides/slide${index + 1}.xml", slideXml(slide.first, slide.second))
            }
        }
    }

    private fun writeDocxWithPoi(file: File, paragraphs: List<String>): Boolean = runCatching {
        if (!canUsePoi("org.apache.poi.xwpf.usermodel.XWPFDocument")) return false
        val document = Class.forName("org.apache.poi.xwpf.usermodel.XWPFDocument").getDeclaredConstructor().newInstance()
        paragraphs.forEach { text ->
            val paragraph = document.call("createParagraph") ?: return@runCatching false
            val run = paragraph.call("createRun") ?: return@runCatching false
            run.call("setText", arrayOf(String::class.java), text)
        }
        FileOutputStream(file).use { output ->
            document.call("write", arrayOf(java.io.OutputStream::class.java), output)
        }
        document.call("close")
        true
    }.getOrDefault(false)

    private fun writeXlsxWithPoi(file: File, rows: List<List<String>>): Boolean = runCatching {
        if (!canUsePoi("org.apache.poi.xssf.usermodel.XSSFWorkbook")) return false
        val workbook = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook").getDeclaredConstructor().newInstance()
        val sheet = workbook.call("createSheet", arrayOf(String::class.java), "PDF Text") ?: return@runCatching false
        rows.forEachIndexed { rowIndex, values ->
            val row = sheet.call("createRow", arrayOf(Int::class.javaPrimitiveType!!), rowIndex) ?: return@runCatching false
            values.forEachIndexed { cellIndex, value ->
                val cell = row.call("createCell", arrayOf(Int::class.javaPrimitiveType!!), cellIndex) ?: return@runCatching false
                cell.call("setCellValue", arrayOf(String::class.java), value)
            }
        }
        FileOutputStream(file).use { output ->
            workbook.call("write", arrayOf(java.io.OutputStream::class.java), output)
        }
        workbook.call("close")
        true
    }.getOrDefault(false)

    private fun writePptxWithPoi(file: File, slides: List<Pair<String, String>>): Boolean = runCatching {
        if (!canUsePoi("org.apache.poi.xslf.usermodel.XMLSlideShow")) return false
        val slideshow = Class.forName("org.apache.poi.xslf.usermodel.XMLSlideShow").getDeclaredConstructor().newInstance()
        slides.forEach { slideData ->
            val slide = slideshow.call("createSlide") ?: return@runCatching false
            val textBox = slide.call("createTextBox") ?: return@runCatching false
            textBox.call("setText", arrayOf(String::class.java), "${slideData.first}\n\n${slideData.second}")
        }
        FileOutputStream(file).use { output ->
            slideshow.call("write", arrayOf(java.io.OutputStream::class.java), output)
        }
        slideshow.call("close")
        true
    }.getOrDefault(false)

    private fun extractDocxTextWithPoi(file: File): String? = runCatching {
        if (!canUsePoi("org.apache.poi.xwpf.usermodel.XWPFDocument")) return null
        val document = Class.forName("org.apache.poi.xwpf.usermodel.XWPFDocument")
            .getDeclaredConstructor(java.io.InputStream::class.java)
            .newInstance(file.inputStream())
        val paragraphs = document.call("getParagraphs") as Iterable<*>
        val text = paragraphs.joinToString("\n") { it?.call("getText") as? String ?: "" }
        document.call("close")
        text
    }.getOrNull()

    private fun extractXlsxTextWithPoi(file: File): String? = runCatching {
        if (!canUsePoi("org.apache.poi.xssf.usermodel.XSSFWorkbook")) return null
        val workbook = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook")
            .getDeclaredConstructor(java.io.InputStream::class.java)
            .newInstance(file.inputStream())
        val sheetCount = workbook.call("getNumberOfSheets") as Int
        val text = buildString {
            for (sheetIndex in 0 until sheetCount) {
                val sheet = workbook.call("getSheetAt", arrayOf(Int::class.javaPrimitiveType!!), sheetIndex) as Iterable<*>
                sheet.forEach { row ->
                    if (row is Iterable<*>) {
                        appendLine(row.joinToString("\t") { cell -> cell?.call("toString") as? String ?: "" })
                    }
                }
            }
        }
        workbook.call("close")
        text
    }.getOrNull()

    private fun extractPptxTextWithPoi(file: File): String? = runCatching {
        if (!canUsePoi("org.apache.poi.xslf.usermodel.XMLSlideShow")) return null
        val slideshow = Class.forName("org.apache.poi.xslf.usermodel.XMLSlideShow")
            .getDeclaredConstructor(java.io.InputStream::class.java)
            .newInstance(file.inputStream())
        val slides = slideshow.call("getSlides") as Iterable<*>
        val text = slides.joinToString("\n\n") { slide ->
            val shapes = slide?.call("getShapes") as? Iterable<*> ?: emptyList<Any>()
            shapes.joinToString("\n") { shape -> runCatching { shape?.call("getText") as? String ?: "" }.getOrDefault("") }
        }
        slideshow.call("close")
        text
    }.getOrNull()

    private fun canUsePoi(vararg classNames: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return classNames.all { runCatching { Class.forName(it) }.isSuccess }
    }

    private fun Any.call(name: String, parameterTypes: Array<Class<*>> = emptyArray(), vararg args: Any?): Any? {
        val method = javaClass.methods.firstOrNull { method ->
            method.name == name && method.parameterTypes.toList() == parameterTypes.toList()
        } ?: javaClass.getMethod(name, *parameterTypes)
        return method.invoke(this, *args)
    }

    private fun extractDocumentText(context: Context, documentPath: String): String {
        val local = materialize(context, documentPath)
        return when (local.extension.lowercase()) {
            "docx" -> extractDocxTextWithPoi(local) ?: extractZipXmlText(local, Regex("<w:t[^>]*>(.*?)</w:t>"))
            "xlsx" -> extractXlsxTextWithPoi(local) ?: extractZipXmlText(local, Regex("<t[^>]*>(.*?)</t>"))
            "pptx" -> extractPptxTextWithPoi(local) ?: extractZipXmlText(local, Regex("<a:t[^>]*>(.*?)</a:t>"))
            "html", "htm" -> local.readText().replace(Regex("<[^>]+>"), " ")
            else -> local.readText()
        }.also { if (local.parentFile == context.cacheDir) local.delete() }
    }

    private fun extractZipXmlText(file: File, regex: Regex): String {
        ZipFile(file).use { zip ->
            return zip.entries().asSequence()
                .filter { it.name.endsWith(".xml") }
                .joinToString("\n") { entry ->
                    val xml = zip.getInputStream(entry).bufferedReader().readText()
                    regex.findAll(xml).joinToString("\n") { unescapeXml(it.groupValues[1]) }
                }
        }
    }

    private fun textToPdf(context: Context, text: String, suffix: String): String {
        val file = outputFile(context, suffix, "pdf")
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()
        text.chunked(3500).forEach { document.add(Paragraph(it)) }
        document.close()
        return file.absolutePath
    }

    private fun cleanupImage(context: Context, imagePath: String, options: Map<String, Any>, index: Int): File {
        val source = materialize(context, imagePath)
        val bitmap = decodeBitmap(source) ?: throw IllegalArgumentException("Unsupported image format: ${source.extension}")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val matrix = ColorMatrix()
        if (options["grayscale"] == true) matrix.setSaturation(0f)
        val contrast = ((options["contrast"] as? Number)?.toFloat() ?: 1f)
        val brightness = ((options["brightness"] as? Number)?.toFloat() ?: 0f)
        matrix.postConcat(ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )))
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        val file = File(context.cacheDir, "scan_clean_${System.currentTimeMillis()}_$index.jpg")
        FileOutputStream(file).use { outputBitmap.compress(Bitmap.CompressFormat.JPEG, (options["quality"] as? Number)?.toInt() ?: 92, it) }
        if (source.parentFile == context.cacheDir) source.delete()
        bitmap.recycle()
        outputBitmap.recycle()
        return file
    }

    private fun decodeBitmap(file: File): Bitmap? {
        BitmapFactory.decodeFile(file.absolutePath)?.let { return it }
        if (Build.VERSION.SDK_INT >= 28) {
            return android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(file))
        }
        return null
    }

    private fun materialize(context: Context, path: String): File {
        val uri = utils.getURI(path)
        if (uri.scheme == "file" || uri.scheme.isNullOrEmpty()) return File(uri.path ?: path)
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "input"
        val file = File(context.cacheDir, "conversion_${System.currentTimeMillis()}_$name")
        context.contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(file).use { output -> input?.copyTo(output) }
        }
        return file
    }

    private fun readerFor(context: Context, pdfPath: String): PdfReader =
        PdfReader(context.contentResolver.openInputStream(utils.getURI(pdfPath)))

    private fun outputFile(context: Context, suffix: String, extension: String): File =
        File(File(context.cacheDir, "pdf_manipulator").apply { mkdirs() }, "${suffix}_${System.currentTimeMillis()}.$extension")

    private fun selectedPages(pages: List<Int>?, pageCount: Int): List<Int> =
        (pages?.takeIf { it.isNotEmpty() } ?: (1..pageCount).toList()).filter { it in 1..pageCount }

    private fun ZipOutputStream.text(name: String, value: String) {
        putNextEntry(ZipEntry(name))
        write(value.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun slideXml(title: String, body: String): String = """<?xml version="1.0" encoding="UTF-8"?><p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/><p:sp><p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>${escapeXml(title)}</a:t></a:r></a:p></p:txBody></p:sp><p:sp><p:nvSpPr><p:cNvPr id="3" name="Body"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>${escapeXml(body)}</a:t></a:r></a:p></p:txBody></p:sp></p:spTree></p:cSld></p:sld>"""

    private fun columnName(index: Int): String {
        var value = index
        var result = ""
        do {
            result = ('A'.code + (value % 26)).toChar() + result
            value = value / 26 - 1
        } while (value >= 0)
        return result
    }

    private fun escapeHtml(text: String): String = escapeXml(text)
    private fun escapeXml(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun unescapeXml(text: String): String =
        text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&")

    private fun normalizedImageExtension(requestedFormat: String, detected: String?): String {
        if (requestedFormat != "original") return requestedFormat.lowercase().removePrefix(".")
        return when (detected?.lowercase()) {
            "jpg", "jpeg" -> "jpg"
            "jp2" -> "jp2"
            "png" -> "png"
            else -> "bin"
        }
    }

    private fun transcodedImageBytes(bytes: ByteArray, requestedFormat: String): ByteArray {
        val normalized = requestedFormat.lowercase()
        if (normalized == "original") return bytes
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val output = java.io.ByteArrayOutputStream()
        val format = if (normalized == "jpg" || normalized == "jpeg") {
            Bitmap.CompressFormat.JPEG
        } else {
            Bitmap.CompressFormat.PNG
        }
        bitmap.compress(format, 92, output)
        bitmap.recycle()
        return output.toByteArray()
    }
}
