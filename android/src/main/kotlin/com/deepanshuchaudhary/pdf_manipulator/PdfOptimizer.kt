package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import com.itextpdf.kernel.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val LOG_TAG = "PdfOptimizer"

/**
 * PDF optimization utility that reduces file size without compromising quality
 */
suspend fun getOptimizedPDFPath(
    sourceFilePath: String,
    removeMetadata: Boolean,
    removeUnusedObjects: Boolean,
    mergeDuplicateObjects: Boolean,
    optimizeStructure: Boolean,
    isExternal: Boolean,
    context: Activity
): String? = withContext(Dispatchers.IO) {

    Log.d(LOG_TAG, "Starting PDF optimization for: $sourceFilePath")

    val utils = Utils()
    val contentResolver: ContentResolver = context.contentResolver
    val uri = utils.getURI(sourceFilePath)

    val inputFile: File = File.createTempFile("input_optimize", ".pdf")

    try {
        streamingCopyFile(uri, inputFile.toUri(), contentResolver)
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Failed to copy input file", e)
        return@withContext null
    }

    val outputFile: File = File.createTempFile("optimized", ".pdf")

    try {
        val reader = PdfReader(inputFile)

        val writerProperties = WriterProperties().apply {
            if (mergeDuplicateObjects) useSmartMode()
            if (optimizeStructure) setFullCompressionMode(true)
        }
        val writer = PdfWriter(outputFile.absolutePath, writerProperties)

        val inputDocument = PdfDocument(reader)
        val outputDocument = PdfDocument(writer)

        inputDocument.copyPagesTo(1, inputDocument.numberOfPages, outputDocument)

        if (removeMetadata) {
            removeDocumentMetadata(outputDocument)
        }

        if (removeUnusedObjects) {
            for (i in 1..outputDocument.numberOfPages) {
                outputDocument.getPage(i).flush()
            }
        }

        if (optimizeStructure) {
            optimizeDocumentStructure(outputDocument)
        }

        inputDocument.close()
        outputDocument.close()
        inputFile.delete()

        // FIX: pick save directory based on isExternal, then return absolutePath (String)
        val saveDir = if (isExternal) context.getExternalFilesDir(null) else context.filesDir
        val savedFile = File(saveDir, "optimized_${System.currentTimeMillis()}.pdf")
        outputFile.copyTo(savedFile, overwrite = true)
        outputFile.delete()

        Log.d(LOG_TAG, "PDF optimization completed successfully")
        return@withContext savedFile.absolutePath  // FIX: was returning File, must return String

    } catch (e: Exception) {
        Log.e(LOG_TAG, "PDF optimization failed", e)
        inputFile.delete()
        outputFile.delete()
        return@withContext null
    }
}
/**
 * Remove metadata from PDF document
 */
private fun removeDocumentMetadata(document: PdfDocument) {
    try {
        val info = document.documentInfo
        info.title = null
        info.author = null
        info.subject = null
        info.keywords = null
        info.creator = null
        info.producer = null

        // Remove XMP metadata stream from the catalog dictionary
        document.catalog.pdfObject.remove(PdfName.Metadata)

        Log.d(LOG_TAG, "Metadata removed from PDF")
    } catch (e: Exception) {
        Log.w(LOG_TAG, "Failed to remove metadata", e)
    }
}

/**
 * Optimize PDF document structure
 */
private fun optimizeDocumentStructure(document: PdfDocument) {
    try {
        // iText 9: getWriter() is now public — can flush the writer buffer directly
        // Full compression is already configured via WriterProperties; this
        // ensures any buffered objects are written out before the document closes
        document.getWriter().flush()

        Log.d(LOG_TAG, "Document structure optimized")
    } catch (e: Exception) {
        Log.w(LOG_TAG, "Failed to optimize structure", e)
    }
}
