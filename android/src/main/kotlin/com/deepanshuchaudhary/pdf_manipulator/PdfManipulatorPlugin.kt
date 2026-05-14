package com.deepanshuchaudhary.pdf_manipulator

import android.content.Context
import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** PdfManipulatorPlugin */
class PdfManipulatorPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var pdfManipulator: PdfManipulator? = null
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var activityBinding: ActivityPluginBinding? = null

    companion object {
        const val LOG_TAG = "PdfManipulatorPlugin"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(LOG_TAG, "onAttachedToEngine - IN")
        context = flutterPluginBinding.applicationContext
        if (pluginBinding != null) {
            Log.w(LOG_TAG, "onAttachedToEngine - already attached")
        }

        pluginBinding = flutterPluginBinding

        val messenger = pluginBinding?.binaryMessenger
        doOnAttachedToEngine(messenger!!)

        Log.d(LOG_TAG, "onAttachedToEngine - OUT")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(LOG_TAG, "onDetachedFromEngine")
        doOnDetachedFromEngine()
    }

    // note: this may be called multiple times on app startup
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.d(LOG_TAG, "onAttachedToActivity")
        doOnAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        Log.d(LOG_TAG, "onDetachedFromActivity")
        doOnDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d(LOG_TAG, "onReattachedToActivityForConfigChanges")
        doOnAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.d(LOG_TAG, "onDetachedFromActivityForConfigChanges")
        doOnDetachedFromActivity()
    }

    private fun doOnAttachedToEngine(messenger: BinaryMessenger) {
        Log.d(LOG_TAG, "doOnAttachedToEngine - IN")

        this.channel = MethodChannel(messenger, "pdf_manipulator")
        this.channel.setMethodCallHandler(this)

        Log.d(LOG_TAG, "doOnAttachedToEngine - OUT")
    }

    private fun doOnDetachedFromEngine() {
        Log.d(LOG_TAG, "doOnDetachedFromEngine - IN")

        if (pluginBinding == null) {
            Log.w(LOG_TAG, "doOnDetachedFromEngine - already detached")
        }
        pluginBinding = null

        this.channel.setMethodCallHandler(null)

        Log.d(LOG_TAG, "doOnDetachedFromEngine - OUT")
    }

    private fun doOnAttachedToActivity(activityBinding: ActivityPluginBinding?) {
        Log.d(LOG_TAG, "doOnAttachedToActivity - IN")

        this.activityBinding = activityBinding

        Log.d(LOG_TAG, "doOnAttachedToActivity - OUT")
    }

    private fun doOnDetachedFromActivity() {
        Log.d(LOG_TAG, "doOnDetachedFromActivity - IN")

        if (pdfManipulator != null) {
            pdfManipulator = null
        }
        activityBinding = null

        Log.d(LOG_TAG, "doOnDetachedFromActivity - OUT")
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.d(LOG_TAG, "onMethodCall - IN , method=${call.method}")
        if (pdfManipulator == null) {
            if (!createPickOrSave()) {
                result.error("init_failed", "Not attached", null)
                return
            }
        }
        when (call.method) {
            "createBlankPdf" -> pdfManipulator!!.createBlankPdf(
                result,
                context,
                call.argument<Int>("pageCount") ?: 1,
                call.argument<Double>("width") ?: 595.0,
                call.argument<Double>("height") ?: 842.0,
            )
            "insertBlankPages" -> pdfManipulator!!.insertBlankPages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<Int>("insertAt") ?: 1,
                call.argument<Int>("blankPageCount") ?: 1,
                call.argument<Double>("width"),
                call.argument<Double>("height"),
            )
            "insertPages" -> pdfManipulator!!.insertPages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<String>("sourcePdfPath").toString(),
                call.argument<Int>("insertAt") ?: 1,
                parseMethodCallArrayOfIntArgument(call, "sourcePages"),
            )
            "replacePages" -> pdfManipulator!!.replacePages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<String>("replacementPdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pageNumbers") ?: listOf(),
                parseMethodCallArrayOfIntArgument(call, "replacementPages"),
            )
            "duplicatePages" -> pdfManipulator!!.duplicatePages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pageNumbers") ?: listOf(),
                call.argument<Boolean>("insertAfterEachPage") ?: true,
            )
            "extractPages" -> pdfManipulator!!.extractPages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pageNumbers") ?: listOf(),
            )
            "cropPages" -> pdfManipulator!!.cropPages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
                call.argument<Double>("left") ?: 0.0,
                call.argument<Double>("bottom") ?: 0.0,
                call.argument<Double>("right") ?: 595.0,
                call.argument<Double>("top") ?: 842.0,
                call.argument<Boolean>("applyToMediaBox") ?: false,
            )
            "resizePages" -> pdfManipulator!!.resizePages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
                call.argument<Double>("width") ?: 595.0,
                call.argument<Double>("height") ?: 842.0,
                call.argument<Boolean>("scaleToFit") ?: true,
            )
            "addPageNumbers" -> pdfManipulator!!.addPageNumbers(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<Map<String, Any>>("options") ?: mapOf(),
            )
            "addHeadersFooters" -> pdfManipulator!!.addHeadersFooters(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "headers") ?: listOf(),
                parseMethodCallArrayOfAnyMapArgument(call, "footers") ?: listOf(),
            )
            "addBackgrounds" -> pdfManipulator!!.addBackgrounds(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "backgrounds") ?: listOf(),
            )
            "addStamps" -> pdfManipulator!!.addStamps(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "stamps") ?: listOf(),
            )
            "addTextBlocks" -> pdfManipulator!!.addTextBlocks(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "blocks") ?: listOf(),
            )
            "addImages" -> pdfManipulator!!.addImages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "images") ?: listOf(),
            )
            "removeAnnotations" -> pdfManipulator!!.removeAnnotations(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "modifyAnnotations" -> pdfManipulator!!.pdfAnnotations(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "annotations") ?: listOf(),
            )
            "flattenAnnotations", "flattenPdf" -> pdfManipulator!!.flattenPdf(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "editText" -> pdfManipulator!!.editText(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "edits") ?: listOf(),
            )
            "editImages" -> pdfManipulator!!.editImages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "edits") ?: listOf(),
            )
            "pdfToWord" -> pdfManipulator!!.pdfToWord(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "pdfToExcel" -> pdfManipulator!!.pdfToExcel(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "pdfToPowerPoint" -> pdfManipulator!!.pdfToPowerPoint(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "pdfToHtml" -> pdfManipulator!!.pdfToHtml(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "pdfToTextFile" -> pdfManipulator!!.pdfToTextFile(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "documentToPdf" -> pdfManipulator!!.documentToPdf(
                result,
                context,
                call.argument<String>("documentPath").toString(),
            )
            "textToPdf" -> pdfManipulator!!.textToPdf(
                result,
                context,
                call.argument<String>("text").orEmpty(),
            )
            "scannerImagesToPdf" -> pdfManipulator!!.scannerImagesToPdf(
                result,
                context,
                parseMethodCallArrayOfStringArgument(call, "imagePaths") ?: listOf(),
                call.argument<Map<String, Any>>("options") ?: mapOf(),
            )
            "pdfAConversion" -> pdfManipulator!!.pdfAConversion(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "pdfAValidation" -> pdfManipulator!!.pdfAValidation(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "exportEmbeddedImages" -> pdfManipulator!!.exportEmbeddedImages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<String>("outputDir").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
                call.argument<String>("format") ?: "original",
            )
            "redactRegions" -> pdfManipulator!!.redactRegions(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "redactions") ?: listOf(),
            )
            "redactSearch" -> pdfManipulator!!.redactSearch(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfStringArgument(call, "terms") ?: listOf(),
                call.argument<Boolean>("caseSensitive") ?: false,
            )
            "redactPatterns" -> pdfManipulator!!.redactPatterns(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfStringArgument(call, "patterns") ?: listOf(),
            )
            "sanitizePdf" -> pdfManipulator!!.sanitizePdf(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<Map<String, Any>>("options") ?: mapOf(),
            )
            "ocrToSearchablePdf" -> pdfManipulator!!.ocrToSearchablePdf(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
                call.argument<Map<String, Any>>("options") ?: mapOf(),
            )
            "createFormFields" -> pdfManipulator!!.createFormFields(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfAnyMapArgument(call, "fields") ?: listOf(),
            )
            "editFormFields" -> pdfManipulator!!.editFormFields(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<Map<String, Any>>("values") ?: mapOf(),
                parseMethodCallArrayOfStringArgument(call, "removeFields") ?: listOf(),
            )
            "xfaInfo" -> pdfManipulator!!.xfaInfo(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "removeXfa" -> pdfManipulator!!.removeXfa(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "verifySignatures" -> pdfManipulator!!.advancedMap(result, "verifySignatures") {
                PdfAdvancedOperations.verifySignatures(context, call.argument<String>("pdfPath").toString())
            }
            "validateSignatureCertificates" -> pdfManipulator!!.advancedMap(result, "validateSignatureCertificates") {
                PdfAdvancedOperations.validateSignatureCertificates(context, call.argument<String>("pdfPath").toString())
            }
            "signatureLtvTimestampInfo" -> pdfManipulator!!.advancedMap(result, "signatureLtvTimestampInfo") {
                PdfAdvancedOperations.signatureLtvTimestampInfo(context, call.argument<String>("pdfPath").toString())
            }
            "addSignatureFields", "createESignRequest" -> pdfManipulator!!.advancedString(result, call.method) {
                PdfAdvancedOperations.addSignatureFields(context, call.argument<String>("pdfPath").toString(), parseMethodCallArrayOfAnyMapArgument(call, "fields") ?: listOf())
            }
            "eSignStatus" -> pdfManipulator!!.advancedMap(result, "eSignStatus") {
                PdfAdvancedOperations.eSignStatus(context, call.argument<String>("pdfPath").toString())
            }
            "addAttachments" -> pdfManipulator!!.advancedString(result, "addAttachments") {
                PdfAdvancedOperations.addAttachments(context, call.argument<String>("pdfPath").toString(), parseMethodCallArrayOfStringArgument(call, "attachmentPaths") ?: listOf(), call.argument<String>("description"))
            }
            "listAttachments" -> pdfManipulator!!.advancedMap(result, "listAttachments") {
                PdfAdvancedOperations.listAttachments(context, call.argument<String>("pdfPath").toString())
            }
            "extractAttachments" -> pdfManipulator!!.advancedMap(result, "extractAttachments") {
                PdfAdvancedOperations.extractAttachments(context, call.argument<String>("pdfPath").toString(), call.argument<String>("outputDir").toString())
            }
            "removeAttachments" -> pdfManipulator!!.advancedString(result, "removeAttachments") {
                PdfAdvancedOperations.removeAttachments(context, call.argument<String>("pdfPath").toString())
            }
            "createPortfolio" -> pdfManipulator!!.advancedString(result, "createPortfolio") {
                PdfAdvancedOperations.createPortfolio(context, call.argument<String>("pdfPath").toString(), parseMethodCallArrayOfStringArgument(call, "attachmentPaths") ?: listOf())
            }
            "layerInfo" -> pdfManipulator!!.advancedMap(result, "layerInfo") {
                PdfAdvancedOperations.layerInfo(context, call.argument<String>("pdfPath").toString())
            }
            "articleThreadsInfo" -> pdfManipulator!!.advancedMap(result, "articleThreadsInfo") {
                PdfAdvancedOperations.articleThreadsInfo(context, call.argument<String>("pdfPath").toString())
            }
            "namedDestinations" -> pdfManipulator!!.advancedMap(result, "namedDestinations") {
                PdfAdvancedOperations.namedDestinations(context, call.argument<String>("pdfPath").toString())
            }
            "addNamedDestination" -> pdfManipulator!!.advancedString(result, "addNamedDestination") {
                PdfAdvancedOperations.addNamedDestination(context, call.argument<String>("pdfPath").toString(), call.argument<String>("name") ?: "destination", call.argument<Int>("page") ?: 1)
            }
            "pageLabels" -> pdfManipulator!!.advancedMap(result, "pageLabels") {
                PdfAdvancedOperations.pageLabels(context, call.argument<String>("pdfPath").toString())
            }
            "addLink" -> pdfManipulator!!.advancedString(result, "addLink") {
                PdfAdvancedOperations.addLink(context, call.argument<String>("pdfPath").toString(), call.argument<Map<String, Any>>("link") ?: mapOf())
            }
            "removeLinks" -> pdfManipulator!!.advancedString(result, "removeLinks") {
                PdfAdvancedOperations.removeLinks(context, call.argument<String>("pdfPath").toString(), parseMethodCallArrayOfIntArgument(call, "pages"))
            }
            "extractTables" -> pdfManipulator!!.advancedMap(result, "extractTables") {
                PdfAdvancedOperations.extractTables(context, call.argument<String>("pdfPath").toString(), parseMethodCallArrayOfIntArgument(call, "pages"))
            }
            "structuredText" -> pdfManipulator!!.advancedMap(result, "structuredText") {
                PdfAdvancedOperations.structuredText(context, call.argument<String>("pdfPath").toString(), parseMethodCallArrayOfIntArgument(call, "pages"))
            }
            "visualDiffPdf" -> pdfManipulator!!.advancedString(result, "visualDiffPdf") {
                PdfAdvancedOperations.visualDiffPdf(context, call.argument<String>("pdfPath1").toString(), call.argument<String>("pdfPath2").toString())
            }
            "accessibilityInfo" -> pdfManipulator!!.advancedMap(result, "accessibilityInfo") {
                PdfAdvancedOperations.accessibilityInfo(context, call.argument<String>("pdfPath").toString())
            }
            "applyBasicAccessibility" -> pdfManipulator!!.advancedString(result, "applyBasicAccessibility") {
                PdfAdvancedOperations.applyBasicAccessibility(context, call.argument<String>("pdfPath").toString(), call.argument<String>("language") ?: "en-US", call.argument<String>("title") ?: "")
            }
            "pdfUaValidation" -> pdfManipulator!!.advancedMap(result, "pdfUaValidation") {
                PdfAdvancedOperations.pdfUaValidation(context, call.argument<String>("pdfPath").toString())
            }
            "addBatesNumbering" -> pdfManipulator!!.advancedString(result, "addBatesNumbering") {
                PdfAdvancedOperations.addBatesNumbering(context, call.argument<String>("pdfPath").toString(), call.argument<String>("prefix") ?: "BATES-", call.argument<Int>("start") ?: 1)
            }
            "addLegalLabels" -> pdfManipulator!!.advancedString(result, "addLegalLabels") {
                PdfAdvancedOperations.addLegalLabels(context, call.argument<String>("pdfPath").toString(), call.argument<String>("exhibit") ?: "EXHIBIT")
            }
            "documentActions" -> pdfManipulator!!.advancedMap(result, "documentActions") {
                PdfAdvancedOperations.documentActions(context, call.argument<String>("pdfPath").toString())
            }
            "removeDocumentActions" -> pdfManipulator!!.advancedString(result, "removeDocumentActions") {
                PdfAdvancedOperations.removeDocumentActions(context, call.argument<String>("pdfPath").toString())
            }
            "richMediaInfo" -> pdfManipulator!!.advancedMap(result, "richMediaInfo") {
                PdfAdvancedOperations.richMediaInfo(context, call.argument<String>("pdfPath").toString())
            }
            "incrementalSaveCopy" -> pdfManipulator!!.advancedString(result, "incrementalSaveCopy") {
                PdfAdvancedOperations.incrementalSaveCopy(context, call.argument<String>("pdfPath").toString())
            }
            "linearizedCopy" -> pdfManipulator!!.advancedString(result, "linearizedCopy") {
                PdfAdvancedOperations.linearizedCopy(context, call.argument<String>("pdfPath").toString())
            }
            "digitalRightsInfo" -> pdfManipulator!!.advancedMap(result, "digitalRightsInfo") {
                PdfAdvancedOperations.digitalRightsInfo(context, call.argument<String>("pdfPath").toString(), call.argument<String>("password") ?: "")
            }
            "validatePageOrder" -> result.success(
                PdfEditorOperations.validatePageOrder(
                    call.argument<Int>("pageCount") ?: 0,
                    parseMethodCallArrayOfIntArgument(call, "pageOrder") ?: listOf()
                )
            )
            "movePageOrder" -> result.success(
                PdfEditorOperations.movedOrder(
                    call.argument<Int>("pageCount") ?: 0,
                    call.argument<Int>("fromPage") ?: 1,
                    call.argument<Int>("toPage") ?: 1
                )
            )
            "swapPageOrder" -> result.success(
                PdfEditorOperations.swappedOrder(
                    call.argument<Int>("pageCount") ?: 0,
                    call.argument<Int>("firstPage") ?: 1,
                    call.argument<Int>("secondPage") ?: 1
                )
            )
            "reversePageOrder" -> result.success((1..(call.argument<Int>("pageCount") ?: 0)).toList().reversed())
            "extractImagesFromPdf" -> pdfManipulator!!.extractImagesFromPdf(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<String>("outputDir").toString(),
            )
            "pdfToImages" -> pdfManipulator!!.pdfToImages(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
                call.argument<String>("imageFormat").toString(),
                call.argument<Int>("quality") ?: 90,
                call.argument<Double>("scale") ?: 1.0,
            )
            "pdfTextExtraction" -> pdfManipulator!!.pdfTextExtraction(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
            )
            "pdfOcr" -> pdfManipulator!!.pdfOcr(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                parseMethodCallArrayOfIntArgument(call, "pages"),
                call.argument<String>("languageCode").toString(),
            )
            "pdfDigitalSignature" -> pdfManipulator!!.pdfDigitalSignature(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<String>("certificatePath").toString(),
                call.argument<String>("certificatePassword").toString(),
                call.argument<String>("reason"),
                call.argument<String>("location"),
                call.argument<String>("contact"),
                call.argument<Map<String, Any>>("appearance"),
            )
            "pdfAnnotations" -> pdfManipulator!!.pdfAnnotations(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<List<Map<String, Any>>>("annotations") ?: listOf(),
            )
            "fillFormFields" -> pdfManipulator!!.fillFormFields(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<Map<String, Any>>("fieldValues") ?: mapOf(),
                call.argument<Boolean>("flatten") == true,
            )
            "extractFormFieldData" -> pdfManipulator!!.extractFormFieldData(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "pdfMetadataReader" -> pdfManipulator!!.pdfMetadataReader(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "pdfMetadataWriter" -> pdfManipulator!!.pdfMetadataWriter(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<String>("title"),
                call.argument<String>("author"),
                call.argument<String>("subject"),
                call.argument<String>("keywords"),
                call.argument<String>("creator"),
                call.argument<String>("producer"),
                call.argument<String>("creationDate"),
                call.argument<String>("modificationDate"),
            )
            "pdfBookmarkReader" -> pdfManipulator!!.pdfBookmarkReader(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "pdfBookmarkWriter" -> pdfManipulator!!.pdfBookmarkWriter(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
                call.argument<List<Map<String, Any>>>("bookmarks") ?: listOf(),
            )
            "pdfComparison" -> pdfManipulator!!.pdfComparison(
                result,
                context,
                call.argument<String>("pdfPath1").toString(),
                call.argument<String>("pdfPath2").toString(),
                call.argument<Boolean>("compareText") ?: true,
                call.argument<Boolean>("compareMetadata") ?: true,
                call.argument<Boolean>("compareStructure") ?: true,
            )
            "pdfRepair" -> pdfManipulator!!.pdfRepair(
                result,
                context,
                call.argument<String>("pdfPath").toString(),
            )
            "mergePDFs" -> pdfManipulator!!.mergePdfs(
                result,
                sourceFilesPaths = parseMethodCallArrayOfStringArgument(
                    call, "pdfsPaths"
                ),
            )
            "splitPDF" -> pdfManipulator!!.splitPdf(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageCount = call.argument("pageCount") ?: 1,
                byteSize = call.argument("byteSize"),
                pageNumbers = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbers"
                ),
                pageRanges = parseMethodCallArrayOfStringArgument(
                    call, "pageRanges"
                ),
                pageRange = call.argument("pageRange"),
            )
            "pdfPageDeleter" -> pdfManipulator!!.pdfPageDeleter(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageNumbers = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbers"
                ),
            )
            "pdfPageReorder" -> pdfManipulator!!.pdfPageReorder(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageNumbers = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbers"
                ),
            )
            "pdfPageRotator" -> pdfManipulator!!.pdfPageRotator(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pagesRotationInfo = parseMethodCallArrayOfMapArgument(
                    call, "pagesRotationInfo"
                ),
            )
            "pdfPageRotatorDeleterReorder" -> pdfManipulator!!.pdfPageRotatorDeleterReorder(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageNumbersForReorder = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbersForReorder"
                ) ?: listOf(),
                pageNumbersForDeleter = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbersForDeleter"
                ) ?: listOf(),
                pagesRotationInfo = parseMethodCallArrayOfMapArgument(
                    call, "pagesRotationInfo"
                ) ?: listOf(),
            )
            "pdfCompressor" -> pdfManipulator!!.pdfCompressor(
                result,
                operationId = call.argument<String>("operationId") ?: "",
                sourceFilePath = call.argument("pdfPath"),
                imageQuality = call.argument("imageQuality"),
                imageScale = call.argument("imageScale"),
                unEmbedFonts = call.argument("unEmbedFonts"),
                advancedOptions = call.argument<Map<String, Any>>("advancedOptions"),
            )
            "pdfOptimizer" -> pdfManipulator!!.pdfOptimizer(
                result,
                sourceFilePath = call.argument("pdfPath"),
                removeMetadata = call.argument("removeMetadata"),
                removeUnusedObjects = call.argument("removeUnusedObjects"),
                mergeDuplicateObjects = call.argument("mergeDuplicateObjects"),
                optimizeStructure = call.argument("optimizeStructure"),
                isExternal = call.argument("isExternal"),
            )
            "pdfWatermark" -> pdfManipulator!!.watermarkPdf(
                result,
                sourceFilePath = call.argument("pdfPath"),
                text = call.argument("text"),
                imagePath = call.argument("imagePath"),
                fontSize = call.argument("fontSize"),
                watermarkLayer = parseMethodCallWatermarkLayerTypeArgument(call)
                    ?: WatermarkLayer.OverContent,
                opacity = call.argument("opacity"),
                rotationAngle = call.argument("rotationAngle"),
                watermarkColor = call.argument("watermarkColor"),
                positionType = parseMethodCallWatermarkPositionTypeArgument(call)
                    ?: PositionType.Center,
                customPositionXCoordinatesList = parseMethodCallArrayOfDoubleArgument(
                    call, "customPositionXCoordinatesList"
                ),
                customPositionYCoordinatesList = parseMethodCallArrayOfDoubleArgument(
                    call, "customPositionYCoordinatesList"
                ),
                imageWidth = call.argument("imageWidth"),
                imageHeight = call.argument("imageHeight"),
            )
            "pdfPagesSize" -> pdfManipulator!!.pdfPagesSize(
                result,
                sourceFilePath = call.argument("pdfPath"),
            )
            "pdfValidityAndProtection" -> pdfManipulator!!.pdfValidityAndProtection(
                result,
                sourceFilePath = call.argument("pdfPath"),
                userOrOwnerPassword = call.argument("password") ?: "",
            )
            "pdfDecryption" -> pdfManipulator!!.pdfDecryption(
                result,
                sourceFilePath = call.argument("pdfPath"),
                userOrOwnerPassword = call.argument("password") ?: "",
            )
            "pdfEncryption" -> pdfManipulator!!.pdfEncryption(
                result,
                sourceFilePath = call.argument("pdfPath"),
                ownerPassword = call.argument("ownerPassword") ?: "",
                userPassword = call.argument("userPassword") ?: "",
                allowPrinting = call.argument<Boolean>("allowPrinting") == true,
                allowModifyContents = call.argument<Boolean>("allowModifyContents") == true,
                allowCopy = call.argument<Boolean>("allowCopy") == true,
                allowModifyAnnotations = call.argument<Boolean>("allowModifyAnnotations") == true,
                allowFillIn = call.argument<Boolean>("allowFillIn") == true,
                allowScreenReaders = call.argument<Boolean>("allowScreenReaders") == true,
                allowAssembly = call.argument<Boolean>("allowAssembly") == true,
                allowDegradedPrinting = call.argument<Boolean>("allowDegradedPrinting") == true,
                standardEncryptionAES40 = call.argument<Boolean>("standardEncryptionAES40") == true,
                standardEncryptionAES128 = call.argument<Boolean>("standardEncryptionAES128") == true,
                encryptionAES128 = call.argument<Boolean>("encryptionAES128") == true,
                encryptionAES256 = call.argument<Boolean>("encryptionAES256") == true,
                encryptEmbeddedFilesOnly = call.argument<Boolean>("encryptEmbeddedFilesOnly") == true,
                doNotEncryptMetadata = call.argument<Boolean>("doNotEncryptMetadata") == true,
            )
            "pdfCertificateEncryption" -> pdfManipulator!!.pdfCertificateEncryption(
                result,
                sourceFilePath = call.argument("pdfPath"),
                recipients = call.argument<List<Map<String, Any>>>("recipients") ?: listOf(),
                standardEncryptionAES40 = call.argument<Boolean>("standardEncryptionAES40") == true,
                standardEncryptionAES128 = call.argument<Boolean>("standardEncryptionAES128") == true,
                encryptionAES128 = call.argument<Boolean>("encryptionAES128") == true,
                encryptionAES256 = call.argument<Boolean>("encryptionAES256") != false,
                encryptEmbeddedFilesOnly = call.argument<Boolean>("encryptEmbeddedFilesOnly") == true,
                doNotEncryptMetadata = call.argument<Boolean>("doNotEncryptMetadata") == true,
            )
            "imagesToPdfs" -> pdfManipulator!!.imagesToPdfs(
                result,
                sourceImagesPaths = parseMethodCallArrayOfStringArgument(
                    call, "imagesPaths"
                ),
                createSinglePdf = call.argument("createSinglePdf"),
            )
            "cancelManipulations" -> result.success(
                pdfManipulator!!.cancelManipulations(call.argument<String>("operationId"))
            )
            else -> result.notImplemented()
        }
    }

    private fun createPickOrSave(): Boolean {
        Log.d(LOG_TAG, "createPickOrSave - IN")

        var pdfManipulator: PdfManipulator? = null
        if (activityBinding != null) {
            pdfManipulator = PdfManipulator(
                activity = activityBinding!!.activity,
                methodChannel = channel
            )
        }
        this.pdfManipulator = pdfManipulator

        Log.d(LOG_TAG, "createPickOrSave - OUT")

        return pdfManipulator != null
    }

    private fun parseMethodCallArrayOfStringArgument(
        call: MethodCall, arg: String
    ): List<String>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<String>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfIntArgument(
        call: MethodCall, arg: String
    ): List<Int>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Int>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfDoubleArgument(
        call: MethodCall, arg: String
    ): List<Double>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Double>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfMapArgument(
        call: MethodCall, arg: String
    ): List<Map<String, Int>>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Map<String, Int>>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfAnyMapArgument(
        call: MethodCall, arg: String
    ): List<Map<String, Any>>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Map<String, Any>>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallWatermarkLayerTypeArgument(call: MethodCall): WatermarkLayer? {
        val arg = "watermarkLayer"
        if (call.hasArgument(arg)) {
            return if (call.argument<String>(arg)?.toString() == "WatermarkLayer.underContent") {
                WatermarkLayer.UnderContent
            } else {
                WatermarkLayer.OverContent
            }
        }
        return null
    }

    private fun parseMethodCallWatermarkPositionTypeArgument(call: MethodCall): PositionType? {
        val arg = "positionType"

        if (call.hasArgument(arg)) {
            return if (call.argument<String>(arg)?.toString() == "PositionType.topLeft") {
                PositionType.TopLeft
            } else if (call.argument<String>(arg)?.toString() == "PositionType.topCenter") {
                PositionType.TopCenter
            } else if (call.argument<String>(arg)?.toString() == "PositionType.topRight") {
                PositionType.TopRight
            } else if (call.argument<String>(arg)?.toString() == "PositionType.centerLeft") {
                PositionType.CenterLeft
            } else if (call.argument<String>(arg)?.toString() == "PositionType.center") {
                PositionType.Center
            } else if (call.argument<String>(arg)?.toString() == "PositionType.centerRight") {
                PositionType.CenterRight
            } else if (call.argument<String>(arg)?.toString() == "PositionType.bottomLeft") {
                PositionType.BottomLeft
            } else if (call.argument<String>(arg)?.toString() == "PositionType.bottomCenter") {
                PositionType.BottomCenter
            } else if (call.argument<String>(arg)?.toString() == "PositionType.bottomRight") {
                PositionType.BottomRight
            } else {
                PositionType.Custom
            }
        }
        return null
    }
}
