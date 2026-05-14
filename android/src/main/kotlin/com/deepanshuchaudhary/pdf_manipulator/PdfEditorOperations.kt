package com.deepanshuchaudhary.pdf_manipulator

import android.content.Context
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfEditorOperations {
    private val utils = Utils()

    suspend fun createBlankPdf(context: Context, pageCount: Int, width: Float, height: Float): String =
        withContext(Dispatchers.IO) {
            val outputFile = File(File(context.cacheDir, "pdf_manipulator").apply { mkdirs() },
                "blank_${System.currentTimeMillis()}.pdf")
            val document = Document(Rectangle(width, height))
            PdfWriter.getInstance(document, FileOutputStream(outputFile))
            document.open()
            repeat(pageCount.coerceAtLeast(1)) { document.newPage() }
            document.close()
            outputFile.absolutePath
        }

    suspend fun insertBlankPages(
        context: Context,
        pdfPath: String,
        insertAt: Int,
        blankPageCount: Int,
        width: Float?,
        height: Float?
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val pageSize = reader.getPageSizeWithRotation(insertAt.coerceIn(1, reader.numberOfPages))
        val blankSize = Rectangle(width ?: pageSize.width, height ?: pageSize.height)
        val outputFile = utils.getOutputFile(pdfPath, context, "insert_blank")
        copyDocument(outputFile, reader) { document, copy ->
            val target = insertAt.coerceIn(1, reader.numberOfPages + 1)
            for (page in 1..reader.numberOfPages) {
                if (page == target) repeat(blankPageCount.coerceAtLeast(1)) { copy.addPage(blankSize, 0) }
                copy.addPage(copy.getImportedPage(reader, page))
            }
            if (target == reader.numberOfPages + 1) repeat(blankPageCount.coerceAtLeast(1)) { copy.addPage(blankSize, 0) }
        }
        reader.close()
        outputFile.absolutePath
    }

    suspend fun insertPages(
        context: Context,
        pdfPath: String,
        sourcePdfPath: String,
        insertAt: Int,
        sourcePages: List<Int>?
    ): String = withContext(Dispatchers.IO) {
        val targetReader = readerFor(context, pdfPath)
        val sourceReader = readerFor(context, sourcePdfPath)
        val pagesToInsert = validPages(sourcePages, sourceReader.numberOfPages)
        val outputFile = utils.getOutputFile(pdfPath, context, "insert_pages")
        copyDocument(outputFile, targetReader) { _, copy ->
            val target = insertAt.coerceIn(1, targetReader.numberOfPages + 1)
            for (page in 1..targetReader.numberOfPages) {
                if (page == target) pagesToInsert.forEach { copy.addPage(copy.getImportedPage(sourceReader, it)) }
                copy.addPage(copy.getImportedPage(targetReader, page))
            }
            if (target == targetReader.numberOfPages + 1) {
                pagesToInsert.forEach { copy.addPage(copy.getImportedPage(sourceReader, it)) }
            }
        }
        targetReader.close()
        sourceReader.close()
        outputFile.absolutePath
    }

    suspend fun replacePages(
        context: Context,
        pdfPath: String,
        replacementPdfPath: String,
        pageNumbers: List<Int>,
        replacementPages: List<Int>?
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val replacementReader = readerFor(context, replacementPdfPath)
        val targets = pageNumbers.toSet()
        val replacements = validPages(replacementPages, replacementReader.numberOfPages)
        val outputFile = utils.getOutputFile(pdfPath, context, "replace_pages")
        var replacementIndex = 0
        copyDocument(outputFile, reader) { _, copy ->
            for (page in 1..reader.numberOfPages) {
                if (targets.contains(page)) {
                    val sourcePage = replacements[replacementIndex.coerceAtMost(replacements.lastIndex)]
                    copy.addPage(copy.getImportedPage(replacementReader, sourcePage))
                    if (replacementIndex < replacements.lastIndex) replacementIndex++
                } else {
                    copy.addPage(copy.getImportedPage(reader, page))
                }
            }
        }
        reader.close()
        replacementReader.close()
        outputFile.absolutePath
    }

    suspend fun duplicatePages(
        context: Context,
        pdfPath: String,
        pageNumbers: List<Int>,
        insertAfterEachPage: Boolean
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val pages = validPages(pageNumbers, reader.numberOfPages)
        val pageSet = pages.toSet()
        val outputFile = utils.getOutputFile(pdfPath, context, "duplicate_pages")
        copyDocument(outputFile, reader) { _, copy ->
            for (page in 1..reader.numberOfPages) {
                copy.addPage(copy.getImportedPage(reader, page))
                if (insertAfterEachPage && pageSet.contains(page)) {
                    copy.addPage(copy.getImportedPage(reader, page))
                }
            }
            if (!insertAfterEachPage) pages.forEach { copy.addPage(copy.getImportedPage(reader, it)) }
        }
        reader.close()
        outputFile.absolutePath
    }

    suspend fun extractPages(context: Context, pdfPath: String, pageNumbers: List<Int>): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val outputFile = utils.getOutputFile(pdfPath, context, "extracted_pages")
            copyDocument(outputFile, reader) { _, copy ->
                validPages(pageNumbers, reader.numberOfPages).forEach { copy.addPage(copy.getImportedPage(reader, it)) }
            }
            reader.close()
            outputFile.absolutePath
        }

    suspend fun cropPages(
        context: Context,
        pdfPath: String,
        pages: List<Int>?,
        left: Float,
        bottom: Float,
        right: Float,
        top: Float,
        applyToMediaBox: Boolean
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val outputFile = utils.getOutputFile(pdfPath, context, "crop")
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))
        val pageSet = validPages(pages, reader.numberOfPages).toSet()
        val box = PdfArray(floatArrayOf(left, bottom, right, top))
        for (page in pageSet) {
            val dictionary = reader.getPageN(page)
            dictionary.put(PdfName.CROPBOX, box)
            if (applyToMediaBox) dictionary.put(PdfName.MEDIABOX, box)
        }
        stamper.close()
        reader.close()
        outputFile.absolutePath
    }

    suspend fun resizePages(
        context: Context,
        pdfPath: String,
        pages: List<Int>?,
        width: Float,
        height: Float,
        scaleToFit: Boolean
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val pageSet = validPages(pages, reader.numberOfPages).toSet()
        val outputFile = utils.getOutputFile(pdfPath, context, "resize")
        val document = Document(Rectangle(width, height))
        val writer = PdfWriter.getInstance(document, FileOutputStream(outputFile))
        document.open()
        val canvas = writer.directContent
        for (page in 1..reader.numberOfPages) {
            val original = reader.getPageSizeWithRotation(page)
            val newSize = if (pageSet.contains(page)) Rectangle(width, height) else original
            document.setPageSize(newSize)
            document.newPage()
            val imported = writer.getImportedPage(reader, page)
            if (pageSet.contains(page) && scaleToFit) {
                val scale = minOf(width / original.width, height / original.height)
                val x = (width - original.width * scale) / 2f
                val y = (height - original.height * scale) / 2f
                canvas.addTemplate(imported, scale, 0f, 0f, scale, x, y)
            } else {
                canvas.addTemplate(imported, 0f, 0f)
            }
        }
        document.close()
        reader.close()
        outputFile.absolutePath
    }

    suspend fun addPageNumbers(context: Context, pdfPath: String, options: Map<String, Any>): String =
        addTextEachPage(context, pdfPath, "page_numbers") { page, total ->
            val start = (options["startNumber"] as? Number)?.toInt() ?: 1
            val pattern = options["pattern"] as? String ?: "{page}"
            textSpec(options).copy(
                text = pattern.replace("{page}", (start + page - 1).toString()).replace("{total}", total.toString())
            )
        }

    suspend fun addHeadersFooters(context: Context, pdfPath: String, headers: List<Map<String, Any>>, footers: List<Map<String, Any>>): String =
        stampTextBlocks(context, pdfPath, headers + footers, "headers_footers")

    suspend fun addTextBlocks(context: Context, pdfPath: String, blocks: List<Map<String, Any>>): String =
        stampTextBlocks(context, pdfPath, blocks, "text_blocks")

    suspend fun addImages(context: Context, pdfPath: String, images: List<Map<String, Any>>): String =
        stampImages(context, pdfPath, images, "images")

    suspend fun addBackgrounds(context: Context, pdfPath: String, backgrounds: List<Map<String, Any>>): String =
        stampImages(context, pdfPath, backgrounds.map { it + ("underContent" to true) }, "backgrounds")

    suspend fun addStamps(context: Context, pdfPath: String, stamps: List<Map<String, Any>>): String {
        val textStamps = stamps.filter { it["text"] != null }
        val imageStamps = stamps.filter { it["imagePath"] != null }
        val afterText = if (textStamps.isNotEmpty()) stampTextBlocks(context, pdfPath, textStamps, "stamps_text") else pdfPath
        return if (imageStamps.isNotEmpty()) stampImages(context, afterText, imageStamps, "stamps") else afterText
    }

    suspend fun removeAnnotations(context: Context, pdfPath: String, pages: List<Int>?): String =
        mutateAnnotations(context, pdfPath, "remove_annotations") { reader, page ->
            reader.getPageN(page).remove(PdfName.ANNOTS)
        }

    suspend fun flattenAnnotations(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val outputFile = utils.getOutputFile(pdfPath, context, "flatten")
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))
        stamper.setFormFlattening(true)
        stamper.setFreeTextFlattening(true)
        stamper.close()
        reader.close()
        outputFile.absolutePath
    }

    suspend fun editText(context: Context, pdfPath: String, edits: List<Map<String, Any>>): String =
        stampTextBlocks(context, pdfPath, edits.map { it + ("coverExisting" to true) }, "edit_text")

    suspend fun editImages(context: Context, pdfPath: String, edits: List<Map<String, Any>>): String =
        stampImages(context, pdfPath, edits.map { it + ("coverExisting" to true) }, "edit_images")

    fun validatePageOrder(pageCount: Int, pageOrder: List<Int>): Map<String, Any> {
        val duplicates = pageOrder.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.toList()
        val outOfRange = pageOrder.filter { it < 1 || it > pageCount }.distinct()
        val missing = (1..pageCount).filter { !pageOrder.contains(it) }
        return mapOf(
            "isValid" to (duplicates.isEmpty() && outOfRange.isEmpty() && missing.isEmpty()),
            "duplicates" to duplicates,
            "outOfRange" to outOfRange,
            "missing" to missing
        )
    }

    fun movedOrder(pageCount: Int, fromPage: Int, toPage: Int): List<Int> {
        val order = (1..pageCount).toMutableList()
        val page = order.removeAt(fromPage.coerceIn(1, pageCount) - 1)
        order.add(toPage.coerceIn(1, pageCount) - 1, page)
        return order
    }

    fun swappedOrder(pageCount: Int, firstPage: Int, secondPage: Int): List<Int> {
        val order = (1..pageCount).toMutableList()
        val first = firstPage.coerceIn(1, pageCount) - 1
        val second = secondPage.coerceIn(1, pageCount) - 1
        val value = order[first]
        order[first] = order[second]
        order[second] = value
        return order
    }

    private suspend fun addTextEachPage(
        context: Context,
        pdfPath: String,
        suffix: String,
        specFactory: (Int, Int) -> TextSpec
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val outputFile = utils.getOutputFile(pdfPath, context, suffix)
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))
        for (page in 1..reader.numberOfPages) drawText(stamper.getOverContent(page), specFactory(page, reader.numberOfPages))
        stamper.close()
        reader.close()
        outputFile.absolutePath
    }

    private suspend fun stampTextBlocks(context: Context, pdfPath: String, blocks: List<Map<String, Any>>, suffix: String): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val outputFile = utils.getOutputFile(pdfPath, context, suffix)
            val stamper = PdfStamper(reader, FileOutputStream(outputFile))
            blocks.forEach { block ->
                val pages = validPages(block["pages"] as? List<Int>, reader.numberOfPages)
                val spec = textSpec(block)
                pages.forEach { page ->
                    val canvas = if (block["underContent"] == true) stamper.getUnderContent(page) else stamper.getOverContent(page)
                    if (block["coverExisting"] == true) cover(canvas, block)
                    drawText(canvas, spec)
                }
            }
            stamper.close()
            reader.close()
            outputFile.absolutePath
        }

    private suspend fun stampImages(context: Context, pdfPath: String, images: List<Map<String, Any>>, suffix: String): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val outputFile = utils.getOutputFile(pdfPath, context, suffix)
            val stamper = PdfStamper(reader, FileOutputStream(outputFile))
            images.forEach { imageData ->
                val pages = validPages(imageData["pages"] as? List<Int>, reader.numberOfPages)
                pages.forEach { page ->
                    val canvas = if (imageData["underContent"] == true) stamper.getUnderContent(page) else stamper.getOverContent(page)
                    if (imageData["coverExisting"] == true) cover(canvas, imageData)
                    val image = Image.getInstance((imageData["imagePath"] as String))
                    val width = (imageData["width"] as? Number)?.toFloat() ?: image.scaledWidth
                    val height = (imageData["height"] as? Number)?.toFloat() ?: image.scaledHeight
                    image.scaleAbsolute(width, height)
                    image.setAbsolutePosition(number(imageData, "x", 0f), number(imageData, "y", 0f))
                    image.setRotationDegrees(number(imageData, "rotation", 0f))
                    canvas.addImage(image)
                }
            }
            stamper.close()
            reader.close()
            outputFile.absolutePath
        }

    private suspend fun mutateAnnotations(
        context: Context,
        pdfPath: String,
        suffix: String,
        mutation: (PdfReader, Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val outputFile = utils.getOutputFile(pdfPath, context, suffix)
        for (page in 1..reader.numberOfPages) mutation(reader, page)
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))
        stamper.close()
        reader.close()
        outputFile.absolutePath
    }

    private fun readerFor(context: Context, pdfPath: String): PdfReader {
        val inputStream = context.contentResolver.openInputStream(utils.getURI(pdfPath))
        return PdfReader(inputStream)
    }

    private fun copyDocument(outputFile: File, reader: PdfReader, block: (Document, PdfCopy) -> Unit) {
        val document = Document(reader.getPageSizeWithRotation(1))
        val copy = PdfCopy(document, FileOutputStream(outputFile))
        copy.setFullCompression()
        document.open()
        block(document, copy)
        document.close()
        copy.close()
    }

    private fun validPages(pages: List<Int>?, pageCount: Int): List<Int> =
        (pages?.takeIf { it.isNotEmpty() } ?: (1..pageCount).toList()).filter { it in 1..pageCount }

    private fun textSpec(data: Map<String, Any>) = TextSpec(
        text = data["text"] as? String ?: "",
        x = number(data, "x", 36f),
        y = number(data, "y", 36f),
        fontSize = number(data, "fontSize", 12f),
        rotation = number(data, "rotation", 0f),
        color = color(data["color"] as? String),
        align = when (data["align"] as? String) {
            "center" -> Element.ALIGN_CENTER
            "right" -> Element.ALIGN_RIGHT
            else -> Element.ALIGN_LEFT
        }
    )

    private fun drawText(canvas: PdfContentByte, spec: TextSpec) {
        val font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED)
        canvas.saveState()
        canvas.beginText()
        canvas.setFontAndSize(font, spec.fontSize)
        canvas.setColorFill(spec.color)
        canvas.showTextAligned(spec.align, spec.text, spec.x, spec.y, spec.rotation)
        canvas.endText()
        canvas.restoreState()
    }

    private fun cover(canvas: PdfContentByte, data: Map<String, Any>) {
        canvas.saveState()
        canvas.setColorFill(color(data["coverColor"] as? String ?: "#FFFFFFFF"))
        canvas.rectangle(
            number(data, "coverX", number(data, "x", 0f)),
            number(data, "coverY", number(data, "y", 0f)),
            number(data, "coverWidth", number(data, "width", 0f)),
            number(data, "coverHeight", number(data, "height", 0f))
        )
        canvas.fill()
        canvas.restoreState()
    }

    private fun number(data: Map<String, Any>, key: String, fallback: Float): Float =
        (data[key] as? Number)?.toFloat() ?: fallback

    private fun color(value: String?): BaseColor {
        val hex = value?.removePrefix("#")?.padStart(8, 'F') ?: "FF000000"
        val argb = hex.toLong(16).toInt()
        return BaseColor(argb shr 16 and 0xff, argb shr 8 and 0xff, argb and 0xff, argb ushr 24 and 0xff)
    }

    private data class TextSpec(
        val text: String,
        val x: Float,
        val y: Float,
        val fontSize: Float,
        val rotation: Float,
        val color: BaseColor,
        val align: Int
    )
}
