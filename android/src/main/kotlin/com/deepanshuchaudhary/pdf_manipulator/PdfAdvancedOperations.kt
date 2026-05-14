package com.deepanshuchaudhary.pdf_manipulator

import android.content.Context
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfAction
import com.itextpdf.text.pdf.PdfAnnotation
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfBoolean
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfDate
import com.itextpdf.text.pdf.PdfDestination
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNameTree
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.SimpleBookmark
import com.itextpdf.text.pdf.SimpleNamedDestination
import com.itextpdf.text.pdf.parser.ImageRenderInfo
import com.itextpdf.text.pdf.parser.PdfReaderContentParser
import com.itextpdf.text.pdf.parser.RenderListener
import com.itextpdf.text.pdf.parser.TextRenderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.SignatureException
import java.security.cert.X509Certificate

object PdfAdvancedOperations {
    private val utils = Utils()

    suspend fun verifySignatures(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val fields = reader.acroFields
        val names = fields.signatureNames
        val signatures = names.map { name ->
            val pkcs7 = fields.verifySignature(name)
            val cert = pkcs7.signingCertificate as? X509Certificate
            mapOf(
                "name" to name,
                "coversWholeDocument" to fields.signatureCoversWholeDocument(name),
                "revision" to fields.getRevision(name),
                "totalRevisions" to fields.totalRevisions,
                "integrityValid" to runCatching { pkcs7.verify() }.getOrDefault(false),
                "signDate" to (pkcs7.signDate?.time?.toString() ?: ""),
                "reason" to (pkcs7.reason ?: ""),
                "location" to (pkcs7.location ?: ""),
                "certificateSubject" to (cert?.subjectX500Principal?.name ?: ""),
                "certificateIssuer" to (cert?.issuerX500Principal?.name ?: ""),
                "certificateNotBefore" to (cert?.notBefore?.toString() ?: ""),
                "certificateNotAfter" to (cert?.notAfter?.toString() ?: "")
            )
        }
        reader.close()
        mapOf("hasSignatures" to names.isNotEmpty(), "signatures" to signatures)
    }

    suspend fun validateSignatureCertificates(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val fields = reader.acroFields
        val chains = fields.signatureNames.map { name ->
            val pkcs7 = fields.verifySignature(name)
            val chain = pkcs7.certificates.filterIsInstance<X509Certificate>()
            mapOf(
                "signatureName" to name,
                "chainLength" to chain.size,
                "certificates" to chain.map { cert ->
                    val validNow = runCatching { cert.checkValidity(); true }.getOrDefault(false)
                    mapOf(
                        "subject" to cert.subjectX500Principal.name,
                        "issuer" to cert.issuerX500Principal.name,
                        "serialNumber" to cert.serialNumber.toString(),
                        "validNow" to validNow,
                        "notBefore" to cert.notBefore.toString(),
                        "notAfter" to cert.notAfter.toString()
                    )
                },
                "notes" to "Cryptographic chain path validation requires caller-provided trust anchors/OCSP/CRL policy."
            )
        }
        reader.close()
        mapOf("chains" to chains)
    }

    suspend fun signatureLtvTimestampInfo(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val dss = reader.catalog.getAsDict(PdfName("DSS"))
        val result = mapOf(
            "hasDss" to (dss != null),
            "hasOcsp" to (dss?.getAsArray(PdfName("OCSPs")) != null),
            "hasCrl" to (dss?.getAsArray(PdfName("CRLs")) != null),
            "hasTimestampSignatures" to reader.acroFields.signatureNames.any { name ->
                runCatching { reader.acroFields.verifySignature(name).isTsp }.getOrDefault(false)
            },
            "notes" to "Adding trusted timestamps/LTV requires a TSA/OCSP/CRL service and signing credentials."
        )
        reader.close()
        result
    }

