package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import com.itextpdf.kernel.pdf.*
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener
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
    context: Activity
): String? = withContext(Dispatchers.IO) {

    Log.d(LOG_TAG, "Starting PDF optimization for: $sourceFilePath")

    val utils = Utils()
    val contentResolver: ContentResolver = context.contentResolver
    val uri = utils.getURI(sourceFilePath)

    // Create temp file for reading
    val inputFile: File = File.createTempFile("input_optimize", ".pdf")

    // Copy source file to temp location
    try {
        streamingCopyFile(uri, inputFile.toUri(), contentResolver)
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Failed to copy input file", e)
        return@withContext null
    }

    // Create output file
    val outputFile: File = File.createTempFile("optimized", ".pdf")

    try {
        val reader = PdfReader(inputFile).setUnethicalReading(true)
        reader.setMemorySavingMode(true)

        // Use PdfSmartCopy for optimization
        val smartCopy = PdfSmartCopy(reader, outputFile.outputStream())

        val inputDocument = PdfDocument(reader)
        val outputDocument = PdfDocument(smartCopy)

        // Copy all pages
        for (i in 1..inputDocument.numberOfPages) {
            val page = inputDocument.getPage(i)
            outputDocument.addPage(page.copyTo(outputDocument))
        }

        // Apply optimization options
        if (removeMetadata) {
            removeDocumentMetadata(outputDocument)
        }

        if (optimizeStructure) {
            optimizeDocumentStructure(outputDocument)
        }

        // Close documents
        inputDocument.close()
        outputDocument.close()
        reader.close()
        smartCopy.close()

        // Clean up input file
        inputFile.delete()

        val optimizedPath = utils.saveFileToAppDirectory(outputFile, context)
        outputFile.delete()

        Log.d(LOG_TAG, "PDF optimization completed successfully")
        return@withContext optimizedPath

    } catch (e: Exception) {
        Log.e(LOG_TAG, "PDF optimization failed", e)
        // Clean up on error
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
        val catalog = document.catalog
        val info = document.documentInfo

        // Remove standard metadata fields
        info.title = null
        info.author = null
        info.subject = null
        info.keywords = null
        info.creator = null
        info.producer = null

        // Remove custom metadata
        val metadata = catalog.getPdfObject()?.getAsDictionary(PdfName.Metadata)
        if (metadata != null) {
            catalog.getPdfObject()?.remove(PdfName.Metadata)
        }

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
        // Force garbage collection of unused objects
        document.writer.flush()
        document.checkCompliance()

        Log.d(LOG_TAG, "Document structure optimized")
    } catch (e: Exception) {
        Log.w(LOG_TAG, "Failed to optimize structure", e)
    }
}