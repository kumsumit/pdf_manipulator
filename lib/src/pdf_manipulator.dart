import 'package:pdf_manipulator/src/pdf_manipulator_method_channel.dart';
import 'pdf_manipulator_platform_interface.dart';

class PdfManipulator {
  /// Merges provided pdf files.
  ///
  /// Returns the path or uri of the resultant merged file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> mergePDFs({PDFMergerParams? params}) {
    return PdfManipulatorPlatform.instance.mergePDFs(params: params);
  }

  /// Split provided pdf file.
  ///
  /// Returns the paths or uris of the resultant split file or null if operation was cancelled.
  /// Throws exception on error.
  Future<List<String>?> splitPDF({PDFSplitterParams? params}) {
    return PdfManipulatorPlatform.instance.splitPDF(params: params);
  }

  /// Deletes pages from provided pdf file.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfPageDeleter({PDFPageDeleterParams? params}) {
    return PdfManipulatorPlatform.instance.pdfPageDeleter(params: params);
  }

  /// Reorders pages of provided pdf file.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfPageReorder({PDFPageReorderParams? params}) {
    return PdfManipulatorPlatform.instance.pdfPageReorder(params: params);
  }

  /// Rotate pages of provided pdf file.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfPageRotator({PDFPageRotatorParams? params}) {
    return PdfManipulatorPlatform.instance.pdfPageRotator(params: params);
  }

  /// Rotate, delete, reorder pages of provided pdf file.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfPageRotatorDeleterReorder({
    PDFPageRotatorDeleterReorderParams? params,
  }) {
    return PdfManipulatorPlatform.instance.pdfPageRotatorDeleterReorder(
      params: params,
    );
  }

  /// Compresses provided pdf file.
  ///
  /// Returns OperationResult containing the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<OperationResult<String?>> pdfCompressor({
    PDFCompressorParams? params,
    ProgressCallback? onProgress,
  }) {
    return PdfManipulatorPlatform.instance.pdfCompressor(
      params: params,
      onProgress: onProgress,
    );
  }

  /// Optimizes provided pdf file to reduce size without quality loss.
  ///
  /// Returns the path or uri of the resultant optimized file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfOptimizer({PDFOptimizerParams? params}) {
    return PdfManipulatorPlatform.instance.pdfOptimizer(params: params);
  }

  /// Watermarks provided pdf file.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfWatermark({PDFWatermarkParams? params}) {
    return PdfManipulatorPlatform.instance.pdfWatermark(params: params);
  }

  /// Provides pdf file pages size info.
  ///
  /// Returns List`<PageSizeInfo>` for pages size info or null if operation was cancelled.
  /// Throws exception on error.
  Future<List<PageSizeInfo>?> pdfPagesSize({PDFPagesSizeParams? params}) {
    return PdfManipulatorPlatform.instance.pdfPagesSize(params: params);
  }

  /// Extracts images from the provided PDF file.
  ///
  /// Returns a list of paths to the extracted images.
  /// Throws exception on error.
  Future<List<String>?> extractImagesFromPdf({
    required ExtractImageFromPDFParams params,
  }) {
    return PdfManipulatorPlatform.instance.extractImagesFromPdf(params: params);
  }

  /// Converts PDF pages to images.
  ///
  /// Returns a list of paths to the generated images.
  /// Throws exception on error.
  Future<List<String>?> pdfToImages({required PDFToImagesParams params}) {
    return PdfManipulatorPlatform.instance.pdfToImages(params: params);
  }

  /// Extracts text from PDF pages.
  ///
  /// Returns PDFTextExtractionResult containing page-wise and full text.
  /// Throws exception on error.
  Future<PDFTextExtractionResult?> pdfTextExtraction({
    required PDFTextExtractionParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfTextExtraction(params: params);
  }

  /// Performs OCR on PDF pages using Google ML Kit.
  ///
  /// Returns PDFOCRResult containing recognized text and confidence scores.
  /// Throws exception on error.
  Future<PDFOCRResult?> pdfOcr({required PDFOCRParams params}) {
    return PdfManipulatorPlatform.instance.pdfOcr(params: params);
  }

  /// Adds a digital signature to PDF using certificate.
  ///
  /// Returns the path to the signed PDF file.
  /// Throws exception on error.
  Future<String?> pdfDigitalSignature({
    required PDFDigitalSignatureParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfDigitalSignature(params: params);
  }

  /// Adds annotations to PDF.
  ///
  /// Returns the path to the annotated PDF file.
  /// Throws exception on error.
  Future<String?> pdfAnnotations({required PDFAnnotationsParams params}) {
    return PdfManipulatorPlatform.instance.pdfAnnotations(params: params);
  }

  /// Fills PDF form fields with the provided values.
  ///
  /// Returns the path to the filled PDF file.
  /// Throws exception on error.
  Future<String?> fillFormFields({required PDFFormFillParams params}) {
    return PdfManipulatorPlatform.instance.fillFormFields(params: params);
  }

  /// Extracts PDF form field names, values, types, and options.
  ///
  /// Returns PDFFormFieldData containing field metadata keyed by field name.
  /// Throws exception on error.
  Future<PDFFormFieldData?> extractFormFieldData({
    required PDFFormFieldDataParams params,
  }) {
    return PdfManipulatorPlatform.instance.extractFormFieldData(params: params);
  }

  /// Provides pdf file validity and protection info.
  ///
  /// Returns PdfValidityAndProtection for pdf file or null if operation was cancelled.
  /// Throws exception on error.
  Future<PdfValidityAndProtection?> pdfValidityAndProtection({
    PDFValidityAndProtectionParams? params,
  }) {
    return PdfManipulatorPlatform.instance.pdfValidityAndProtection(
      params: params,
    );
  }

  /// Provides pdf file for decryption.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfDecryption({PDFDecryptionParams? params}) {
    return PdfManipulatorPlatform.instance.pdfDecryption(params: params);
  }

  /// Provides pdf file for encryption.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfEncryption({PDFEncryptionParams? params}) {
    return PdfManipulatorPlatform.instance.pdfEncryption(params: params);
  }

  /// Provides pdf file for certificate-based encryption.
  ///
  /// Returns the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<String?> pdfCertificateEncryption({
    required PDFCertificateEncryptionParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfCertificateEncryption(
      params: params,
    );
  }

  /// Provide images to convert to pdfs.
  ///
  /// Returns the paths or uris of pdf files or null if operation was cancelled.
  /// Throws exception on error.
  Future<List<String>?> imagesToPdfs({ImagesToPDFsParams? params}) {
    return PdfManipulatorPlatform.instance.imagesToPdfs(params: params);
  }

  /// Cancels running manipulations.
  ///
  /// Returns the cancelling message.
  Future<String?> cancelManipulations({String? operationId}) {
    return PdfManipulatorPlatform.instance.cancelManipulations(
      operationId: operationId,
    );
  }

  /// Reads PDF metadata (title, author, subject, keywords, etc.).
  ///
  /// Returns PDFMetadataResult containing metadata information.
  /// Throws exception on error.
  Future<PDFMetadataResult?> pdfMetadataReader({
    required PDFMetadataReaderParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfMetadataReader(params: params);
  }

  /// Updates PDF metadata (title, author, subject, keywords, etc.).
  ///
  /// Returns the path to the updated PDF file.
  /// Throws exception on error.
  Future<String?> pdfMetadataWriter({required PDFMetadataWriterParams params}) {
    return PdfManipulatorPlatform.instance.pdfMetadataWriter(params: params);
  }

  /// Extracts PDF bookmarks/outlines (table of contents).
  ///
  /// Returns PDFBookmarkData containing hierarchical bookmark structure.
  /// Throws exception on error.
  Future<PDFBookmarkData?> pdfBookmarkReader({
    required PDFBookmarkReaderParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfBookmarkReader(params: params);
  }

  /// Creates or modifies PDF bookmarks/outlines.
  ///
  /// Returns the path to the updated PDF file.
  /// Throws exception on error.
  Future<String?> pdfBookmarkWriter({required PDFBookmarkWriterParams params}) {
    return PdfManipulatorPlatform.instance.pdfBookmarkWriter(params: params);
  }

  /// Compares two PDFs and highlights differences.
  ///
  /// Returns PDFComparisonResult containing detailed comparison information.
  /// Throws exception on error.
  Future<PDFComparisonResult?> pdfComparison({
    required PDFComparisonParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfComparison(params: params);
  }

  /// Attempts to repair a corrupted or damaged PDF file.
  ///
  /// Returns PDFRepairResult containing repair status and recovered content.
  /// Throws exception on error.
  Future<PDFRepairResult?> pdfRepair({required PDFRepairParams params}) {
    return PdfManipulatorPlatform.instance.pdfRepair(params: params);
  }

  Future<String?> createBlankPdf({required PDFCreateBlankParams params}) {
    return PdfManipulatorPlatform.instance.createBlankPdf(params: params);
  }

  Future<String?> insertBlankPages({
    required PDFInsertBlankPagesParams params,
  }) {
    return PdfManipulatorPlatform.instance.insertBlankPages(params: params);
  }

  Future<String?> insertPages({required PDFInsertPagesParams params}) {
    return PdfManipulatorPlatform.instance.insertPages(params: params);
  }

  Future<String?> replacePages({required PDFReplacePagesParams params}) {
    return PdfManipulatorPlatform.instance.replacePages(params: params);
  }

  Future<String?> duplicatePages({required PDFDuplicatePagesParams params}) {
    return PdfManipulatorPlatform.instance.duplicatePages(params: params);
  }

  Future<String?> extractPages({required PDFExtractPagesParams params}) {
    return PdfManipulatorPlatform.instance.extractPages(params: params);
  }

  Future<String?> cropPages({required PDFCropPagesParams params}) {
    return PdfManipulatorPlatform.instance.cropPages(params: params);
  }

  Future<String?> resizePages({required PDFResizePagesParams params}) {
    return PdfManipulatorPlatform.instance.resizePages(params: params);
  }

  Future<String?> addPageNumbers({required PDFPageNumbersParams params}) {
    return PdfManipulatorPlatform.instance.addPageNumbers(params: params);
  }

  Future<String?> addHeadersFooters({required PDFHeadersFootersParams params}) {
    return PdfManipulatorPlatform.instance.addHeadersFooters(params: params);
  }

  Future<String?> addBackgrounds({required PDFBackgroundsParams params}) {
    return PdfManipulatorPlatform.instance.addBackgrounds(params: params);
  }

  Future<String?> addStamps({required PDFStampsParams params}) {
    return PdfManipulatorPlatform.instance.addStamps(params: params);
  }

  Future<String?> addTextBlocks({required PDFTextBlocksParams params}) {
    return PdfManipulatorPlatform.instance.addTextBlocks(params: params);
  }

  Future<String?> addImages({required PDFImagesParams params}) {
    return PdfManipulatorPlatform.instance.addImages(params: params);
  }

  Future<String?> editText({required PDFTextEditsParams params}) {
    return PdfManipulatorPlatform.instance.editText(params: params);
  }

  Future<String?> editImages({required PDFImageEditsParams params}) {
    return PdfManipulatorPlatform.instance.editImages(params: params);
  }

  Future<String?> removeAnnotations({
    required PDFRemoveAnnotationsParams params,
  }) {
    return PdfManipulatorPlatform.instance.removeAnnotations(params: params);
  }

  Future<String?> modifyAnnotations({required PDFAnnotationsParams params}) {
    return PdfManipulatorPlatform.instance.modifyAnnotations(params: params);
  }

  Future<String?> flattenAnnotations({required PDFFlattenParams params}) {
    return PdfManipulatorPlatform.instance.flattenAnnotations(params: params);
  }

  Future<String?> flattenPdf({required PDFFlattenParams params}) {
    return PdfManipulatorPlatform.instance.flattenPdf(params: params);
  }

  Future<PDFPageOrderValidationResult?> validatePageOrder({
    required PDFPageOrderValidationParams params,
  }) {
    return PdfManipulatorPlatform.instance.validatePageOrder(params: params);
  }

  Future<List<int>?> movePageOrder({required PDFMovePageOrderParams params}) {
    return PdfManipulatorPlatform.instance.movePageOrder(params: params);
  }

  Future<List<int>?> swapPageOrder({required PDFSwapPageOrderParams params}) {
    return PdfManipulatorPlatform.instance.swapPageOrder(params: params);
  }

  Future<List<int>?> reversePageOrder({
    required PDFReversePageOrderParams params,
  }) {
    return PdfManipulatorPlatform.instance.reversePageOrder(params: params);
  }

  Future<String?> pdfToWord({required PDFDocumentExportParams params}) {
    return PdfManipulatorPlatform.instance.pdfToWord(params: params);
  }

  Future<String?> pdfToExcel({required PDFDocumentExportParams params}) {
    return PdfManipulatorPlatform.instance.pdfToExcel(params: params);
  }

  Future<String?> pdfToPowerPoint({required PDFDocumentExportParams params}) {
    return PdfManipulatorPlatform.instance.pdfToPowerPoint(params: params);
  }

  Future<String?> pdfToHtml({required PDFDocumentExportParams params}) {
    return PdfManipulatorPlatform.instance.pdfToHtml(params: params);
  }

  Future<String?> pdfToTextFile({required PDFDocumentExportParams params}) {
    return PdfManipulatorPlatform.instance.pdfToTextFile(params: params);
  }

  Future<String?> documentToPdf({required PDFDocumentToPdfParams params}) {
    return PdfManipulatorPlatform.instance.documentToPdf(params: params);
  }

  Future<String?> textToPdf({required PDFTextToPdfParams params}) {
    return PdfManipulatorPlatform.instance.textToPdf(params: params);
  }

  Future<String?> scannerImagesToPdf({
    required PDFScannerImagesToPdfParams params,
  }) {
    return PdfManipulatorPlatform.instance.scannerImagesToPdf(params: params);
  }

  Future<String?> pdfAConversion({required PDFArchiveConversionParams params}) {
    return PdfManipulatorPlatform.instance.pdfAConversion(params: params);
  }

  Future<PDFArchiveValidationResult?> pdfAValidation({
    required PDFArchiveValidationParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfAValidation(params: params);
  }

  Future<PDFEmbeddedImagesExportResult?> exportEmbeddedImages({
    required PDFEmbeddedImagesExportParams params,
  }) {
    return PdfManipulatorPlatform.instance.exportEmbeddedImages(params: params);
  }

  Future<String?> redactRegions({required PDFRedactRegionsParams params}) {
    return PdfManipulatorPlatform.instance.redactRegions(params: params);
  }

  Future<String?> redactSearch({required PDFRedactSearchParams params}) {
    return PdfManipulatorPlatform.instance.redactSearch(params: params);
  }

  Future<String?> redactPatterns({required PDFRedactPatternsParams params}) {
    return PdfManipulatorPlatform.instance.redactPatterns(params: params);
  }

  Future<String?> sanitizePdf({required PDFSanitizeParams params}) {
    return PdfManipulatorPlatform.instance.sanitizePdf(params: params);
  }

  Future<String?> ocrToSearchablePdf({required PDFSearchableOCRParams params}) {
    return PdfManipulatorPlatform.instance.ocrToSearchablePdf(params: params);
  }

  Future<String?> createFormFields({
    required PDFCreateFormFieldsParams params,
  }) {
    return PdfManipulatorPlatform.instance.createFormFields(params: params);
  }

  Future<String?> editFormFields({required PDFEditFormFieldsParams params}) {
    return PdfManipulatorPlatform.instance.editFormFields(params: params);
  }

  Future<PDFXfaInfo?> xfaInfo({required PDFXfaParams params}) {
    return PdfManipulatorPlatform.instance.xfaInfo(params: params);
  }

  Future<String?> removeXfa({required PDFXfaParams params}) {
    return PdfManipulatorPlatform.instance.removeXfa(params: params);
  }

  Future<Map<String, dynamic>?> verifySignatures(String pdfPath) =>
      _advancedInfo('verifySignatures', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> validateSignatureCertificates(String pdfPath) =>
      _advancedInfo('validateSignatureCertificates', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> signatureLtvTimestampInfo(String pdfPath) =>
      _advancedInfo('signatureLtvTimestampInfo', {'pdfPath': pdfPath});
  Future<String?> addSignatureFields({
    required String pdfPath,
    required List<Map<String, dynamic>> fields,
  }) => _advancedDocument('addSignatureFields', {
    'pdfPath': pdfPath,
    'fields': fields,
  });
  Future<String?> createESignRequest({
    required String pdfPath,
    required List<Map<String, dynamic>> fields,
  }) => _advancedDocument('createESignRequest', {
    'pdfPath': pdfPath,
    'fields': fields,
  });
  Future<Map<String, dynamic>?> eSignStatus(String pdfPath) =>
      _advancedInfo('eSignStatus', {'pdfPath': pdfPath});
  Future<String?> addAttachments({
    required String pdfPath,
    required List<String> attachmentPaths,
    String? description,
  }) => _advancedDocument('addAttachments', {
    'pdfPath': pdfPath,
    'attachmentPaths': attachmentPaths,
    'description': description,
  });
  Future<Map<String, dynamic>?> listAttachments(String pdfPath) =>
      _advancedInfo('listAttachments', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> extractAttachments({
    required String pdfPath,
    required String outputDir,
  }) => _advancedInfo('extractAttachments', {
    'pdfPath': pdfPath,
    'outputDir': outputDir,
  });
  Future<String?> removeAttachments(String pdfPath) =>
      _advancedDocument('removeAttachments', {'pdfPath': pdfPath});
  Future<String?> createPortfolio({
    required String pdfPath,
    required List<String> attachmentPaths,
  }) => _advancedDocument('createPortfolio', {
    'pdfPath': pdfPath,
    'attachmentPaths': attachmentPaths,
  });
  Future<Map<String, dynamic>?> layerInfo(String pdfPath) =>
      _advancedInfo('layerInfo', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> articleThreadsInfo(String pdfPath) =>
      _advancedInfo('articleThreadsInfo', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> namedDestinations(String pdfPath) =>
      _advancedInfo('namedDestinations', {'pdfPath': pdfPath});
  Future<String?> addNamedDestination({
    required String pdfPath,
    required String name,
    required int page,
  }) => _advancedDocument('addNamedDestination', {
    'pdfPath': pdfPath,
    'name': name,
    'page': page,
  });
  Future<Map<String, dynamic>?> pageLabels(String pdfPath) =>
      _advancedInfo('pageLabels', {'pdfPath': pdfPath});
  Future<String?> addLink({
    required String pdfPath,
    required Map<String, dynamic> link,
  }) => _advancedDocument('addLink', {'pdfPath': pdfPath, 'link': link});
  Future<String?> removeLinks({required String pdfPath, List<int>? pages}) =>
      _advancedDocument('removeLinks', {'pdfPath': pdfPath, 'pages': pages});
  Future<Map<String, dynamic>?> extractTables({
    required String pdfPath,
    List<int>? pages,
  }) => _advancedInfo('extractTables', {'pdfPath': pdfPath, 'pages': pages});
  Future<Map<String, dynamic>?> structuredText({
    required String pdfPath,
    List<int>? pages,
  }) => _advancedInfo('structuredText', {'pdfPath': pdfPath, 'pages': pages});
  Future<String?> visualDiffPdf({
    required String pdfPath1,
    required String pdfPath2,
  }) => _advancedDocument('visualDiffPdf', {
    'pdfPath1': pdfPath1,
    'pdfPath2': pdfPath2,
  });
  Future<Map<String, dynamic>?> accessibilityInfo(String pdfPath) =>
      _advancedInfo('accessibilityInfo', {'pdfPath': pdfPath});
  Future<String?> applyBasicAccessibility({
    required String pdfPath,
    String language = 'en-US',
    String title = '',
  }) => _advancedDocument('applyBasicAccessibility', {
    'pdfPath': pdfPath,
    'language': language,
    'title': title,
  });
  Future<Map<String, dynamic>?> pdfUaValidationAdvanced(String pdfPath) =>
      _advancedInfo('pdfUaValidation', {'pdfPath': pdfPath});
  Future<String?> addBatesNumbering({
    required String pdfPath,
    String prefix = 'BATES-',
    int start = 1,
  }) => _advancedDocument('addBatesNumbering', {
    'pdfPath': pdfPath,
    'prefix': prefix,
    'start': start,
  });
  Future<String?> addLegalLabels({
    required String pdfPath,
    String exhibit = 'EXHIBIT',
  }) => _advancedDocument('addLegalLabels', {
    'pdfPath': pdfPath,
    'exhibit': exhibit,
  });
  Future<Map<String, dynamic>?> documentActions(String pdfPath) =>
      _advancedInfo('documentActions', {'pdfPath': pdfPath});
  Future<String?> removeDocumentActions(String pdfPath) =>
      _advancedDocument('removeDocumentActions', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> richMediaInfo(String pdfPath) =>
      _advancedInfo('richMediaInfo', {'pdfPath': pdfPath});
  Future<String?> incrementalSaveCopy(String pdfPath) =>
      _advancedDocument('incrementalSaveCopy', {'pdfPath': pdfPath});
  Future<String?> linearizedCopy(String pdfPath) =>
      _advancedDocument('linearizedCopy', {'pdfPath': pdfPath});
  Future<Map<String, dynamic>?> digitalRightsInfo({
    required String pdfPath,
    String password = '',
  }) => _advancedInfo('digitalRightsInfo', {
    'pdfPath': pdfPath,
    'password': password,
  });

  Future<Map<String, dynamic>?> _advancedInfo(
    String method,
    Map<String, dynamic> values,
  ) {
    return PdfManipulatorPlatform.instance.advancedInfo(
      method: method,
      params: PDFAdvancedParams(values),
    );
  }

  Future<String?> _advancedDocument(
    String method,
    Map<String, dynamic> values,
  ) {
    return PdfManipulatorPlatform.instance.advancedDocument(
      method: method,
      params: PDFAdvancedParams(values),
    );
  }

  /// Runs multiple PDF operations sequentially.
  ///
  /// Returns per-operation results. When [PDFBatchProcessorParams.stopOnError]
  /// is true, processing stops after the first failed operation.
  Future<PDFBatchProcessorResult> batchProcess({
    required PDFBatchProcessorParams params,
    BatchProgressCallback? onProgress,
  }) async {
    final results = <PDFBatchOperationResult>[];
    final total = params.operations.length;

    for (var index = 0; index < total; index++) {
      final operation = params.operations[index];

      try {
        final result = await _runBatchOperation(operation);
        results.add(
          PDFBatchOperationResult(
            type: operation.type,
            id: operation.id,
            success: true,
            result: result,
          ),
        );
      } catch (error) {
        results.add(
          PDFBatchOperationResult(
            type: operation.type,
            id: operation.id,
            success: false,
            error: error.toString(),
          ),
        );

        onProgress?.call(index + 1, total, operation);
        if (params.stopOnError) {
          break;
        }
        continue;
      }

      onProgress?.call(index + 1, total, operation);
    }

    return PDFBatchProcessorResult(results: results);
  }

  Future<Object?> _runBatchOperation(PDFBatchOperation operation) {
    switch (operation.type) {
      case PDFBatchOperationType.merge:
        return mergePDFs(params: operation.params as PDFMergerParams);
      case PDFBatchOperationType.split:
        return splitPDF(params: operation.params as PDFSplitterParams);
      case PDFBatchOperationType.deletePages:
        return pdfPageDeleter(params: operation.params as PDFPageDeleterParams);
      case PDFBatchOperationType.reorderPages:
        return pdfPageReorder(params: operation.params as PDFPageReorderParams);
      case PDFBatchOperationType.rotatePages:
        return pdfPageRotator(params: operation.params as PDFPageRotatorParams);
      case PDFBatchOperationType.rotateDeleteReorderPages:
        return pdfPageRotatorDeleterReorder(
          params: operation.params as PDFPageRotatorDeleterReorderParams,
        );
      case PDFBatchOperationType.compress:
        return pdfCompressor(params: operation.params as PDFCompressorParams);
      case PDFBatchOperationType.watermark:
        return pdfWatermark(params: operation.params as PDFWatermarkParams);
      case PDFBatchOperationType.pagesSize:
        return pdfPagesSize(params: operation.params as PDFPagesSizeParams);
      case PDFBatchOperationType.validityAndProtection:
        return pdfValidityAndProtection(
          params: operation.params as PDFValidityAndProtectionParams,
        );
      case PDFBatchOperationType.decrypt:
        return pdfDecryption(params: operation.params as PDFDecryptionParams);
      case PDFBatchOperationType.encrypt:
        return pdfEncryption(params: operation.params as PDFEncryptionParams);
      case PDFBatchOperationType.certificateEncrypt:
        return pdfCertificateEncryption(
          params: operation.params as PDFCertificateEncryptionParams,
        );
      case PDFBatchOperationType.imagesToPdfs:
        return imagesToPdfs(params: operation.params as ImagesToPDFsParams);
      case PDFBatchOperationType.extractImages:
        return extractImagesFromPdf(
          params: operation.params as ExtractImageFromPDFParams,
        );
      case PDFBatchOperationType.pdfToImages:
        return pdfToImages(params: operation.params as PDFToImagesParams);
      case PDFBatchOperationType.textExtraction:
        return pdfTextExtraction(
          params: operation.params as PDFTextExtractionParams,
        );
      case PDFBatchOperationType.ocr:
        return pdfOcr(params: operation.params as PDFOCRParams);
      case PDFBatchOperationType.digitalSignature:
        return pdfDigitalSignature(
          params: operation.params as PDFDigitalSignatureParams,
        );
      case PDFBatchOperationType.annotations:
        return pdfAnnotations(params: operation.params as PDFAnnotationsParams);
      case PDFBatchOperationType.fillFormFields:
        return fillFormFields(params: operation.params as PDFFormFillParams);
      case PDFBatchOperationType.extractFormFieldData:
        return extractFormFieldData(
          params: operation.params as PDFFormFieldDataParams,
        );
      case PDFBatchOperationType.metadataReader:
        return pdfMetadataReader(
          params: operation.params as PDFMetadataReaderParams,
        );
      case PDFBatchOperationType.metadataWriter:
        return pdfMetadataWriter(
          params: operation.params as PDFMetadataWriterParams,
        );
      case PDFBatchOperationType.bookmarkReader:
        return pdfBookmarkReader(
          params: operation.params as PDFBookmarkReaderParams,
        );
      case PDFBatchOperationType.bookmarkWriter:
        return pdfBookmarkWriter(
          params: operation.params as PDFBookmarkWriterParams,
        );
      case PDFBatchOperationType.comparison:
        return pdfComparison(params: operation.params as PDFComparisonParams);
      case PDFBatchOperationType.repair:
        return pdfRepair(params: operation.params as PDFRepairParams);
    }
  }
}