    suspend fun addSignatureFields(context: Context, pdfPath: String, fields: List<Map<String, Any>>): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "signature_fields")
            val stamper = PdfStamper(reader, FileOutputStream(output), '\u0000', true)
            fields.forEach { field ->
                val name = field["name"] as? String ?: return@forEach
                val page = (field["page"] as? Number)?.toInt() ?: 1
                val rect = rectFrom(field) ?: Rectangle(72f, 72f, 220f, 132f)
                val signature = com.itextpdf.text.pdf.PdfFormField.createSignature(stamper.writer)
                signature.setFieldName(name)
                signature.setWidget(rect, PdfAnnotation.HIGHLIGHT_INVERT)
                stamper.addAnnotation(signature, page)
            }
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun eSignStatus(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val fields = reader.acroFields
        val signatures = fields.signatureNames.toSet()
        val allFields = fields.fields.keys.filter { fields.getFieldType(it) == AcroFields.FIELD_TYPE_SIGNATURE }
        reader.close()
        mapOf(
            "status" to if (allFields.isNotEmpty() && allFields.all { signatures.contains(it) }) "complete" else "pending",
            "signed" to signatures.toList(),
            "pending" to allFields.filterNot { signatures.contains(it) }
        )
    }

    suspend fun addAttachments(context: Context, pdfPath: String, paths: List<String>, description: String?): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "attachments")
            val stamper = PdfStamper(reader, FileOutputStream(output), '\u0000', true)
            paths.forEach { path ->
                val file = materialize(context, path)
                val spec = PdfFileSpecification.fileEmbedded(stamper.writer, file.absolutePath, file.name, null)
                stamper.addFileAttachment(description ?: file.name, spec)
            }
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun listAttachments(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val attachments = embeddedFileSpecs(reader).map { (name, spec) ->
            val file = spec.getAsDict(PdfName.EF)?.getAsStream(PdfName.F)
            mapOf("name" to name, "size" to (file?.length() ?: 0))
        }
        reader.close()
        mapOf("attachments" to attachments)
    }

    suspend fun extractAttachments(context: Context, pdfPath: String, outputDir: String): Map<String, Any> =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val dir = File(outputDir).apply { mkdirs() }
            val paths = embeddedFileSpecs(reader).mapNotNull { (name, spec) ->
                val stream = spec.getAsDict(PdfName.EF)?.getAsStream(PdfName.F) ?: return@mapNotNull null
                val safeName = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                val output = File(dir, safeName)
                output.writeBytes(PdfReader.getStreamBytes(stream as PRStream))
                output.absolutePath
            }
            reader.close()
            mapOf("paths" to paths)
        }

    suspend fun removeAttachments(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "remove_attachments")
        val stamper = PdfStamper(reader, FileOutputStream(output))
        reader.catalog.getAsDict(PdfName.NAMES)?.remove(PdfName.EMBEDDEDFILES)
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun createPortfolio(context: Context, pdfPath: String, attachmentPaths: List<String>): String =
        withContext(Dispatchers.IO) {
            val withAttachments = addAttachments(context, pdfPath, attachmentPaths, "Portfolio attachment")
            val reader = readerFor(context, withAttachments)
            val output = utils.getOutputFile(pdfPath, context, "portfolio")
            val stamper = PdfStamper(reader, FileOutputStream(output))
            reader.catalog.put(PdfName.COLLECTION, PdfDictionary())
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun layerInfo(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val ocProps = reader.catalog.getAsDict(PdfName.OCPROPERTIES)
        val ocgs = ocProps?.getAsArray(PdfName.OCGS)
        val layers = (0 until (ocgs?.size() ?: 0)).map { index -> ocgs!!.getPdfObject(index).toString() }
        reader.close()
        mapOf("hasLayers" to layers.isNotEmpty(), "layers" to layers)
    }

    suspend fun articleThreadsInfo(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val threads = reader.catalog.getAsArray(PdfName.THREADS)
        val result = mapOf("count" to (threads?.size() ?: 0), "hasArticleThreads" to ((threads?.size() ?: 0) > 0))
        reader.close()
        result
    }

    suspend fun namedDestinations(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val destinations = SimpleNamedDestination.getNamedDestination(reader, false)
        reader.close()
        mapOf("destinations" to destinations.map { mapOf("name" to it.key.toString(), "destination" to it.value.toString()) })
    }

    suspend fun addNamedDestination(context: Context, pdfPath: String, name: String, page: Int): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "named_destination")
            val stamper = PdfStamper(reader, FileOutputStream(output), '\u0000', true)
            stamper.writer.addNamedDestination(name, page, PdfDestination(PdfDestination.FIT))
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun pageLabels(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val labels = com.itextpdf.text.pdf.PdfPageLabels.getPageLabels(reader)?.toList() ?: emptyList()
        reader.close()
        mapOf("labels" to labels)
    }

    suspend fun addLink(context: Context, pdfPath: String, link: Map<String, Any>): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "link")
        val stamper = PdfStamper(reader, FileOutputStream(output), '\u0000', true)
        val page = (link["page"] as? Number)?.toInt() ?: 1
        val rect = rectFrom(link) ?: Rectangle(72f, 72f, 220f, 96f)
        val annotation = when {
            link["url"] != null -> PdfAnnotation.createLink(
                stamper.writer,
                rect,
                PdfAnnotation.HIGHLIGHT_INVERT,
                PdfAction(link["url"].toString())
            )
            link["destinationPage"] != null -> PdfAnnotation.createLink(
                stamper.writer,
                rect,
                PdfAnnotation.HIGHLIGHT_INVERT,
                PdfAction.gotoLocalPage((link["destinationPage"] as Number).toInt(), PdfDestination(PdfDestination.FIT), stamper.writer)
            )
            else -> null
        }
        if (annotation != null) stamper.addAnnotation(annotation, page)
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun removeLinks(context: Context, pdfPath: String, pages: List<Int>?): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "remove_links")
        val selected = selectedPages(pages, reader.numberOfPages)
        selected.forEach { page ->
            val annots = reader.getPageN(page).getAsArray(PdfName.ANNOTS) ?: return@forEach
            val kept = PdfArray()
            for (index in 0 until annots.size()) {
                val annot = annots.getAsDict(index)
                if (annot?.getAsName(PdfName.SUBTYPE) != PdfName.LINK) kept.add(annots.getPdfObject(index))
            }
            reader.getPageN(page).put(PdfName.ANNOTS, kept)
        }
        val stamper = PdfStamper(reader, FileOutputStream(output))
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun extractTables(context: Context, pdfPath: String, pages: List<Int>?): Map<String, Any> =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val tables = selectedPages(pages, reader.numberOfPages).map { page ->
                val text = com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(reader, page)
                mapOf("page" to page, "rows" to text.lines().map { it.trim().split(Regex("\\s{2,}|\\t")).filter(String::isNotBlank) }.filter { it.size > 1 })
            }
            reader.close()
            mapOf("tables" to tables)
        }

    suspend fun structuredText(context: Context, pdfPath: String, pages: List<Int>?): Map<String, Any> =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val parser = PdfReaderContentParser(reader)
            val result = selectedPages(pages, reader.numberOfPages).map { page ->
                val listener = StructuredTextListener()
                parser.processContent(page, listener)
                mapOf("page" to page, "spans" to listener.spans)
            }
            reader.close()
            mapOf("pages" to result)
        }

    suspend fun visualDiffPdf(context: Context, pdfPath1: String, pdfPath2: String): String = withContext(Dispatchers.IO) {
        val reader1 = readerFor(context, pdfPath1)
        val reader2 = readerFor(context, pdfPath2)
        val output = utils.getOutputFile(pdfPath1, context, "visual_diff")
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(output))
        document.open()
        val maxPages = maxOf(reader1.numberOfPages, reader2.numberOfPages)
        document.add(Paragraph("Visual diff summary"))
        for (page in 1..maxPages) {
            val text1 = if (page <= reader1.numberOfPages) com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(reader1, page) else ""
            val text2 = if (page <= reader2.numberOfPages) com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(reader2, page) else ""
            if (text1 != text2) document.add(Paragraph("Page $page differs"))
        }
        document.close()
        reader1.close()
        reader2.close()
        output.absolutePath
    }

    suspend fun accessibilityInfo(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val catalog = reader.catalog
        val result = mapOf(
            "isTagged" to (catalog.getAsDict(PdfName.STRUCTTREEROOT) != null),
            "hasLang" to (catalog.getAsString(PdfName.LANG)?.toString() ?: ""),
            "hasMarkInfo" to (catalog.getAsDict(PdfName.MARKINFO) != null)
        )
        reader.close()
        result
    }

    suspend fun applyBasicAccessibility(context: Context, pdfPath: String, language: String, title: String): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "accessibility")
            val stamper = PdfStamper(reader, FileOutputStream(output))
            reader.catalog.put(PdfName.LANG, PdfString(language))
            val markInfo = PdfDictionary()
            markInfo.put(PdfName.MARKED, PdfBoolean.PDFTRUE)
            reader.catalog.put(PdfName.MARKINFO, markInfo)
            stamper.moreInfo = mapOf("Title" to title)
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun pdfUaValidation(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val info = accessibilityInfo(context, pdfPath)
        val issues = mutableListOf<String>()
        if (info["isTagged"] != true) issues.add("Missing tag tree")
        if ((info["hasLang"] as String).isBlank()) issues.add("Missing document language")
        mapOf("isLikelyPdfUa" to issues.isEmpty(), "issues" to issues, "notes" to "Full PDF/UA validation requires a standards validator.")
    }

    suspend fun addBatesNumbering(context: Context, pdfPath: String, prefix: String, start: Int): String =
        withContext(Dispatchers.IO) {
            val reader = readerFor(context, pdfPath)
            val output = utils.getOutputFile(pdfPath, context, "bates")
            val stamper = PdfStamper(reader, FileOutputStream(output))
            val font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
            for (page in 1..reader.numberOfPages) {
                val canvas = stamper.getOverContent(page)
                canvas.beginText()
                canvas.setFontAndSize(font, 9f)
                canvas.showTextAligned(1, "$prefix${(start + page - 1).toString().padStart(6, '0')}", 300f, 18f, 0f)
                canvas.endText()
            }
            stamper.close()
            reader.close()
            output.absolutePath
        }

    suspend fun addLegalLabels(context: Context, pdfPath: String, exhibit: String): String =
        PdfEditorOperations.addHeadersFooters(
            context,
            pdfPath,
            listOf(mapOf("text" to exhibit, "x" to 36.0, "y" to 806.0, "fontSize" to 10.0)),
            emptyList()
        )

    suspend fun documentActions(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val catalog = reader.catalog
        val result = mapOf(
            "openAction" to (catalog.get(PdfName.OPENACTION)?.toString() ?: ""),
            "additionalActions" to (catalog.get(PdfName.AA)?.toString() ?: ""),
            "hasJavaScriptNames" to (catalog.getAsDict(PdfName.NAMES)?.get(PdfName.JAVASCRIPT) != null)
        )
        reader.close()
        result
    }

    suspend fun removeDocumentActions(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "remove_actions")
        val stamper = PdfStamper(reader, FileOutputStream(output))
        reader.catalog.remove(PdfName.OPENACTION)
        reader.catalog.remove(PdfName.AA)
        reader.catalog.getAsDict(PdfName.NAMES)?.remove(PdfName.JAVASCRIPT)
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun richMediaInfo(context: Context, pdfPath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        var count = 0
        for (page in 1..reader.numberOfPages) {
            val annots = reader.getPageN(page).getAsArray(PdfName.ANNOTS) ?: continue
            for (index in 0 until annots.size()) {
                val subtype = annots.getAsDict(index)?.getAsName(PdfName.SUBTYPE)
                if (subtype == PdfName("RichMedia") || subtype == PdfName.MOVIE || subtype == PdfName.SCREEN) count++
            }
        }
        reader.close()
        mapOf("richMediaAnnotations" to count)
    }

    suspend fun incrementalSaveCopy(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "incremental")
        val stamper = PdfStamper(reader, FileOutputStream(output), '\u0000', true)
        stamper.close()
        reader.close()
        output.absolutePath
    }

    suspend fun linearizedCopy(context: Context, pdfPath: String): String = withContext(Dispatchers.IO) {
        val reader = readerFor(context, pdfPath)
        val output = utils.getOutputFile(pdfPath, context, "fast_web_view_copy")
        val document = Document()
        val copy = PdfCopy(document, FileOutputStream(output))
        copy.setFullCompression()
        document.open()
        for (page in 1..reader.numberOfPages) copy.addPage(copy.getImportedPage(reader, page))
        document.close()
        copy.close()
        reader.close()
        output.absolutePath
    }

    suspend fun digitalRightsInfo(context: Context, pdfPath: String, password: String): Map<String, Any> =
        withContext(Dispatchers.IO) {
            val reader = if (password.isBlank()) readerFor(context, pdfPath) else PdfReader(context.contentResolver.openInputStream(utils.getURI(pdfPath)), password.toByteArray())
            val permissions = reader.permissions
            val result = mapOf(
                "isEncrypted" to reader.isEncrypted,
                "permissions" to permissions,
                "isOpenedWithFullPermissions" to reader.isOpenedWithFullPermissions,
                "allowPrinting" to ((permissions and PdfWriter.ALLOW_PRINTING.toLong()) == PdfWriter.ALLOW_PRINTING.toLong()),
                "allowCopy" to ((permissions and PdfWriter.ALLOW_COPY.toLong()) == PdfWriter.ALLOW_COPY.toLong()),
                "allowModifyContents" to ((permissions and PdfWriter.ALLOW_MODIFY_CONTENTS.toLong()) == PdfWriter.ALLOW_MODIFY_CONTENTS.toLong()),
                "allowModifyAnnotations" to ((permissions and PdfWriter.ALLOW_MODIFY_ANNOTATIONS.toLong()) == PdfWriter.ALLOW_MODIFY_ANNOTATIONS.toLong())
            )
            reader.close()
            result
        }

    private fun embeddedFileSpecs(reader: PdfReader): List<Pair<String, PdfDictionary>> {
        val names = reader.catalog.getAsDict(PdfName.NAMES)?.getAsDict(PdfName.EMBEDDEDFILES) ?: return emptyList()
        val tree = PdfNameTree.readTree(names)
        return tree.mapNotNull { (name, value) ->
            val filespec = PdfReader.getPdfObject(value) as? PdfDictionary ?: return@mapNotNull null
            name.toString() to filespec
        }
    }

    private fun selectedPages(pages: List<Int>?, pageCount: Int): List<Int> =
        (pages?.takeIf { it.isNotEmpty() } ?: (1..pageCount).toList()).filter { it in 1..pageCount }

    private fun rectFrom(data: Map<String, Any>): Rectangle? {
        val rect = data["rect"] as? List<*>
        if (rect != null && rect.size >= 4) {
            return Rectangle((rect[0] as Number).toFloat(), (rect[1] as Number).toFloat(), (rect[2] as Number).toFloat(), (rect[3] as Number).toFloat())
        }
        val left = (data["left"] as? Number)?.toFloat() ?: return null
        val bottom = (data["bottom"] as? Number)?.toFloat() ?: return null
        val right = (data["right"] as? Number)?.toFloat() ?: return null
        val top = (data["top"] as? Number)?.toFloat() ?: return null
        return Rectangle(left, bottom, right, top)
    }

    private fun materialize(context: Context, path: String): File {
        val uri = utils.getURI(path)
        if (uri.scheme == "file" || uri.scheme.isNullOrEmpty()) return File(uri.path ?: path)
        val file = File(context.cacheDir, "advanced_${System.currentTimeMillis()}_${uri.lastPathSegment?.substringAfterLast('/') ?: "file"}")
        context.contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(file).use { output -> input?.copyTo(output) }
        }
        return file
    }

    private fun readerFor(context: Context, pdfPath: String): PdfReader =
        PdfReader(context.contentResolver.openInputStream(utils.getURI(pdfPath)))

    private class StructuredTextListener : RenderListener {
        val spans = mutableListOf<Map<String, Any>>()
        override fun beginTextBlock() = Unit
        override fun endTextBlock() = Unit
        override fun renderImage(renderInfo: ImageRenderInfo?) = Unit
        override fun renderText(renderInfo: TextRenderInfo) {
            val baseline = renderInfo.baseline
            val ascent = renderInfo.ascentLine
            val descent = renderInfo.descentLine
            spans.add(
                mapOf(
                    "text" to renderInfo.text,
                    "font" to (renderInfo.font?.postscriptFontName ?: ""),
                    "fontSize" to renderInfo.singleSpaceWidth,
                    "left" to minOf(baseline.startPoint[0], baseline.endPoint[0]),
                    "right" to maxOf(baseline.startPoint[0], baseline.endPoint[0]),
                    "top" to maxOf(ascent.startPoint[1], ascent.endPoint[1]),
                    "bottom" to minOf(descent.startPoint[1], descent.endPoint[1])
                )
            )
        }
    }
}
