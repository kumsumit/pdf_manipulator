package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
import com.itextpdf.text.pdf.PdfAnnotation
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfSignatureAppearance
import com.itextpdf.text.pdf.PdfWriter
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
    ) {
        Log.d(
            LOG_TAG, "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? = getCompressedPDFPath(
                    sourceFilePath!!, imageQuality!!, imageScale!!, unEmbedFonts!!, activity
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
}
