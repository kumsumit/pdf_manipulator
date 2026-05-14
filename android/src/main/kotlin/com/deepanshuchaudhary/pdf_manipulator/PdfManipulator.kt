package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.Document
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.PdfAnnotation
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.SimpleBookmark
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.security.BouncyCastleDigest
import com.itextpdf.text.pdf.security.ExternalSignature
import com.itextpdf.text.pdf.security.MakeSignature
import com.itextpdf.text.pdf.security.PrivateKeySignature
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate


private const val LOG_TAG = "PdfManipulator"

data class OperationInfo(
    val operationId: String,
    val job: Job,
    val methodChannel: MethodChannel
)

class PdfManipulator(
    private val activity: Activity,
    private val methodChannel: MethodChannel
) {

    private val runningOperations = mutableMapOf<String, OperationInfo>()
    private val operationLock = Any()
    private var job: Job? = null

    private val utils = Utils()

    private data class LocalPdfFile(val file: File, val shouldDelete: Boolean)

    fun createBlankPdf(resultCallback: Result, context: Context, pageCount: Int, width: Double, height: Double) {
        launchEditorOperation(resultCallback, "createBlankPdf") {
            PdfEditorOperations.createBlankPdf(context, pageCount, width.toFloat(), height.toFloat())
        }
    }

    fun insertBlankPages(resultCallback: Result, context: Context, pdfPath: String, insertAt: Int, blankPageCount: Int, width: Double?, height: Double?) {
        launchEditorOperation(resultCallback, "insertBlankPages") {
            PdfEditorOperations.insertBlankPages(context, pdfPath, insertAt, blankPageCount, width?.toFloat(), height?.toFloat())
        }
    }

    fun insertPages(resultCallback: Result, context: Context, pdfPath: String, sourcePdfPath: String, insertAt: Int, sourcePages: List<Int>?) {
        launchEditorOperation(resultCallback, "insertPages") {
            PdfEditorOperations.insertPages(context, pdfPath, sourcePdfPath, insertAt, sourcePages)
        }
    }

    fun replacePages(resultCallback: Result, context: Context, pdfPath: String, replacementPdfPath: String, pageNumbers: List<Int>, replacementPages: List<Int>?) {
        launchEditorOperation(resultCallback, "replacePages") {
            PdfEditorOperations.replacePages(context, pdfPath, replacementPdfPath, pageNumbers, replacementPages)
        }
    }

    fun duplicatePages(resultCallback: Result, context: Context, pdfPath: String, pageNumbers: List<Int>, insertAfterEachPage: Boolean) {
        launchEditorOperation(resultCallback, "duplicatePages") {
            PdfEditorOperations.duplicatePages(context, pdfPath, pageNumbers, insertAfterEachPage)
        }
    }

    fun extractPages(resultCallback: Result, context: Context, pdfPath: String, pageNumbers: List<Int>) {
        launchEditorOperation(resultCallback, "extractPages") {
            PdfEditorOperations.extractPages(context, pdfPath, pageNumbers)
        }
    }

    fun cropPages(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?, left: Double, bottom: Double, right: Double, top: Double, applyToMediaBox: Boolean) {
        launchEditorOperation(resultCallback, "cropPages") {
            PdfEditorOperations.cropPages(context, pdfPath, pages, left.toFloat(), bottom.toFloat(), right.toFloat(), top.toFloat(), applyToMediaBox)
        }
    }

    fun resizePages(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?, width: Double, height: Double, scaleToFit: Boolean) {
        launchEditorOperation(resultCallback, "resizePages") {
            PdfEditorOperations.resizePages(context, pdfPath, pages, width.toFloat(), height.toFloat(), scaleToFit)
        }
    }

    fun addPageNumbers(resultCallback: Result, context: Context, pdfPath: String, options: Map<String, Any>) {
        launchEditorOperation(resultCallback, "addPageNumbers") {
            PdfEditorOperations.addPageNumbers(context, pdfPath, options)
        }
    }

    fun addHeadersFooters(resultCallback: Result, context: Context, pdfPath: String, headers: List<Map<String, Any>>, footers: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "addHeadersFooters") {
            PdfEditorOperations.addHeadersFooters(context, pdfPath, headers, footers)
        }
    }

    fun addBackgrounds(resultCallback: Result, context: Context, pdfPath: String, backgrounds: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "addBackgrounds") {
            PdfEditorOperations.addBackgrounds(context, pdfPath, backgrounds)
        }
    }

    fun addStamps(resultCallback: Result, context: Context, pdfPath: String, stamps: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "addStamps") {
            PdfEditorOperations.addStamps(context, pdfPath, stamps)
        }
    }

    fun addTextBlocks(resultCallback: Result, context: Context, pdfPath: String, blocks: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "addTextBlocks") {
            PdfEditorOperations.addTextBlocks(context, pdfPath, blocks)
        }
    }

    fun addImages(resultCallback: Result, context: Context, pdfPath: String, images: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "addImages") {
            PdfEditorOperations.addImages(context, pdfPath, images)
        }
    }

    fun removeAnnotations(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?) {
        launchEditorOperation(resultCallback, "removeAnnotations") {
            PdfEditorOperations.removeAnnotations(context, pdfPath, pages)
        }
    }

    fun flattenPdf(resultCallback: Result, context: Context, pdfPath: String) {
        launchEditorOperation(resultCallback, "flattenPdf") {
            PdfEditorOperations.flattenAnnotations(context, pdfPath)
        }
    }

    fun editText(resultCallback: Result, context: Context, pdfPath: String, edits: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "editText") {
            PdfEditorOperations.editText(context, pdfPath, edits)
        }
    }

    fun editImages(resultCallback: Result, context: Context, pdfPath: String, edits: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "editImages") {
            PdfEditorOperations.editImages(context, pdfPath, edits)
        }
    }

    fun pdfToWord(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?) {
        launchEditorOperation(resultCallback, "pdfToWord") {
            PdfConversionOperations.pdfToWord(context, pdfPath, pages)
        }
    }

    fun pdfToExcel(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?) {
        launchEditorOperation(resultCallback, "pdfToExcel") {
            PdfConversionOperations.pdfToExcel(context, pdfPath, pages)
        }
    }

    fun pdfToPowerPoint(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?) {
        launchEditorOperation(resultCallback, "pdfToPowerPoint") {
            PdfConversionOperations.pdfToPowerPoint(context, pdfPath, pages)
        }
    }

    fun pdfToHtml(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?) {
        launchEditorOperation(resultCallback, "pdfToHtml") {
            PdfConversionOperations.pdfToHtml(context, pdfPath, pages)
        }
    }

    fun pdfToTextFile(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?) {
        launchEditorOperation(resultCallback, "pdfToTextFile") {
            PdfConversionOperations.pdfToTextFile(context, pdfPath, pages)
        }
    }

    fun documentToPdf(resultCallback: Result, context: Context, documentPath: String) {
        launchEditorOperation(resultCallback, "documentToPdf") {
            PdfConversionOperations.documentToPdf(context, documentPath)
        }
    }

    fun textToPdf(resultCallback: Result, context: Context, text: String) {
        launchEditorOperation(resultCallback, "textToPdf") {
            PdfConversionOperations.textToPdf(context, text)
        }
    }

    fun scannerImagesToPdf(resultCallback: Result, context: Context, imagePaths: List<String>, options: Map<String, Any>) {
        launchEditorOperation(resultCallback, "scannerImagesToPdf") {
            PdfConversionOperations.scannerImagesToPdf(context, imagePaths, options)
        }
    }

    fun pdfAConversion(resultCallback: Result, context: Context, pdfPath: String) {
        launchEditorOperation(resultCallback, "pdfAConversion") {
            PdfConversionOperations.pdfAConversion(context, pdfPath)
        }
    }

    fun pdfAValidation(resultCallback: Result, context: Context, pdfPath: String) {
        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                utils.finishSuccessfullyWithMap(PdfConversionOperations.pdfAValidation(context, pdfPath), resultCallback)
            } catch (e: Exception) {
                utils.finishWithError("pdfAValidation_exception", e.stackTraceToString(), null, resultCallback)
            }
        }
    }

    fun exportEmbeddedImages(resultCallback: Result, context: Context, pdfPath: String, outputDir: String, pages: List<Int>?, format: String) {
        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                utils.finishSuccessfullyWithMap(PdfConversionOperations.exportEmbeddedImages(context, pdfPath, outputDir, pages, format), resultCallback)
            } catch (e: Exception) {
                utils.finishWithError("exportEmbeddedImages_exception", e.stackTraceToString(), null, resultCallback)
            }
        }
    }

    fun redactRegions(resultCallback: Result, context: Context, pdfPath: String, redactions: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "redactRegions") {
            PdfSecurityFormsOperations.redactRegions(context, pdfPath, redactions)
        }
    }

    fun redactSearch(resultCallback: Result, context: Context, pdfPath: String, terms: List<String>, caseSensitive: Boolean) {
        launchEditorOperation(resultCallback, "redactSearch") {
            PdfSecurityFormsOperations.redactSearch(context, pdfPath, terms, caseSensitive)
        }
    }

    fun redactPatterns(resultCallback: Result, context: Context, pdfPath: String, patterns: List<String>) {
        launchEditorOperation(resultCallback, "redactPatterns") {
            PdfSecurityFormsOperations.redactPatterns(context, pdfPath, patterns)
        }
    }

    fun sanitizePdf(resultCallback: Result, context: Context, pdfPath: String, options: Map<String, Any>) {
        launchEditorOperation(resultCallback, "sanitizePdf") {
            PdfSecurityFormsOperations.sanitize(context, pdfPath, options)
        }
    }

    fun ocrToSearchablePdf(resultCallback: Result, context: Context, pdfPath: String, pages: List<Int>?, options: Map<String, Any>) {
        launchEditorOperation(resultCallback, "ocrToSearchablePdf") {
            PdfSecurityFormsOperations.ocrToSearchablePdf(context, pdfPath, pages, options)
        }
    }

    fun createFormFields(resultCallback: Result, context: Context, pdfPath: String, fields: List<Map<String, Any>>) {
        launchEditorOperation(resultCallback, "createFormFields") {
            PdfSecurityFormsOperations.createFormFields(context, pdfPath, fields)
        }
    }

    fun editFormFields(resultCallback: Result, context: Context, pdfPath: String, values: Map<String, Any>, removeFields: List<String>) {
        launchEditorOperation(resultCallback, "editFormFields") {
            PdfSecurityFormsOperations.editFormFields(context, pdfPath, values, removeFields)
        }
    }

    fun xfaInfo(resultCallback: Result, context: Context, pdfPath: String) {
        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                utils.finishSuccessfullyWithMap(PdfSecurityFormsOperations.xfaInfo(context, pdfPath), resultCallback)
            } catch (e: Exception) {
                utils.finishWithError("xfaInfo_exception", e.stackTraceToString(), null, resultCallback)
            }
        }
    }

    fun removeXfa(resultCallback: Result, context: Context, pdfPath: String) {
        launchEditorOperation(resultCallback, "removeXfa") {
            PdfSecurityFormsOperations.removeXfa(context, pdfPath)
        }
    }

    fun advancedString(resultCallback: Result, errorPrefix: String, operation: suspend () -> String) {
        launchEditorOperation(resultCallback, errorPrefix, operation)
    }

    fun advancedMap(resultCallback: Result, errorPrefix: String, operation: suspend () -> Map<String, Any>) {
        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                utils.finishSuccessfullyWithMap(operation(), resultCallback)
            } catch (e: Exception) {
                utils.finishWithError("${errorPrefix}_exception", e.stackTraceToString(), null, resultCallback)
            } catch (e: OutOfMemoryError) {
                utils.finishWithError("${errorPrefix}_OutOfMemoryError", e.stackTraceToString(), null, resultCallback)
            }
        }
    }

    private fun launchEditorOperation(resultCallback: Result, errorPrefix: String, operation: suspend () -> String) {
        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                utils.finishSuccessfullyWithString(operation(), resultCallback)
            } catch (e: Exception) {
                utils.finishWithError("${errorPrefix}_exception", e.stackTraceToString(), null, resultCallback)
            } catch (e: OutOfMemoryError) {
                utils.finishWithError("${errorPrefix}_OutOfMemoryError", e.stackTraceToString(), null, resultCallback)
            }
        }
    }

    // For merging multiple pdf files.
    fun mergePdfs(
        resultCallback: Result,
        sourceFilesPaths: List<String>?,
    ) {
        Log.d(
            LOG_TAG, "mergePdfs - IN, sourceFilesPaths=$sourceFilesPaths"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val mergedPDFPath: String? = getMergedPDFPath(sourceFilesPaths!!, activity)

                utils.finishSuccessfullyWithString(mergedPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "mergePdfs_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "mergePdfs_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "mergePdfs - OUT")
    }

    // For merging multiple pdf files.
    fun splitPdf(
        resultCallback: Result,
        sourceFilePath: String?,
        pageCount: Int,
        byteSize: Number?,
        pageNumbers: List<Int>?,
        pageRanges: List<String>?,
        pageRange: String?,
    ) {
        Log.d(
            LOG_TAG,
            "splitPdf - IN, sourceFilePath=$sourceFilePath, pageCount=$pageCount, byteSize=$byteSize, pageNumbers=$pageNumbers, pageRanges=$pageRanges, pageRange=$pageRange"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val splitPDFPaths: List<String>? = if (byteSize != null) {
                    getSplitPDFPathsByByteSize(
                        sourceFilePath!!, byteSize.toLong(), activity
                    )
                } else if (pageNumbers != null) {
                    getSplitPDFPathsByPageNumbers(sourceFilePath!!, pageNumbers, activity)
                } else if (pageRanges != null) {
                    getSplitPDFPathsByPageRanges(sourceFilePath!!, pageRanges, activity)
                } else if (pageRange != null) {
                    getSplitPDFPathsByPageRange(sourceFilePath!!, pageRange, activity)
                } else {
                    getSplitPDFPathsByPageCount(sourceFilePath!!, pageCount, activity)
                }
                utils.finishSplitSuccessfullyWithListOfString(splitPDFPaths, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "splitPdf_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "splitPdf_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "splitPdf - OUT")
    }

    // For removing pages from pdf.
    fun pdfPageDeleter(
        resultCallback: Result,
        sourceFilePath: String?,
        pageNumbers: List<Int>?,
    ) {
        Log.d(
            LOG_TAG, "removePdfPages - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? =
                    getPDFPageDeleter(sourceFilePath!!, pageNumbers!!, activity)

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "removePdfPages_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "removePdfPages_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "removePdfPages - OUT")
    }

    // For reordering pages of pdf.
    fun pdfPageReorder(
        resultCallback: Result,
        sourceFilePath: String?,
        pageNumbers: List<Int>?,
    ) {
        Log.d(
            LOG_TAG, "pdfPageReorder - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? =
                    getPDFPageReorder(sourceFilePath!!, pageNumbers!!, activity)

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfPageReorder_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfPageReorder_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfPageReorder - OUT")
    }

    // For rotating pages of pdf.
    fun pdfPageRotator(
        resultCallback: Result,
        sourceFilePath: String?,
        pagesRotationInfo: List<Map<String, Int>>?,
    ) {
        Log.d(
            LOG_TAG, "pdfPageRotator - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val newPagesRotationInfo: MutableList<PageRotationInfo> = mutableListOf()

                pagesRotationInfo!!.forEach {
                    val temp = PageRotationInfo(
                        pageNumber = it["pageNumber"]!!, rotationAngle = it["rotationAngle"]!!
                    )
                    newPagesRotationInfo.add(temp)
                }

                val resultPDFPath: String? =
                    getPDFPageRotator(sourceFilePath!!, newPagesRotationInfo, activity)

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfPageRotator_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfPageRotator_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfPageRotator - OUT")
    }

    // For reordering, deleting, rotating pages of pdf.
    fun pdfPageRotatorDeleterReorder(
        resultCallback: Result,
        sourceFilePath: String?,
        pageNumbersForReorder: List<Int>,
        pageNumbersForDeleter: List<Int>,
        pagesRotationInfo: List<Map<String, Int>>,
    ) {
        Log.d(
            LOG_TAG, "pdfPageRotatorDeleterReorder - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val newPagesRotationInfo: MutableList<PageRotationInfo> = mutableListOf()

                pagesRotationInfo.forEach {
                    val temp = PageRotationInfo(
                        pageNumber = it["pageNumber"]!!, rotationAngle = it["rotationAngle"]!!
                    )
                    newPagesRotationInfo.add(temp)
                }

                val resultPDFPath: String? = getPDFPageRotatorDeleterReorder(
                    sourceFilePath!!,
                    pageNumbersForReorder,
                    pageNumbersForDeleter,
                    newPagesRotationInfo,
                    activity
                )

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfPageRotatorDeleterReorder_exception",
                    e.stackTraceToString(),
                    null,
                    resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfPageRotatorDeleterReorder_OutOfMemoryError",
                    e.stackTraceToString(),
                    null,
                    resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfPageRotatorDeleterReorder - OUT")
    }


    // For compressing pdf.
    fun pdfCompressor(
        resultCallback: Result,
        operationId: String,
        sourceFilePath: String?,
        imageQuality: Int?,
        imageScale: Double?,
        unEmbedFonts: Boolean?,
        advancedOptions: Map<String, Any>?,
    ) {
        Log.d(
            LOG_TAG, "pdfCompressor - IN, operationId=$operationId, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        lateinit var compressionJob: Job
        compressionJob = uiScope.launch {
            try {
                // Register the operation
                synchronized(operationLock) {
                    runningOperations[operationId] = OperationInfo(operationId, compressionJob, methodChannel)
                }

                val resultPDFPath: String? = getCompressedPDFPathWithProgress(
                    sourceFilePath!!, imageQuality!!, imageScale!!, unEmbedFonts!!, advancedOptions, activity, operationId, methodChannel
                )

                // Remove the operation from tracking
                synchronized(operationLock) {
                    runningOperations.remove(operationId)
                }

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                // Remove the operation from tracking on error
                synchronized(operationLock) {
                    runningOperations.remove(operationId)
                }
                utils.finishWithError(
                    "pdfCompressor_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                // Remove the operation from tracking on error
                synchronized(operationLock) {
                    runningOperations.remove(operationId)
                }
                utils.finishWithError(
                    "pdfCompressor_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfCompressor - OUT")
    }

    // For optimizing pdf (reduce size without quality loss).
    fun pdfOptimizer(
        resultCallback: Result,
        sourceFilePath: String?,
        removeMetadata: Boolean?,
        removeUnusedObjects: Boolean?,
        mergeDuplicateObjects: Boolean?,
        optimizeStructure: Boolean?,
        isExternal: Boolean?,
    ) {
        Log.d(
            LOG_TAG, "pdfOptimizer - IN, sourceFilePath=$sourceFilePath, isExternal=$isExternal"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? = getOptimizedPDFPath(
                    sourceFilePath!!,
                    removeMetadata ?: false,
                    removeUnusedObjects ?: true,
                    mergeDuplicateObjects ?: true,
                    optimizeStructure ?: true,
                    isExternal ?: true,
                    activity
                )

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfOptimizer_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfOptimizer_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfOptimizer - OUT")
    }

    // For compressing pdf.
    fun watermarkPdf(
        resultCallback: Result,
        sourceFilePath: String?,
        text: String?,
        imagePath: String?,
        fontSize: Double?,
        watermarkLayer: WatermarkLayer?,
        opacity: Double?,
        rotationAngle: Double?,
        watermarkColor: String?,
        positionType: PositionType?,
        customPositionXCoordinatesList: List<Double>?,
        customPositionYCoordinatesList: List<Double>?,
        imageWidth: Double?,
        imageHeight: Double?,
    ) {
        Log.d(
            LOG_TAG, "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? = getWatermarkedPDFPath(
                    sourceFilePath!!,
                    text,
                    imagePath,
                    fontSize!!,
                    watermarkLayer!!,
                    opacity!!,
                    rotationAngle!!,
                    watermarkColor!!,
                    positionType!!,
                    customPositionXCoordinatesList ?: listOf(),
                    customPositionYCoordinatesList ?: listOf(),
                    imageWidth,
                    imageHeight,
                    activity
                )

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfCompressor_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfCompressor_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfCompressor - OUT")
    }

    // For pdf pages size.
    fun pdfPagesSize(
        resultCallback: Result,
        sourceFilePath: String?,
    ) {
        Log.d(
            LOG_TAG, "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val result: List<List<Double>> = getPDFPagesSize(
                    sourceFilePath!!, activity
                )
                if (result.isEmpty()) {
                    utils.finishSplitSuccessfullyWithListOfListOfDouble(null, resultCallback)
                } else {
                    utils.finishSplitSuccessfullyWithListOfListOfDouble(result, resultCallback)
                }
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfCompressor_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfCompressor_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfCompressor - OUT")
    }

    // For pdf validity and protection.
    fun pdfValidityAndProtection(
        resultCallback: Result,
        sourceFilePath: String?,
        userOrOwnerPassword: String?,
    ) {
        Log.d(
            LOG_TAG, "pdfValidityAndProtection - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val result: List<Boolean?> = getPdfValidityAndProtection(
                    sourceFilePath!!, userOrOwnerPassword!!, activity
                )

                utils.finishSplitSuccessfullyWithListOfBoolean(result, resultCallback)

            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfValidityAndProtection_exception",
                    e.stackTraceToString(),
                    null,
                    resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfValidityAndProtection_OutOfMemoryError",
                    e.stackTraceToString(),
                    null,
                    resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfValidityAndProtection - OUT")
    }

    // For pdf decryption.
    fun pdfDecryption(
        resultCallback: Result,
        sourceFilePath: String?,
        userOrOwnerPassword: String?,
    ) {
        Log.d(
            LOG_TAG, "pdfDecryption - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val result: String? = getPdfDecrypted(
                    sourceFilePath!!, userOrOwnerPassword!!, activity
                )

                utils.finishSuccessfullyWithString(result, resultCallback)

            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfDecryption_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfDecryption_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfDecryption - OUT")
    }

    // For pdf encryption.
    fun pdfEncryption(
        resultCallback: Result,
        sourceFilePath: String?,
        ownerPassword: String?,
        userPassword: String?,
        allowPrinting: Boolean,
        allowModifyContents: Boolean,
        allowCopy: Boolean,
        allowModifyAnnotations: Boolean,
        allowFillIn: Boolean,
        allowScreenReaders: Boolean,
        allowAssembly: Boolean,
        allowDegradedPrinting: Boolean,
        standardEncryptionAES40: Boolean,
        standardEncryptionAES128: Boolean,
        encryptionAES128: Boolean,
        encryptionAES256: Boolean,
        encryptEmbeddedFilesOnly: Boolean,
        doNotEncryptMetadata: Boolean,
    ) {
        Log.d(
            LOG_TAG, "pdfEncryption - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val pdfPath: String? = getPdfEncrypted(
                    sourceFilePath!!,
                    ownerPassword!!,
                    userPassword!!,
                    allowPrinting,
                    allowModifyContents,
                    allowCopy,
                    allowModifyAnnotations,
                    allowFillIn,
                    allowScreenReaders,
                    allowAssembly,
                    allowDegradedPrinting,
                    standardEncryptionAES40,
                    standardEncryptionAES128,
                    encryptionAES128,
                    encryptionAES256,
                    encryptEmbeddedFilesOnly,
                    doNotEncryptMetadata,
                    activity
                )

                utils.finishSuccessfullyWithString(pdfPath, resultCallback)

            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfEncryption_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfEncryption_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfEncryption - OUT")
    }

    // For certificate-based pdf encryption.
    fun pdfCertificateEncryption(
        resultCallback: Result,
        sourceFilePath: String?,
        recipients: List<Map<String, Any>>,
        standardEncryptionAES40: Boolean,
        standardEncryptionAES128: Boolean,
        encryptionAES128: Boolean,
        encryptionAES256: Boolean,
        encryptEmbeddedFilesOnly: Boolean,
        doNotEncryptMetadata: Boolean,
    ) {
        Log.d(
            LOG_TAG, "pdfCertificateEncryption - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val pdfPath: String? = getPdfCertificateEncrypted(
                    sourceFilePath!!,
                    recipients,
                    standardEncryptionAES40,
                    standardEncryptionAES128,
                    encryptionAES128,
                    encryptionAES256,
                    encryptEmbeddedFilesOnly,
                    doNotEncryptMetadata,
                    activity
                )

                utils.finishSuccessfullyWithString(pdfPath, resultCallback)

            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfCertificateEncryption_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfCertificateEncryption_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfCertificateEncryption - OUT")
    }

    // For converting images to pdfs.
    fun imagesToPdfs(
        resultCallback: Result,
        sourceImagesPaths: List<String>?,
        createSinglePdf: Boolean?
    ) {
        Log.d(
            LOG_TAG,
            "imagesToPdfs - IN, sourceImagesPaths = $sourceImagesPaths, createSinglePdf = $createSinglePdf"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val result: List<String> = getPdfsFromImages(
                    sourceImagesPaths!!, createSinglePdf!!, activity,
                )
                if (result.isEmpty()) {
                    utils.finishSplitSuccessfullyWithListOfString(null, resultCallback)
                } else {
                    utils.finishSplitSuccessfullyWithListOfString(result, resultCallback)
                }

            } catch (e: Exception) {
                Log.e(LOG_TAG, "imagesToPdfs_exception", e)
                utils.finishWithError(
                    "imagesToPdfs_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                Log.e(LOG_TAG, "imagesToPdfs_OutOfMemoryError", e)
                utils.finishWithError(
                    "imagesToPdfs_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "imagesToPdfs - OUT")
    }

    fun cancelManipulations(operationId: String?): String {
        if (operationId != null) {
            // Cancel specific operation
            synchronized(operationLock) {
                val operationInfo = runningOperations[operationId]
                if (operationInfo != null) {
                    operationInfo.job.cancel()
                    runningOperations.remove(operationId)
                    Log.d(LOG_TAG, "Canceled operation: $operationId")
                    return "Canceled operation: $operationId"
                } else {
                    Log.w(LOG_TAG, "Operation not found: $operationId")
                    return "Operation not found: $operationId"
                }
            }
        } else {
            // Cancel all operations
            val canceledCount: Int
            synchronized(operationLock) {
                canceledCount = runningOperations.size
                for ((id, operationInfo) in runningOperations) {
                    operationInfo.job.cancel()
                    Log.d(LOG_TAG, "Canceled operation: $id")
                }
                runningOperations.clear()
            }
            Log.d(LOG_TAG, "Canceled all manipulations")
            return "Canceled $canceledCount operation(s)"
        }
    }



    fun extractImagesFromPdf(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        outputDir: String
    ) {
        Log.d(LOG_TAG, "extractImageFromPdf - IN, pdfPath=$pdfPath, outputDir=$outputDir")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val pdfUri = utils.getURI(pdfPath)
                val inputStream = context.contentResolver.openInputStream(pdfUri)
                val pdfBytes = inputStream?.readBytes()
                inputStream?.close()

                if (pdfBytes != null) {
                    val extractedImagePaths = extractImagesFromPdf(pdfBytes, outputDir)
                    utils.finishSplitSuccessfullyWithListOfString(extractedImagePaths, resultCallback)
                } else {
                    utils.finishWithError(
                        "extractImagesFromPdf_error",
                        "Failed to read PDF file",
                        null,
                        resultCallback
                    )
                }
            } catch (e: Exception) {
                utils.finishWithError(
                    "extractImagesFromPdf_exception",
                    e.stackTraceToString(),
                    null,
                    resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "extractImagesFromPdf_OutOfMemoryError",
                    e.stackTraceToString(),
                    null,
                    resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "extractImageFromPdf - OUT")
    }

    // For converting PDF pages to images
    fun pdfToImages(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        pages: List<Int>?,
        imageFormat: String,
        quality: Int,
        scale: Double
    ) {
        Log.d(LOG_TAG, "pdfToImages - IN, pdfPath=$pdfPath, pages=$pages, imageFormat=$imageFormat, quality=$quality, scale=$scale")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val generatedImagePaths = convertPdfToImages(
                    context, pdfPath, pages, imageFormat, quality, scale.toFloat()
                )
                utils.finishSplitSuccessfullyWithListOfString(generatedImagePaths, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfToImages_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfToImages_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfToImages - OUT")
    }

    // For extracting text from PDF pages
    fun pdfTextExtraction(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        pages: List<Int>?
    ) {
        Log.d(LOG_TAG, "pdfTextExtraction - IN, pdfPath=$pdfPath, pages=$pages")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val extractionResult = extractTextFromPdf(context, pdfPath, pages)
                utils.finishSuccessfullyWithMap(extractionResult, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfTextExtraction_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfTextExtraction_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfTextExtraction - OUT")
    }

    // For performing OCR on PDF pages
    fun pdfOcr(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        pages: List<Int>?,
        languageCode: String
    ) {
        Log.d(LOG_TAG, "pdfOcr - IN, pdfPath=$pdfPath, pages=$pages, languageCode=$languageCode")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val ocrResult = performOcrOnPdf(context, pdfPath, pages, languageCode)
                utils.finishSuccessfullyWithMap(ocrResult, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfOcr_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfOcr_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfOcr - OUT")
    }

    // For adding digital signature to PDF
    fun pdfDigitalSignature(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        certificatePath: String,
        certificatePassword: String,
        reason: String?,
        location: String?,
        contact: String?,
        appearance: Map<String, Any>?
    ) {
        Log.d(LOG_TAG, "pdfDigitalSignature - IN, pdfPath=$pdfPath, certificatePath=$certificatePath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val signedPdfPath = addDigitalSignature(
                    context,
                    pdfPath,
                    certificatePath,
                    certificatePassword,
                    reason,
                    location,
                    contact,
                    appearance
                )
                utils.finishSuccessfullyWithString(signedPdfPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfDigitalSignature_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfDigitalSignature_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfDigitalSignature - OUT")
    }

    // For adding annotations to PDF
    fun pdfAnnotations(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        annotations: List<Map<String, Any>>
    ) {
        Log.d(LOG_TAG, "pdfAnnotations - IN, pdfPath=$pdfPath, annotationsCount=${annotations.size}")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val annotatedPdfPath = addAnnotationsToPdf(context, pdfPath, annotations)
                utils.finishSuccessfullyWithString(annotatedPdfPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfAnnotations_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfAnnotations_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfAnnotations - OUT")
    }

    // For filling PDF form fields
    fun fillFormFields(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        fieldValues: Map<String, Any>,
        flatten: Boolean
    ) {
        Log.d(LOG_TAG, "fillFormFields - IN, pdfPath=$pdfPath, fieldsCount=${fieldValues.size}")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val filledPdfPath = fillPdfFormFields(context, pdfPath, fieldValues, flatten)
                utils.finishSuccessfullyWithString(filledPdfPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "fillFormFields_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "fillFormFields_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "fillFormFields - OUT")
    }

    // For extracting PDF form field data
    fun extractFormFieldData(
        resultCallback: Result,
        context: Context,
        pdfPath: String
    ) {
        Log.d(LOG_TAG, "extractFormFieldData - IN, pdfPath=$pdfPath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val formFieldData = extractPdfFormFieldData(context, pdfPath)
                utils.finishSuccessfullyWithMap(formFieldData, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "extractFormFieldData_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "extractFormFieldData_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "extractFormFieldData - OUT")
    }

    // Usage example
//    val pdfUri = Uri.parse("content://path/to/your/pdf/document.pdf")
//    val outputDir = "path/to/your/output/directory"
//    extractImagesFromPdf(context, pdfUri, outputDir)



//    fun extractImagesFromPdf( pdfBytes: ByteArray?): List<File> {
//        val pdfReader = PdfReader(pdfBytes)
//        val images = mutableListOf<File>()
//        for (i in 0 until pdfReader.numberOfPages) {
//            val parser = PdfReaderContentParser(pdfReader)
//            val imageProcessor = parser.processContent(i, )//   processContent(i, parser.getClass().getClassLoader())
//            val image = imageProcessor.getImage()
//            if (image != null) {
//                images.add(File("path/to/save/image_${i}.jpg"))
//                // Save the image to the file
//            }
//        }
//        pdfReader.close()
//        return images
//    }


//     fun extractImagesFromPdf(result: Result, pdfBytes: ByteArray?) {
//
//        val uiScope = CoroutineScope(Dispatchers.Main)
//        job = uiScope.launch {
//
//            if (pdfBytes == null) {
//                result.error("invalid_argument", "PDF bytes are null", null)
//                return@launch
//            }
//
//            val context = activity
//
//            try {
//                val extractedImages =  getExtractImagesFromPdf(pdfBytes, context)
//                result.success(extractedImages)
//            } catch (e: Exception) {
//                result.error("extraction_failed", "Failed to extract images: ${e.message}", null)
//            }
//        }
//    }

    private suspend fun convertPdfToImages(
        context: Context,
        pdfPath: String,
        pages: List<Int>?,
        imageFormat: String,
        quality: Int,
        scale: Float
    ): List<String> = withContext(Dispatchers.IO) {
        val imagePaths = mutableListOf<String>()

        try {
            val pdfUri = utils.getURI(pdfPath)
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IOException("Cannot open PDF file")

            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val pageCount = pdfRenderer.pageCount

            // Determine which pages to convert
            val pagesToConvert = pages?.takeIf { it.isNotEmpty() } ?: (0 until pageCount).toList()

            // Create output directory in cache
            val outputDir = File(context.cacheDir, "pdf_images").apply {
                if (!exists()) mkdirs()
            }

            for (pageIndex in pagesToConvert) {
                if (pageIndex !in 0..<pageCount) continue

                val page = pdfRenderer.openPage(pageIndex)

                // Calculate scaled dimensions
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()

                // Create bitmap with scaled dimensions
                val bitmap = createBitmap(width, height)

                // Render page to bitmap
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE) // White background
                canvas.scale(scale, scale)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Generate file name
                val fileName = "page_${pageIndex + 1}.${imageFormat.lowercase()}"
                val imageFile = File(outputDir, fileName)

                // Save bitmap to file
                FileOutputStream(imageFile).use { out ->
                    when (imageFormat.lowercase()) {
                        "png" -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        "jpeg", "jpg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                        "webp" -> bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out)
                        else -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }

                imagePaths.add(imageFile.absolutePath)

                // Clean up
                page.close()
                bitmap.recycle()
            }

            pdfRenderer.close()
            parcelFileDescriptor.close()

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error converting PDF to images", e)
            throw e
        }

        return@withContext imagePaths
    }

    private suspend fun extractTextFromPdf(
        context: Context,
        pdfPath: String,
        pages: List<Int>?
    ): Map<String, Any> = withContext(Dispatchers.IO) {
        val pageTexts = mutableMapOf<String, String>()
        val fullText = StringBuilder()

        try {
            val pdfUri = utils.getURI(pdfPath)
            val inputStream = context.contentResolver.openInputStream(pdfUri)
                ?: throw IOException("Cannot open PDF file")

            val pdfReader = PdfReader(inputStream)
            val numberOfPages = pdfReader.numberOfPages

            // Determine which pages to extract
            val pagesToExtract = pages?.takeIf { it.isNotEmpty() } ?: (1..numberOfPages).toList()

            for (pageNum in pagesToExtract) {
                if (pageNum in 1..numberOfPages) {
                    try {
                        val pageText = PdfTextExtractor.getTextFromPage(pdfReader, pageNum)
                        pageTexts[pageNum.toString()] = pageText
                        if (fullText.isNotEmpty()) {
                            fullText.append("\n\n")
                        }
                        fullText.append("Page $pageNum:\n").append(pageText)
                    } catch (e: Exception) {
                        Log.w(LOG_TAG, "Failed to extract text from page $pageNum", e)
                        pageTexts[pageNum.toString()] = ""
                    }
                }
            }

            pdfReader.close()
            inputStream.close()

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error extracting text from PDF", e)
            throw e
        }

        return@withContext mapOf(
            "pageTexts" to pageTexts,
            "fullText" to fullText.toString()
        )
    }

    private suspend fun performOcrOnPdf(
        context: Context,
        pdfPath: String,
        pages: List<Int>?,
        languageCode: String
    ): Map<String, Any> = withContext(Dispatchers.IO) {
        val pageResults = mutableMapOf<String, Map<String, Any>>()
        val fullText = StringBuilder()
        val pdfUri = utils.getURI(pdfPath)
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IOException("Cannot open PDF file")
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            val selectedPages = pages?.takeIf { it.isNotEmpty() }
                ?: (1..pdfRenderer.pageCount).toList()

            for (pageNumber in selectedPages) {
                val pageIndex = pageNumber - 1
                if (pageIndex !in 0 until pdfRenderer.pageCount) continue

                val page = pdfRenderer.openPage(pageIndex)
                val bitmap = createBitmap(page.width, page.height)
                try {
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val visionText = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)))
                    val confidence = calculateAverageConfidence(visionText)
                    pageResults[pageNumber.toString()] = mapOf(
                        "text" to visionText.text,
                        "confidence" to confidence
                    )

                    if (fullText.isNotEmpty()) fullText.append("\n\n")
                    fullText.append(visionText.text)
                } finally {
                    page.close()
                    bitmap.recycle()
                }
            }
        } finally {
            recognizer.close()
            pdfRenderer.close()
            parcelFileDescriptor.close()
        }

        return@withContext mapOf(
            "pageResults" to pageResults,
            "fullText" to fullText.toString(),
            "languageCode" to languageCode
        )
    }

    private suspend fun fillPdfFormFields(
        context: Context,
        pdfPath: String,
        fieldValues: Map<String, Any>,
        flatten: Boolean
    ): String = withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "form_filled_pdfs").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(outputDir, "form_filled_${System.currentTimeMillis()}.pdf")

        val inputStream = openPdfInputStream(context, pdfPath)
        val reader = PdfReader(inputStream)
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        try {
            val fields = stamper.acroFields
            fields.setGenerateAppearances(true)

            val missingFields = mutableListOf<String>()
            fieldValues.forEach { (name, value) ->
                val fieldValue = normalizeFormFieldValue(fields, name, value)
                if (!fields.setField(name, fieldValue)) {
                    missingFields.add(name)
                }
            }

            if (missingFields.isNotEmpty()) {
                throw IllegalArgumentException(
                    "PDF form does not contain field(s): ${missingFields.joinToString(", ")}"
                )
            }

            stamper.setFormFlattening(flatten)
        } finally {
            stamper.close()
            reader.close()
            inputStream.close()
        }

        return@withContext outputFile.absolutePath
    }

    private suspend fun extractPdfFormFieldData(
        context: Context,
        pdfPath: String
    ): Map<String, Any> = withContext(Dispatchers.IO) {
        val inputStream = openPdfInputStream(context, pdfPath)
        val reader = PdfReader(inputStream)

        try {
            val acroFields = reader.acroFields
            val fields = mutableMapOf<String, Any>()

            acroFields.fields.keys.sorted().forEach { fieldName ->
                val fieldType = acroFields.getFieldType(fieldName)
                val options = getFormFieldOptions(acroFields, fieldName)
                val item = acroFields.getFieldItem(fieldName)
                val fieldFlags = item?.getMerged(0)?.getAsNumber(PdfName.FF)?.intValue() ?: 0

                fields[fieldName] = mapOf(
                    "name" to fieldName,
                    "value" to (acroFields.getField(fieldName) ?: ""),
                    "type" to formFieldTypeName(fieldType),
                    "options" to options,
                    "isRequired" to ((fieldFlags and 2) != 0)
                )
            }

            return@withContext mapOf("fields" to fields)
        } finally {
            reader.close()
            inputStream.close()
        }
    }

    private fun openPdfInputStream(context: Context, pdfPath: String): FileInputStream {
        val uri = pdfPath.toUri()
        if (uri.scheme == null || uri.scheme == "file") {
            val filePath = uri.path ?: pdfPath
            return FileInputStream(File(filePath))
        }

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open PDF file")

        val tempFile = File.createTempFile("pdf_form_input_", ".pdf", context.cacheDir)
        tempFile.deleteOnExit()
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use { it.copyTo(outputStream) }
        }

        return FileInputStream(tempFile)
    }

    private fun normalizeFormFieldValue(
        fields: AcroFields,
        fieldName: String,
        value: Any
    ): String {
        if (value is Boolean) {
            return if (value) {
                fields.getAppearanceStates(fieldName)
                    ?.firstOrNull { !it.equals("Off", ignoreCase = true) }
                    ?: "Yes"
            } else {
                "Off"
            }
        }

        return value.toString()
    }

    private fun getFormFieldOptions(fields: AcroFields, fieldName: String): List<String> {
        val appearanceStates = fields.getAppearanceStates(fieldName)
            ?.filter { !it.equals("Off", ignoreCase = true) }
            ?: listOf()
        val displayOptions = fields.getListOptionDisplay(fieldName)?.toList() ?: listOf()
        val exportOptions = fields.getListOptionExport(fieldName)?.toList() ?: listOf()

        return (displayOptions + exportOptions + appearanceStates).distinct()
    }

    private fun formFieldTypeName(fieldType: Int): String {
        return when (fieldType) {
            AcroFields.FIELD_TYPE_PUSHBUTTON -> "pushButton"
            AcroFields.FIELD_TYPE_CHECKBOX -> "checkbox"
            AcroFields.FIELD_TYPE_RADIOBUTTON -> "radio"
            AcroFields.FIELD_TYPE_TEXT -> "text"
            AcroFields.FIELD_TYPE_LIST -> "list"
            AcroFields.FIELD_TYPE_COMBO -> "combo"
            AcroFields.FIELD_TYPE_SIGNATURE -> "signature"
            else -> "unknown"
        }
    }

    private suspend fun addAnnotationsToPdf(
        context: Context,
        pdfPath: String,
        annotations: List<Map<String, Any>>
    ): String = withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "annotated_pdfs").apply {
            if (!exists()) mkdirs()
        }

        // Create output file
        val inputFileName = File(pdfPath).nameWithoutExtension
        val outputFile = File(outputDir, "${inputFileName}_annotated.pdf")

        // Load PDF and create stamper
        val pdfUri = utils.getURI(pdfPath)
        val inputStream = context.contentResolver.openInputStream(pdfUri)
            ?: throw IOException("Cannot open PDF file")

        val reader = PdfReader(inputStream)
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        // Add annotations
        for (annotationData in annotations) {
            try {
                val annotation = createAnnotationFromData(stamper, annotationData)
                if (annotation != null) {
                    val pageNum = (annotationData["pageNumber"] as? Number)?.toInt() ?: 1
                    stamper.addAnnotation(annotation, pageNum)
                }
            } catch (e: Exception) {
                Log.w(LOG_TAG, "Failed to add annotation: $annotationData", e)
            }
        }

        // Close resources
        stamper.close()
        reader.close()
        inputStream.close()

        return@withContext outputFile.absolutePath
    }

    private fun createAnnotationFromData(stamper: PdfStamper, data: Map<String, Any>): PdfAnnotation? {
        val type = data["type"] as? String ?: return null
        val rectData = data["rect"] as? List<*> ?: return null
        if (rectData.size < 4) return null

        val rect = Rectangle(
            (rectData[0] as Number).toFloat(),
            (rectData[1] as Number).toFloat(),
            (rectData[2] as Number).toFloat(),
            (rectData[3] as Number).toFloat()
        )

        val title = data["title"] as? String
        val contents = data["contents"] as? String

        return when (type) {
            "text" -> {
                val iconName = data["iconName"] as? String ?: "Note"
                val isOpen = data["isOpen"] as? Boolean ?: false
                PdfAnnotation.createText(
                    stamper.writer,
                    rect,
                    title ?: "",
                    contents ?: "",
                    isOpen,
                    iconName
                )
            }
            "highlight" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.MARKUP_HIGHLIGHT)
            "underline" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.MARKUP_UNDERLINE)
            "strikeThrough" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.MARKUP_STRIKEOUT)
            "squiggly" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.MARKUP_SQUIGGLY)
            "link" -> {
                val url = data["url"] as? String ?: return null
                PdfAnnotation.createLink(
                    stamper.writer,
                    rect,
                    PdfAnnotation.HIGHLIGHT_INVERT,
                    url
                )
            }
            "ink" -> createInkAnnotation(stamper, data, rect)
            else -> null
        }
    }

    private fun createMarkupAnnotation(stamper: PdfStamper, data: Map<String, Any>, rect: Rectangle, subtype: Int): PdfAnnotation? {
        val quadsData = data["quads"] as? List<*> ?: return null
        val contents = data["contents"] as? String ?: ""

        val quads = mutableListOf<Float>()
        for (quadData in quadsData) {
            if (quadData is List<*> && quadData.size >= 8) {
                for (i in 0..7) {
                    quads.add((quadData[i] as Number).toFloat())
                }
            }
        }
        if (quads.isEmpty()) return null

        return PdfAnnotation.createMarkup(
            stamper.writer,
            rect,
            contents,
            subtype,
            quads.toFloatArray()
        )
    }

    private fun createInkAnnotation(stamper: PdfStamper, data: Map<String, Any>, rect: Rectangle): PdfAnnotation? {
        val inkListData = data["inkList"] as? List<*> ?: return null
        val contents = data["contents"] as? String ?: ""

        val inkList = inkListData.mapNotNull { strokeData ->
            if (strokeData is List<*>) {
                val stroke = mutableListOf<Float>()
                for (point in strokeData) {
                    if (point is Number) {
                        stroke.add(point.toFloat())
                    }
                }
                stroke.toFloatArray()
            } else {
                null
            }
        }.toTypedArray()
        if (inkList.isEmpty()) return null

        return PdfAnnotation.createInk(
            stamper.writer,
            rect,
            contents,
            inkList
        )
    }

    private fun calculateAverageConfidence(visionText: com.google.mlkit.vision.text.Text): Double {
        val textBlocks = visionText.textBlocks
        if (textBlocks.isEmpty()) return 0.0

        var totalConfidence = 0.0
        var totalElements = 0

        for (block in textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    totalConfidence += element.confidence
                    totalElements++
                }
            }
        }

        return if (totalElements > 0) totalConfidence / totalElements else 0.0
    }

    private suspend fun addDigitalSignature(
        context: Context,
        pdfPath: String,
        certificatePath: String,
        certificatePassword: String,
        reason: String?,
        location: String?,
        contact: String?,
        appearance: Map<String, Any>?
    ): String = withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "signed_pdfs").apply {
            if (!exists()) mkdirs()
        }

        // Load certificate and private key
        val keyStore = KeyStore.getInstance("PKCS12")
        val certUri = utils.getURI(certificatePath)
        context.contentResolver.openInputStream(certUri)?.use { certStream ->
            keyStore.load(certStream, certificatePassword.toCharArray())
        } ?: throw IOException("Cannot open certificate file")

        val alias = keyStore.aliases().nextElement()
        val privateKey = keyStore.getKey(alias, certificatePassword.toCharArray()) as PrivateKey
        val chain = keyStore.getCertificateChain(alias).map { it as Certificate }

        // Create output file
        val inputFileName = File(pdfPath).nameWithoutExtension
        val outputFile = File(outputDir, "${inputFileName}_signed.pdf")

        // Load PDF and create stamper
        val pdfUri = utils.getURI(pdfPath)
        val inputStream = context.contentResolver.openInputStream(pdfUri)
            ?: throw IOException("Cannot open PDF file")

        val reader = PdfReader(inputStream)
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        // Create signature appearance
        val signatureAppearance = stamper.signatureAppearance

        // Configure signature appearance
        if (reason != null) signatureAppearance.reason = reason
        if (location != null) signatureAppearance.location = location
        if (contact != null) signatureAppearance.contact = contact

        // Set signature position and size
        if (appearance != null) {
            val pageNum = (appearance["pageNumber"] as? Int) ?: 1
            val x = (appearance["x"] as? Number)?.toFloat() ?: 36f
            val y = (appearance["y"] as? Number)?.toFloat() ?: 36f
            val width = (appearance["width"] as? Number)?.toFloat() ?: 200f
            val height = (appearance["height"] as? Number)?.toFloat() ?: 100f

            val rect = Rectangle(x, y, x + width, y + height)
            signatureAppearance.setVisibleSignature(rect, pageNum, "signature")

            val text = appearance["text"] as? String
            if (text != null) {
                signatureAppearance.layer2Text = text
            }

        } else {
            // Default signature position if no appearance specified
            val rect = Rectangle(36f, 36f, 236f, 136f)
            signatureAppearance.setVisibleSignature(rect, 1, "signature")
        }

        // Create signature
        val digest = BouncyCastleDigest()
        val signature = PrivateKeySignature(privateKey, "SHA256", "BC")
        val externalSignature = object : ExternalSignature {
            override fun getHashAlgorithm(): String = "SHA256"
            override fun getEncryptionAlgorithm(): String = privateKey.algorithm
            override fun sign(message: ByteArray): ByteArray = signature.sign(message)
        }

        // Sign the document
        MakeSignature.signDetached(
            signatureAppearance,
            digest,
            externalSignature,
            chain.toTypedArray(),
            null,
            null,
            null,
            0,
            MakeSignature.CryptoStandard.CMS
        )

        // Close resources
        stamper.close()
        reader.close()
        inputStream.close()

        return@withContext outputFile.absolutePath
    }

    // For reading PDF metadata
    fun pdfMetadataReader(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
    ) {
        Log.d(LOG_TAG, "pdfMetadataReader - IN, pdfPath=$pdfPath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val metadataResult = withContext(Dispatchers.IO) {
                    readPDFMetadata(context, pdfPath)
                }

                val metadataPayload = metadataResult.mapNotNull { (key, value) ->
                    value?.let { key to it }
                }.toMap()
                utils.finishSuccessfullyWithMap(metadataPayload, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfMetadataReader_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfMetadataReader_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfMetadataReader - OUT")
    }

    // For writing PDF metadata
    fun pdfMetadataWriter(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        title: String?,
        author: String?,
        subject: String?,
        keywords: String?,
        creator: String?,
        producer: String?,
        creationDate: String?,
        modificationDate: String?,
    ) {
        Log.d(LOG_TAG, "pdfMetadataWriter - IN, pdfPath=$pdfPath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath = withContext(Dispatchers.IO) {
                    writePDFMetadata(
                        pdfPath,
                        title,
                        author,
                        subject,
                        keywords,
                        creator,
                        producer,
                        creationDate,
                        modificationDate,
                        context
                    )
                }

                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfMetadataWriter_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfMetadataWriter_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfMetadataWriter - OUT")
    }

    private fun readPDFMetadata(context: Context, pdfPath: String): Map<String, String?> {
        val localPdf = materializePdf(context, pdfPath, "metadata_read")
        val reader = PdfReader(localPdf.file.absolutePath)

        return try {
            val info = reader.info
            mapOf(
                "title" to info["Title"],
                "author" to info["Author"],
                "subject" to info["Subject"],
                "keywords" to info["Keywords"],
                "creator" to info["Creator"],
                "producer" to info["Producer"],
                "creationDate" to info["CreationDate"]?.let { convertPDFDateToISO(it) },
                "modificationDate" to info["ModDate"]?.let { convertPDFDateToISO(it) },
            )
        } finally {
            reader.close()
            deleteIfTemp(localPdf)
        }
    }

    // Helper function to write PDF metadata
    private fun writePDFMetadata(
        pdfPath: String,
        title: String?,
        author: String?,
        subject: String?,
        keywords: String?,
        creator: String?,
        producer: String?,
        creationDate: String?,
        modificationDate: String?,
        context: Context
    ): String {
        val localPdf = materializePdf(context, pdfPath, "metadata_write")
        val reader = PdfReader(localPdf.file.absolutePath)
        val outputFile = utils.getOutputFile(pdfPath, context, "metadata")

        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        try {
            val info = HashMap<String, String>()
            reader.info.forEach { (key, value) ->
                if (value != null) info[key] = value
            }

            title?.let { info["Title"] = it }
            author?.let { info["Author"] = it }
            subject?.let { info["Subject"] = it }
            keywords?.let { info["Keywords"] = it }
            creator?.let { info["Creator"] = it }
            producer?.let { info["Producer"] = it }
            creationDate?.let { info["CreationDate"] = convertISOToPDFDate(it) }
            modificationDate?.let { info["ModDate"] = convertISOToPDFDate(it) }

            stamper.moreInfo = info
        } finally {
            stamper.close()
            reader.close()
            deleteIfTemp(localPdf)
        }

        return outputFile.absolutePath
    }

    // Helper function to convert PDF date format to ISO 8601
    private fun convertPDFDateToISO(pdfDate: String): String? {
        try {
            // PDF date format: D:YYYYMMDDHHMMSSOHH'MM'
            // Example: D:20231201120000+00'00'
            if (pdfDate.startsWith("D:")) {
                val dateStr = pdfDate.substring(2)
                val year = dateStr.substring(0, 4)
                val month = dateStr.substring(4, 6)
                val day = dateStr.substring(6, 8)
                val hour = if (dateStr.length > 8) dateStr.substring(8, 10) else "00"
                val minute = if (dateStr.length > 10) dateStr.substring(10, 12) else "00"
                val second = if (dateStr.length > 12) dateStr.substring(12, 14) else "00"

                return "$year-$month-${day}T$hour:$minute:${second}Z"
            }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Failed to parse PDF date: $pdfDate", e)
        }
        return null
    }

    // Helper function to convert ISO 8601 to PDF date format
    private fun convertISOToPDFDate(isoDate: String): String {
        try {
            // ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ
            // Convert to PDF format: D:YYYYMMDDHHMMSSZ00'00'
            val dateTime = isoDate.replace("-", "").replace("T", "").replace(":", "").replace("Z", "")
            if (dateTime.length >= 14) {
                val year = dateTime.substring(0, 4)
                val month = dateTime.substring(4, 6)
                val day = dateTime.substring(6, 8)
                val hour = dateTime.substring(8, 10)
                val minute = dateTime.substring(10, 12)
                val second = dateTime.substring(12, 14)

                return "D:$year$month$day$hour$minute$second+00'00'"
            }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Failed to convert ISO date: $isoDate", e)
        }
        return isoDate // Return original if conversion fails
    }

    private fun materializePdf(context: Context, pdfPath: String, prefix: String): LocalPdfFile {
        val uri = utils.getURI(pdfPath)
        if (uri.scheme.isNullOrEmpty() || uri.scheme == "file") {
            val filePath = uri.path ?: pdfPath
            return LocalPdfFile(File(filePath), false)
        }

        val tempFile = File.createTempFile(prefix, ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Cannot open PDF file: $pdfPath")

        return LocalPdfFile(tempFile, true)
    }

    private fun deleteIfTemp(localPdf: LocalPdfFile) {
        if (localPdf.shouldDelete) {
            utils.safeDeleteTempFiles(listOf(localPdf.file))
        }
    }

    fun pdfBookmarkReader(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
    ) {
        Log.d(LOG_TAG, "pdfBookmarkReader - IN, pdfPath=$pdfPath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val bookmarkResult = withContext(Dispatchers.IO) {
                    readPDFBookmarks(context, pdfPath)
                }
                utils.finishSuccessfullyWithMap(bookmarkResult, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfBookmarkReader_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfBookmarkReader_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfBookmarkReader - OUT")
    }

    fun pdfBookmarkWriter(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
        bookmarks: List<Map<String, Any>>,
    ) {
        Log.d(LOG_TAG, "pdfBookmarkWriter - IN, pdfPath=$pdfPath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath = withContext(Dispatchers.IO) {
                    writePDFBookmarks(pdfPath, bookmarks, context)
                }
                utils.finishSuccessfullyWithString(resultPDFPath, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfBookmarkWriter_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfBookmarkWriter_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfBookmarkWriter - OUT")
    }

    fun pdfComparison(
        resultCallback: Result,
        context: Context,
        pdfPath1: String,
        pdfPath2: String,
        compareText: Boolean,
        compareMetadata: Boolean,
        compareStructure: Boolean,
    ) {
        Log.d(LOG_TAG, "pdfComparison - IN, pdfPath1=$pdfPath1, pdfPath2=$pdfPath2")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val comparisonResult = withContext(Dispatchers.IO) {
                    comparePDFs(context, pdfPath1, pdfPath2, compareText, compareMetadata, compareStructure)
                }
                utils.finishSuccessfullyWithMap(comparisonResult, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfComparison_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfComparison_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfComparison - OUT")
    }

    fun pdfRepair(
        resultCallback: Result,
        context: Context,
        pdfPath: String,
    ) {
        Log.d(LOG_TAG, "pdfRepair - IN, pdfPath=$pdfPath")

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val repairResult = withContext(Dispatchers.IO) {
                    attemptPDFRepair(pdfPath, context)
                }
                utils.finishSuccessfullyWithMap(repairResult, resultCallback)
            } catch (e: Exception) {
                utils.finishWithError(
                    "pdfRepair_exception", e.stackTraceToString(), null, resultCallback
                )
            } catch (e: OutOfMemoryError) {
                utils.finishWithError(
                    "pdfRepair_OutOfMemoryError", e.stackTraceToString(), null, resultCallback
                )
            }
        }
        Log.d(LOG_TAG, "pdfRepair - OUT")
    }

    private fun readPDFBookmarks(context: Context, pdfPath: String): Map<String, Any> {
        val localPdf = materializePdf(context, pdfPath, "bookmarks_read")
        val reader = PdfReader(localPdf.file.absolutePath)

        return try {
            mapOf("bookmarks" to processBookmarks(SimpleBookmark.getBookmark(reader)))
        } finally {
            reader.close()
            deleteIfTemp(localPdf)
        }
    }

    private fun writePDFBookmarks(
        pdfPath: String,
        bookmarks: List<Map<String, Any>>,
        context: Context
    ): String {
        val localPdf = materializePdf(context, pdfPath, "bookmarks_write")
        val reader = PdfReader(localPdf.file.absolutePath)
        val outputFile = utils.getOutputFile(pdfPath, context, "bookmarks")
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        try {
            stamper.setOutlines(convertToITextBookmarks(bookmarks))
        } finally {
            stamper.close()
            reader.close()
            deleteIfTemp(localPdf)
        }

        return outputFile.absolutePath
    }

    private fun processBookmarks(bookmarks: List<*>?): List<Map<String, Any>> {
        if (bookmarks == null) return emptyList()

        return bookmarks.mapNotNull { rawBookmark ->
            val bookmark = rawBookmark as? Map<*, *> ?: return@mapNotNull null
            val children = bookmark["Kids"] as? List<*>
            val pageNumber = (bookmark["Page"] as? String)
                ?.substringBefore(" ")
                ?.toIntOrNull()
                ?: 0

            mapOf(
                "title" to (bookmark["Title"] as? String ?: ""),
                "pageNumber" to pageNumber,
                "children" to processBookmarks(children)
            )
        }
    }

    private fun convertToITextBookmarks(bookmarks: List<Map<String, Any>>): List<HashMap<String, Any>> {
        return bookmarks.map { bookmark ->
            val iTextBookmark = HashMap<String, Any>()
            iTextBookmark["Title"] = bookmark["title"] as? String ?: ""

            val pageNumber = (bookmark["pageNumber"] as? Number)?.toInt()
            if (pageNumber != null && pageNumber > 0) {
                iTextBookmark["Action"] = "GoTo"
                iTextBookmark["Page"] = "$pageNumber Fit"
            }

            val children = (bookmark["children"] as? List<*>)
                ?.mapNotNull { child -> toStringAnyMap(child) }
            if (!children.isNullOrEmpty()) {
                iTextBookmark["Kids"] = convertToITextBookmarks(children)
            }

            iTextBookmark
        }
    }

    private fun toStringAnyMap(value: Any?): Map<String, Any>? {
        val source = value as? Map<*, *> ?: return null
        val result = HashMap<String, Any>()
        source.forEach { (key, mapValue) ->
            if (key != null && mapValue != null) {
                result[key.toString()] = mapValue
            }
        }
        return result
    }

    private fun comparePDFs(
        context: Context,
        pdfPath1: String,
        pdfPath2: String,
        compareText: Boolean,
        compareMetadata: Boolean,
        compareStructure: Boolean
    ): Map<String, Any> {
        val localPdf1 = materializePdf(context, pdfPath1, "compare_1")
        val localPdf2 = materializePdf(context, pdfPath2, "compare_2")
        val result = mutableMapOf<String, Any>()
        val summary = mutableListOf<String>()
        var overallSimilarity = 1.0

        try {
            if (compareStructure) {
                val structureComparison = comparePDFStructure(localPdf1.file, localPdf2.file)
                result["structureComparison"] = structureComparison
                val differences = structureComparison["differences"] as List<*>
                if (differences.isNotEmpty()) {
                    summary.add("${differences.size} structural differences found")
                    overallSimilarity *= 0.75
                }
            }

            if (compareMetadata) {
                val metadataComparison = comparePDFMetadata(context, localPdf1.file.absolutePath, localPdf2.file.absolutePath)
                result["metadataComparison"] = metadataComparison
                val differences = metadataComparison["differences"] as List<*>
                if (differences.isNotEmpty()) {
                    summary.add("${differences.size} metadata differences found")
                    overallSimilarity *= 0.9
                }
            }

            if (compareText) {
                val textComparison = comparePDFText(localPdf1.file.absolutePath, localPdf2.file.absolutePath)
                result["textComparison"] = textComparison
                val similarity = textComparison["similarity"] as Double
                val differences = textComparison["differences"] as List<*>
                if (differences.isNotEmpty()) {
                    summary.add("${differences.size} text differences found")
                    summary.add("Text similarity: ${(similarity * 100).toInt()}%")
                    overallSimilarity *= similarity
                }
            }

            if (summary.isEmpty()) summary.add("PDFs are identical")

            result["overallSimilarity"] = overallSimilarity.coerceIn(0.0, 1.0)
            result["summary"] = summary
            return result
        } finally {
            deleteIfTemp(localPdf1)
            deleteIfTemp(localPdf2)
        }
    }

    private fun comparePDFStructure(file1: File, file2: File): Map<String, Any> {
        val reader1 = PdfReader(file1.absolutePath)
        val reader2 = PdfReader(file2.absolutePath)
        val pageCount1 = reader1.numberOfPages
        val pageCount2 = reader2.numberOfPages
        val differences = mutableListOf<String>()

        try {
            if (pageCount1 != pageCount2) {
                differences.add("Page count: $pageCount1 vs $pageCount2")
            }

            if (file1.length() != file2.length()) {
                differences.add("File size: ${file1.length()}B vs ${file2.length()}B")
            }

            val comparablePages = minOf(pageCount1, pageCount2)
            for (pageNumber in 1..comparablePages) {
                val size1 = reader1.getPageSizeWithRotation(pageNumber)
                val size2 = reader2.getPageSizeWithRotation(pageNumber)
                if (size1.width != size2.width || size1.height != size2.height || reader1.getPageRotation(pageNumber) != reader2.getPageRotation(pageNumber)) {
                    differences.add(
                        "Page $pageNumber geometry: ${size1.width}x${size1.height}@${reader1.getPageRotation(pageNumber)} vs ${size2.width}x${size2.height}@${reader2.getPageRotation(pageNumber)}"
                    )
                }
            }
        } finally {
            reader1.close()
            reader2.close()
        }

        return mapOf(
            "pageCount1" to pageCount1,
            "pageCount2" to pageCount2,
            "pageCountEqual" to (pageCount1 == pageCount2),
            "differences" to differences
        )
    }

    private fun comparePDFMetadata(context: Context, pdfPath1: String, pdfPath2: String): Map<String, Any> {
        val metadata1 = readPDFMetadata(context, pdfPath1)
        val metadata2 = readPDFMetadata(context, pdfPath2)
        val differences = mutableListOf<Map<String, Any>>()

        listOf("title", "author", "subject", "keywords", "creator", "producer", "creationDate", "modificationDate").forEach { field ->
            val value1 = metadata1[field]
            val value2 = metadata2[field]
            if (value1 != value2) {
                differences.add(mapOf("field" to field, "value1" to (value1 ?: ""), "value2" to (value2 ?: "")))
            }
        }

        return mapOf("metadata1" to metadata1, "metadata2" to metadata2, "differences" to differences)
    }

    private fun comparePDFText(pdfPath1: String, pdfPath2: String): Map<String, Any> {
        val pageTexts1 = extractPageTexts(pdfPath1)
        val pageTexts2 = extractPageTexts(pdfPath2)
        val text1 = extractFullText(pageTexts1)
        val text2 = extractFullText(pageTexts2)
        val similarity = calculateTextSimilarity(text1, text2)
        val differences = mutableListOf<Map<String, Any>>()
        var offset1 = 0
        var offset2 = 0
        val maxPages = maxOf(pageTexts1.size, pageTexts2.size)

        for (index in 0 until maxPages) {
            val pageText1 = pageTexts1.getOrNull(index).orEmpty()
            val pageText2 = pageTexts2.getOrNull(index).orEmpty()
            if (pageText1 != pageText2) {
                differences.add(createTextDifference(index + 1, offset1, offset2, pageText1, pageText2))
            }
            offset1 += pageText1.length + 1
            offset2 += pageText2.length + 1
        }

        return mapOf("text1" to text1, "text2" to text2, "similarity" to similarity, "differences" to differences)
    }

    private fun extractPageTexts(pdfPath: String): List<String> {
        val reader = PdfReader(pdfPath)
        return try {
            (1..reader.numberOfPages).map { pageNum ->
                PdfTextExtractor.getTextFromPage(reader, pageNum).trim()
            }
        } finally {
            reader.close()
        }
    }

    private fun createTextDifference(
        pageNumber: Int,
        offset1: Int,
        offset2: Int,
        text1: String,
        text2: String
    ): Map<String, Any> {
        var prefix = 0
        val minLength = minOf(text1.length, text2.length)
        while (prefix < minLength && text1[prefix] == text2[prefix]) {
            prefix++
        }

        var suffix = 0
        while (
            suffix < minLength - prefix &&
            text1[text1.length - 1 - suffix] == text2[text2.length - 1 - suffix]
        ) {
            suffix++
        }

        val changed1 = text1.substring(prefix, text1.length - suffix)
        val changed2 = text2.substring(prefix, text2.length - suffix)
        val type = when {
            changed1.isEmpty() -> "added"
            changed2.isEmpty() -> "removed"
            else -> "modified"
        }
        val preview = when (type) {
            "added" -> changed2
            "removed" -> changed1
            else -> "Page $pageNumber changed from '${changed1.take(80)}' to '${changed2.take(80)}'"
        }

        return mapOf(
            "type" to type,
            "position1" to offset1 + prefix,
            "position2" to offset2 + prefix,
            "length" to maxOf(changed1.length, changed2.length),
            "content" to preview.take(240)
        )
    }

    private fun extractFullText(pageTexts: List<String>): String {
        return pageTexts.joinToString("\n")
    }

    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        if (text1 == text2) return 1.0
        if (text1.isEmpty() || text2.isEmpty()) return 0.0

        val words1 = text1.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()
        val words2 = text2.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()
        val union = words1.union(words2).size
        return if (union == 0) 1.0 else words1.intersect(words2).size.toDouble() / union.toDouble()
    }

    private fun attemptPDFRepair(pdfPath: String, context: Context): Map<String, Any> {
        val issues = mutableListOf<String>()
        val recoveredElements = mutableListOf<String>()
        val localPdf = materializePdf(context, pdfPath, "repair")
        var repairedPdfPath = ""
        var canOpen = false
        var hasValidStructure = false
        var hasReadableContent = false
        var pageCount = 0
        var pagesRecovered = 0
        var textContentLength = 0
        var repairMethod = "failed"

        try {
            val reader = PdfReader(localPdf.file.absolutePath)
            canOpen = true
            pageCount = reader.numberOfPages
            val outputFile = utils.getOutputFile(pdfPath, context, "repaired")
            val document = Document()
            val copy = PdfCopy(document, FileOutputStream(outputFile))

            try {
                document.open()
                reader.consolidateNamedDestinations()

                for (pageNumber in 1 downTo pageCount) {
                    try {
                        copy.addPage(copy.getImportedPage(reader, pageNumber))
                        pagesRecovered++
                        recoveredElements.add("Page $pageNumber")

                        try {
                            textContentLength += PdfTextExtractor.getTextFromPage(reader, pageNumber).length
                            hasReadableContent = true
                        } catch (textError: Exception) {
                            issues.add("Page $pageNumber text extraction failed: ${textError.message}")
                        }
                    } catch (pageError: Exception) {
                        issues.add("Page $pageNumber could not be recovered: ${pageError.message}")
                    }
                }

                hasValidStructure = pagesRecovered > 0
                if (pagesRecovered > 0) {
                    repairedPdfPath = outputFile.absolutePath
                    repairMethod = if (pagesRecovered == pageCount) "page_rebuild" else "partial_page_rebuild"
                } else {
                    issues.add("No readable pages could be copied into a repaired document")
                    outputFile.delete()
                }
            } finally {
                document.close()
                copy.close()
                reader.close()
            }
        } catch (e: Exception) {
            issues.add("Cannot repair PDF: ${e.message}")
        } finally {
            deleteIfTemp(localPdf)
        }

        val wasRepaired = repairedPdfPath.isNotEmpty()
        val corruptionLevel = when {
            !canOpen -> 1.0
            pageCount == 0 -> 1.0
            pagesRecovered == pageCount -> 0.0
            pagesRecovered > 0 -> 1.0 - (pagesRecovered.toDouble() / pageCount.toDouble())
            else -> 1.0
        }

        return mapOf(
            "wasRepaired" to wasRepaired,
            "repairedPdfPath" to repairedPdfPath,
            "originalStatus" to mapOf(
                "canOpen" to canOpen,
                "hasValidStructure" to hasValidStructure,
                "hasReadableContent" to hasReadableContent,
                "corruptionLevel" to corruptionLevel,
                "detectedIssues" to issues
            ),
            "repairStatus" to mapOf(
                "completed" to wasRepaired,
                "contentRecovered" to (pagesRecovered > 0),
                "fullyFunctional" to (pageCount > 0 && pagesRecovered == pageCount),
                "repairMethod" to repairMethod,
                "repairInfo" to if (wasRepaired) {
                    listOf("Recovered $pagesRecovered of $pageCount pages")
                } else {
                    issues
                }
            ),
            "issues" to issues,
            "recoveredContent" to mapOf(
                "pagesRecovered" to pagesRecovered,
                "textContentLength" to textContentLength,
                "imagesRecovered" to 0,
                "metadataPreserved" to false,
                "recoveredElements" to recoveredElements
            )
        )
    }
}
