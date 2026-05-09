package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Context
import android.net.Uri
import android.graphics.pdf.PdfRenderer
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.Document
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfAnnotation
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfSignatureAppearance
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.SimpleBookmark
import com.itextpdf.text.pdf.security.BouncyCastleDigest
import com.itextpdf.text.pdf.security.ExternalDigest
import com.itextpdf.text.pdf.security.ExternalSignature
import com.itextpdf.text.pdf.security.MakeSignature
import com.itextpdf.text.pdf.security.PrivateKeySignature
import com.itextpdf.text.pdf.parser.ImageRenderInfo
import com.itextpdf.text.pdf.parser.PdfReaderContentParser
import com.itextpdf.text.pdf.parser.RenderListener
import com.itextpdf.text.pdf.parser.TextRenderInfo
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.HashMap


private const val LOG_TAG = "PdfManipulator"

class PdfManipulator(
    private val activity: Activity
) {

    private var job: Job? = null

    private val utils = Utils()

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
        sourceFilePath: String?,
        imageQuality: Int?,
        imageScale: Double?,
        unEmbedFonts: Boolean?,
        advancedOptions: Map<String, Any>?,
    ) {
        Log.d(
            LOG_TAG, "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? = getCompressedPDFPath(
                    sourceFilePath!!, imageQuality!!, imageScale!!, unEmbedFonts!!, advancedOptions, activity
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

    // For compressing pdf.
    fun watermarkPdf(
        resultCallback: Result,
        sourceFilePath: String?,
        text: String?,
        fontSize: Double?,
        watermarkLayer: WatermarkLayer?,
        opacity: Double?,
        rotationAngle: Double?,
        watermarkColor: String?,
        positionType: PositionType?,
        customPositionXCoordinatesList: List<Double>?,
        customPositionYCoordinatesList: List<Double>?,
    ) {
        Log.d(
            LOG_TAG, "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? = getWatermarkedPDFPath(
                    sourceFilePath!!,
                    text!!,
                    fontSize!!,
                    watermarkLayer!!,
                    opacity!!,
                    rotationAngle!!,
                    watermarkColor!!,
                    positionType!!,
                    customPositionXCoordinatesList ?: listOf(),
                    customPositionYCoordinatesList ?: listOf(),
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

    fun cancelManipulations(
    ) {
        job?.cancel()
        Log.d(LOG_TAG, "Canceled Manipulations")
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
                val pdfUri = Uri.parse(pdfPath)
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
            val pdfUri = Uri.parse(pdfPath)
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
                if (pageIndex < 0 || pageIndex >= pageCount) continue

                val page = pdfRenderer.openPage(pageIndex)

                // Calculate scaled dimensions
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()

                // Create bitmap with scaled dimensions
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

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
            val pdfUri = Uri.parse(pdfPath)
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
        val uri = Uri.parse(pdfPath)
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
        val pdfUri = Uri.parse(pdfPath)
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
            "highlight" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.HIGHLIGHT)
            "underline" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.UNDERLINE)
            "strikeThrough" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.STRIKEOUT)
            "squiggly" -> createMarkupAnnotation(stamper, data, rect, PdfAnnotation.SQUIGGLY)
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

    private fun createMarkupAnnotation(stamper: PdfStamper, data: Map<String, Any>, rect: Rectangle, subtype: PdfName): PdfAnnotation? {
        val quadsData = data["quads"] as? List<*> ?: return null
        val title = data["title"] as? String ?: ""
        val contents = data["contents"] as? String ?: ""

        val quads = PdfArray()
        for (quadData in quadsData) {
            if (quadData is List<*> && quadData.size >= 8) {
                for (i in 0..7) {
                    quads.add(PdfName((quadData[i] as Number).toFloat()))
                }
            }
        }

        return PdfAnnotation.createMarkup(
            stamper.writer,
            rect,
            title,
            contents,
            subtype,
            quads
        )
    }

    private fun createInkAnnotation(stamper: PdfStamper, data: Map<String, Any>, rect: Rectangle): PdfAnnotation? {
        val inkListData = data["inkList"] as? List<*> ?: return null
        val title = data["title"] as? String ?: ""
        val contents = data["contents"] as? String ?: ""

        val inkList = PdfArray()
        for (strokeData in inkListData) {
            if (strokeData is List<*>) {
                val stroke = PdfArray()
                for (point in strokeData) {
                    if (point is Number) {
                        stroke.add(PdfName(point.toFloat()))
                    }
                }
                inkList.add(stroke)
            }
        }

        return PdfAnnotation.createInk(
            stamper.writer,
            rect,
            title,
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
        val certUri = Uri.parse(certificatePath)
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
        val pdfUri = Uri.parse(pdfPath)
        val inputStream = context.contentResolver.openInputStream(pdfUri)
            ?: throw IOException("Cannot open PDF file")

        val reader = PdfReader(inputStream)
        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        // Create signature appearance
        val signatureAppearance = stamper.getSignatureAppearance()

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

            val fontSize = (appearance["fontSize"] as? Number)?.toFloat() ?: 12f
            signatureAppearance.layer2FontSize = fontSize
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
                    readPDFMetadata(pdfPath)
                }

                utils.finishSuccessfullyWithMap(metadataResult, resultCallback)
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

    // Helper function to read PDF metadata
    private fun readPDFMetadata(pdfPath: String): Map<String, String?> {
        val reader = PdfReader(pdfPath)
        val info = reader.info

        // Convert date strings to ISO 8601 format if present
        val creationDate = info.get("CreationDate")
        val modificationDate = info.get("ModDate")

        val result = mutableMapOf<String, String?>()
        result["title"] = info.get("Title")
        result["author"] = info.get("Author")
        result["subject"] = info.get("Subject")
        result["keywords"] = info.get("Keywords")
        result["creator"] = info.get("Creator")
        result["producer"] = info.get("Producer")
        result["creationDate"] = creationDate?.let { convertPDFDateToISO(it) }
        result["modificationDate"] = modificationDate?.let { convertPDFDateToISO(it) }

        reader.close()
        return result
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
        val reader = PdfReader(pdfPath)
        val outputFile = utils.getOutputFile(pdfPath, context, "metadata")

        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        // Create new info dictionary
        val info = HashMap<String, String>()

        // Copy existing metadata
        reader.info.forEach { (key, value) ->
            info[key] = value
        }

        // Update with new values
        title?.let { info["Title"] = it }
        author?.let { info["Author"] = it }
        subject?.let { info["Subject"] = it }
        keywords?.let { info["Keywords"] = it }
        creator?.let { info["Creator"] = it }
        producer?.let { info["Producer"] = it }

        // Handle dates
        creationDate?.let { info["CreationDate"] = convertISOToPDFDate(it) }
        modificationDate?.let { info["ModDate"] = convertISOToPDFDate(it) }

        // Set the updated metadata
        stamper.setMoreInfo(info)

        stamper.close()
        reader.close()

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

    // Helper function to read PDF bookmarks
    private fun readPDFBookmarks(pdfPath: String): Map<String, Any> {
        val reader = PdfReader(pdfPath)
        val bookmarks = SimpleBookmark.getBookmark(reader)

        val processedBookmarks = processBookmarks(bookmarks)

        reader.close()

        return mapOf("bookmarks" to processedBookmarks)
    }

    // Helper function to write PDF bookmarks
    private fun writePDFBookmarks(
        pdfPath: String,
        bookmarks: List<Map<String, Any>>,
        context: Context
    ): String {
        val reader = PdfReader(pdfPath)
        val outputFile = utils.getOutputFile(pdfPath, context, "bookmarks")

        val stamper = PdfStamper(reader, FileOutputStream(outputFile))

        // Convert bookmark list to iText format
        val bookmarkList = convertToITextBookmarks(bookmarks)

        // Set the bookmarks
        stamper.setOutlines(bookmarkList)

        stamper.close()
        reader.close()

        return outputFile.absolutePath
    }

    // Helper function to process bookmarks from iText format to our format
    private fun processBookmarks(bookmarks: List<Map<String, Any>>?): List<Map<String, Any>> {
        if (bookmarks == null) return emptyList()

        return bookmarks.map { bookmark ->
            val processedBookmark = mutableMapOf<String, Any>()

            // Extract title
            processedBookmark["title"] = bookmark["Title"] as? String ?: ""

            // Extract page information
            val action = bookmark["Action"] as? Map<*, *>
            if (action != null) {
                val type = action["S"] as? PdfName
                if (type?.toString() == "/GoTo") {
                    val destination = action["D"] as? List<*>
                    if (destination != null && destination.isNotEmpty()) {
                        // Try to extract page number
                        when (val pageRef = destination[0]) {
                            is PdfArray -> {
                                // Handle page reference
                                processedBookmark["page"] = pageRef.toString()
                            }
                            is Int -> {
                                processedBookmark["pageNumber"] = pageRef
                            }
                            is String -> {
                                if (pageRef.startsWith("#page=")) {
                                    val pageNum = pageRef.substring(6).toIntOrNull()
                                    if (pageNum != null) {
                                        processedBookmark["pageNumber"] = pageNum
    }

    // For comparing two PDFs
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
                    comparePDFs(pdfPath1, pdfPath2, compareText, compareMetadata, compareStructure)
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

    // Helper function to compare two PDFs
    private fun comparePDFs(
        pdfPath1: String,
        pdfPath2: String,
        compareText: Boolean,
        compareMetadata: Boolean,
        compareStructure: Boolean
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val summary = mutableListOf<String>()
        var overallSimilarity = 1.0

        // Structure comparison
        var structureComparison: Map<String, Any>? = null
        if (compareStructure) {
            structureComparison = comparePDFStructure(pdfPath1, pdfPath2)
            result["structureComparison"] = structureComparison
            val pageCountEqual = structureComparison["pageCountEqual"] as Boolean
            if (!pageCountEqual) {
                summary.add("Page counts differ")
                overallSimilarity *= 0.7
            }
        }

        // Metadata comparison
        var metadataComparison: Map<String, Any>? = null
        if (compareMetadata) {
            metadataComparison = comparePDFMetadata(pdfPath1, pdfPath2)
            result["metadataComparison"] = metadataComparison
            val differences = metadataComparison["differences"] as List<Map<String, Any>>
            if (differences.isNotEmpty()) {
                summary.add("${differences.size} metadata differences found")
                overallSimilarity *= 0.9
            }
        }

        // Text comparison
        var textComparison: Map<String, Any>? = null
        if (compareText) {
            textComparison = comparePDFText(pdfPath1, pdfPath2)
            result["textComparison"] = textComparison
            val similarity = textComparison["similarity"] as Double
            val differences = textComparison["differences"] as List<Map<String, Any>>

            if (similarity < 1.0) {
                summary.add("Text similarity: ${(similarity * 100).toInt()}%")
                overallSimilarity *= similarity
            }

            if (differences.isNotEmpty()) {
                summary.add("${differences.size} text differences found")
            }
        }

        if (summary.isEmpty()) {
            summary.add("PDFs are identical")
        }

        result["overallSimilarity"] = overallSimilarity
        result["summary"] = summary

        return result
    }

    // Helper function to compare PDF structure
    private fun comparePDFStructure(pdfPath1: String, pdfPath2: String): Map<String, Any> {
        val reader1 = PdfReader(pdfPath1)
        val reader2 = PdfReader(pdfPath2)

        val pageCount1 = reader1.numberOfPages
        val pageCount2 = reader2.numberOfPages
        val pageCountEqual = pageCount1 == pageCount2

        val differences = mutableListOf<String>()

        if (!pageCountEqual) {
            differences.add("Page count: $pageCount1 vs $pageCount2")
        }

        // Compare file sizes
        val file1 = File(pdfPath1)
        val file2 = File(pdfPath2)
        val size1 = file1.length()
        val size2 = file2.length()

        if (size1 != size2) {
            differences.add("File size: ${size1}B vs ${size2}B")
        }

        reader1.close()
        reader2.close()

        return mapOf(
            "pageCount1" to pageCount1,
            "pageCount2" to pageCount2,
            "pageCountEqual" to pageCountEqual,
            "differences" to differences
        )
    }

    // Helper function to compare PDF metadata
    private fun comparePDFMetadata(pdfPath1: String, pdfPath2: String): Map<String, Any> {
        val metadata1 = readPDFMetadata(pdfPath1)
        val metadata2 = readPDFMetadata(pdfPath2)

        val differences = mutableListOf<Map<String, Any>>()

        val fields = listOf("title", "author", "subject", "keywords", "creator", "producer", "creationDate", "modificationDate")

        for (field in fields) {
            val value1 = metadata1[field] as? String
            val value2 = metadata2[field] as? String

            if (value1 != value2) {
                differences.add(mapOf(
                    "field" to field,
                    "value1" to (value1 ?: ""),
                    "value2" to (value2 ?: "")
                ))
            }
        }

        return mapOf(
            "metadata1" to metadata1,
            "metadata2" to metadata2,
            "differences" to differences
        )
    }

    // Helper function to compare PDF text content
    private fun comparePDFText(pdfPath1: String, pdfPath2: String): Map<String, Any> {
        val text1 = extractFullText(pdfPath1)
        val text2 = extractFullText(pdfPath2)

        val differences = mutableListOf<Map<String, Any>>()

        // Simple text similarity calculation
        val similarity = calculateTextSimilarity(text1, text2)

        // Find basic differences (this is a simplified implementation)
        if (text1 != text2) {
            val maxLength = maxOf(text1.length, text2.length)
            val minLength = minOf(text1.length, text2.length)

            differences.add(mapOf(
                "type" to "modified",
                "position1" to 0,
                "position2" to 0,
                "length" to maxLength,
                "content" to "Text content differs"
            ))
        }

        return mapOf(
            "text1" to text1,
            "text2" to text2,
            "similarity" to similarity,
            "differences" to differences
        )
    }

    // Helper function to extract full text from PDF
    private fun extractFullText(pdfPath: String): String {
        val reader = PdfReader(pdfPath)
        val text = StringBuilder()

        for (pageNum in 1..reader.numberOfPages) {
            val pageText = PdfTextExtractor.getTextFromPage(reader, pageNum)
            text.append(pageText).append("\n")
        }

        reader.close()
        return text.toString()
    }

    // Helper function to calculate text similarity (simple implementation)
    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        if (text1 == text2) return 1.0
        if (text1.isEmpty() && text2.isEmpty()) return 1.0
        if (text1.isEmpty() || text2.isEmpty()) return 0.0

        // Simple similarity based on common words
        val words1 = text1.lowercase().split(Regex("\\s+")).toSet()
        val words2 = text2.lowercase().split(Regex("\\s+")).toSet()

        val intersection = words1.intersect(words2).size.toDouble()
        val union = words1.union(words2).size.toDouble()

        return if (union > 0) intersection / union else 0.0
    }
    }

    // For repairing corrupted PDFs
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

    // Helper function to attempt PDF repair
    private fun attemptPDFRepair(pdfPath: String, context: Context): Map<String, Any> {
        val issues = mutableListOf<String>()
        val recoveredElements = mutableListOf<String>()

        // Analyze original PDF corruption status
        val corruptionStatus = analyzePDFCorruption(pdfPath)
        issues.addAll(corruptionStatus.detectedIssues)

        var repairedPdfPath: String? = null
        var repairStatus: Map<String, Any>
        var recoveredContent: Map<String, Any>? = null

        if (corruptionStatus.canOpen && corruptionStatus.corruptionLevel < 0.8) {
            // Try gentle repair - just copy and validate
            try {
                val outputFile = utils.getOutputFile(pdfPath, context, "repaired")
                File(pdfPath).copyTo(outputFile, overwrite = true)

                // Validate the copy
                val reader = PdfReader(outputFile.absolutePath)
                reader.close()

                repairedPdfPath = outputFile.absolutePath
                repairStatus = mapOf(
                    "completed" to true,
                    "contentRecovered" to true,
                    "fullyFunctional" to (corruptionStatus.corruptionLevel < 0.1),
                    "repairMethod" to "validation_copy",
                    "repairInfo" to listOf("PDF structure validated and copied")
                )
                recoveredContent = mapOf(
                    "pagesRecovered" to corruptionStatus.pageCount,
                    "textContentLength" to 0, // Would need full extraction
                    "imagesRecovered" to 0,
                    "metadataPreserved" to true,
                    "recoveredElements" to recoveredElements
                )
            } catch (e: Exception) {
                issues.add("Copy validation failed: ${e.message}")
                repairStatus = mapOf(
                    "completed" to false,
                    "contentRecovered" to false,
                    "fullyFunctional" to false,
                    "repairMethod" to "failed_copy",
                    "repairInfo" to listOf("Unable to create valid copy: ${e.message}")
                )
            }
        } else if (corruptionStatus.canOpen && corruptionStatus.hasReadableContent) {
            // Try content extraction and reconstruction
            try {
                val reconstructionResult = reconstructPDF(pdfPath, context)
                if (reconstructionResult != null) {
                    repairedPdfPath = reconstructionResult.first
                    recoveredContent = reconstructionResult.second
                    recoveredElements.addAll(recoveredContent["recoveredElements"] as List<String>)

                    repairStatus = mapOf(
                        "completed" to true,
                        "contentRecovered" to true,
                        "fullyFunctional" to (recoveredContent["pagesRecovered"] as Int > 0),
                        "repairMethod" to "content_reconstruction",
                        "repairInfo" to listOf("Content extracted and reconstructed into new PDF")
                    )
                } else {
                    repairStatus = mapOf(
                        "completed" to false,
                        "contentRecovered" to false,
                        "fullyFunctional" to false,
                        "repairMethod" to "reconstruction_failed",
                        "repairInfo" to listOf("Content reconstruction failed")
                    )
                }
            } catch (e: Exception) {
                issues.add("Reconstruction failed: ${e.message}")
                repairStatus = mapOf(
                    "completed" to false,
                    "contentRecovered" to false,
                    "fullyFunctional" to false,
                    "repairMethod" to "reconstruction_error",
                    "repairInfo" to listOf("Reconstruction error: ${e.message}")
                )
            }
        } else {
            // PDF is too corrupted for repair
            repairStatus = mapOf(
                "completed" to false,
                "contentRecovered" to false,
                "fullyFunctional" to false,
                "repairMethod" to "unrepairable",
                "repairInfo" to listOf("PDF corruption level too high for repair")
            )
            issues.add("PDF is too severely corrupted for repair")
        }

        return mapOf(
            "wasRepaired" to (repairedPdfPath != null),
            "repairedPdfPath" to repairedPdfPath,
            "originalStatus" to corruptionStatus.toMap(),
            "repairStatus" to repairStatus,
            "issues" to issues,
            "recoveredContent" to recoveredContent
        )
    }

    // Helper function to analyze PDF corruption
    private fun analyzePDFCorruption(pdfPath: String): PDFCorruptionAnalysis {
        val detectedIssues = mutableListOf<String>()
        var canOpen = false
        var hasValidStructure = false
        var hasReadableContent = false
        var pageCount = 0
        var corruptionLevel = 0.0

        try {
            val reader = PdfReader(pdfPath)
            canOpen = true
            pageCount = reader.numberOfPages

            // Check basic structure
            try {
                // Try to access pages
                for (i in 1..minOf(pageCount, 5)) { // Check first 5 pages
                    val page = reader.getPageN(i)
                    if (page != null) {
                        hasValidStructure = true
                        hasReadableContent = true
                        break
                    }
                }
            } catch (e: Exception) {
                detectedIssues.add("Page access error: ${e.message}")
                corruptionLevel += 0.3
            }

            // Check if we can extract text from at least one page
            if (hasValidStructure) {
                try {
                    val text = PdfTextExtractor.getTextFromPage(reader, 1)
                    if (text.isNotEmpty()) {
                        hasReadableContent = true
                    } else {
                        detectedIssues.add("No readable text content found")
                        corruptionLevel += 0.2
                    }
                } catch (e: Exception) {
                    detectedIssues.add("Text extraction error: ${e.message}")
                    corruptionLevel += 0.3
                }
            }

            // Check file size vs expected
            val file = File(pdfPath)
            val fileSizeKB = file.length() / 1024.0
            if (fileSizeKB < 1.0) {
                detectedIssues.add("Unusually small file size: ${fileSizeKB}KB")
                corruptionLevel += 0.2
            }

            reader.close()

        } catch (e: Exception) {
            detectedIssues.add("Cannot open PDF: ${e.message}")
            corruptionLevel = 1.0
        }

        if (!canOpen) corruptionLevel = 1.0
        else if (!hasValidStructure) corruptionLevel = minOf(corruptionLevel + 0.5, 1.0)
        else if (!hasReadableContent) corruptionLevel = minOf(corruptionLevel + 0.3, 1.0)

        return PDFCorruptionAnalysis(
            canOpen = canOpen,
            hasValidStructure = hasValidStructure,
            hasReadableContent = hasReadableContent,
            corruptionLevel = corruptionLevel,
            detectedIssues = detectedIssues,
            pageCount = pageCount
        )
    }

    // Helper function to reconstruct PDF from corrupted file
    private fun reconstructPDF(pdfPath: String, context: Context): Pair<String, Map<String, Any>>? {
        return try {
            val outputFile = utils.getOutputFile(pdfPath, context, "reconstructed")
            val document = Document()
            val writer = PdfWriter.getInstance(document, FileOutputStream(outputFile))

            document.open()

            // Try to extract and reconstruct content
            val reader = PdfReader(pdfPath)
            val recoveredElements = mutableListOf<String>()
            var pagesRecovered = 0
            var textContentLength = 0
            var imagesRecovered = 0

            // Copy readable pages
            for (pageNum in 1..reader.numberOfPages) {
                try {
                    val page = reader.getPageN(pageNum)
                    if (page != null) {
                        document.newPage()
                        writer.getImportedPage(reader, pageNum)
                        pagesRecovered++
                        recoveredElements.add("Page $pageNum")

                        // Try to extract text from this page
                        try {
                            val text = PdfTextExtractor.getTextFromPage(reader, pageNum)
                            textContentLength += text.length
                        } catch (e: Exception) {
                            // Text extraction failed for this page
                        }
                    }
                } catch (e: Exception) {
                    // Skip corrupted page
                    recoveredElements.add("Page $pageNum (corrupted, skipped)")
                }
            }

            reader.close()
            document.close()

            val recoveredContent = mapOf(
                "pagesRecovered" to pagesRecovered,
                "textContentLength" to textContentLength,
                "imagesRecovered" to imagesRecovered,
                "metadataPreserved" to false, // We don't preserve metadata in reconstruction
                "recoveredElements" to recoveredElements
            )

            Pair(outputFile.absolutePath, recoveredContent)
        } catch (e: Exception) {
            null
        }
    }

    // Data class for PDF corruption analysis
    private data class PDFCorruptionAnalysis(
        val canOpen: Boolean,
        val hasValidStructure: Boolean,
        val hasReadableContent: Boolean,
        val corruptionLevel: Double,
        val detectedIssues: List<String>,
        val pageCount: Int
    ) {
        fun toMap(): Map<String, Any> {
            return mapOf(
                "canOpen" to canOpen,
                "hasValidStructure" to hasValidStructure,
                "hasReadableContent" to hasReadableContent,
                "corruptionLevel" to corruptionLevel,
                "detectedIssues" to detectedIssues
            )
        }
    }
}
                        }

                        // Extract coordinates if available
                        if (destination.size > 3) {
                            processedBookmark["x"] = (destination[2] as? Number)?.toDouble() ?: 0.0
                            processedBookmark["y"] = (destination[3] as? Number)?.toDouble() ?: 0.0
                        }

                        // Extract zoom if available
                        if (destination.size > 4) {
                            processedBookmark["zoom"] = (destination[4] as? Number)?.toDouble() ?: 0.0
                        }
                    }
                }
            }

            // Process children recursively
            val kids = bookmark["Kids"] as? List<Map<String, Any>>
            if (kids != null) {
                processedBookmark["children"] = processBookmarks(kids)
            } else {
                processedBookmark["children"] = emptyList<Map<String, Any>>()
            }

            processedBookmark
        }
    }

    // Helper function to convert our bookmark format to iText format
    private fun convertToITextBookmarks(bookmarks: List<Map<String, Any>>): List<Map<String, Any>> {
        return bookmarks.map { bookmark ->
            val iTextBookmark = mutableMapOf<String, Any>()

            // Set title
            iTextBookmark["Title"] = bookmark["title"] as? String ?: ""

            // Create action for page navigation
            val pageNumber = bookmark["pageNumber"] as? Int
            if (pageNumber != null) {
                val action = mutableMapOf<String, Any>()
                action["S"] = PdfName("GoTo")

                // Create destination array
                val destination = mutableListOf<Any>()
                destination.add(pageNumber) // Page number

                // Add coordinates if provided
                val x = bookmark["x"] as? Double ?: 0.0
                val y = bookmark["y"] as? Double ?: 0.0
                val zoom = bookmark["zoom"] as? Double ?: 0.0

                destination.add(PdfName("XYZ"))
                destination.add(x)
                destination.add(y)
                destination.add(zoom)

                action["D"] = destination
                iTextBookmark["Action"] = action
            }

            // Process children recursively
            val children = bookmark["children"] as? List<Map<String, Any>>
            if (children != null && children.isNotEmpty()) {
                iTextBookmark["Kids"] = convertToITextBookmarks(children)
            }

            iTextBookmark
        }
    }
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

    // For writing PDF bookmarks
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
}
