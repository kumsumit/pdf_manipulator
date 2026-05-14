import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'pdf_manipulator_method_channel.dart';

/// Callback for reporting operation progress
typedef ProgressCallback = void Function(double progress, String message);

/// Result containing operation ID for tracking and cancellation
class OperationResult<T> {
  final T result;
  final String operationId;

  OperationResult({required this.result, required this.operationId});
}

abstract class PdfManipulatorPlatform extends PlatformInterface {
  /// Constructs a PdfManipulatorPlatform.
  PdfManipulatorPlatform() : super(token: _token);

  static final Object _token = Object();

  static PdfManipulatorPlatform _instance = MethodChannelPdfManipulator();

  /// The default instance of [PdfManipulatorPlatform] to use.
  ///
  /// Defaults to [MethodChannelPdfManipulator].
  static PdfManipulatorPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [PdfManipulatorPlatform] when
  /// they register themselves.
  static set instance(PdfManipulatorPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> mergePDFs({PDFMergerParams? params}) {
    throw UnimplementedError('mergePDFs() has not been implemented.');
  }

  Future<List<String>?> splitPDF({PDFSplitterParams? params}) {
    throw UnimplementedError('splitPDF() has not been implemented.');
  }

  Future<String?> pdfPageDeleter({PDFPageDeleterParams? params}) {
    throw UnimplementedError('pdfPageDeleter() has not been implemented.');
  }

  Future<String?> pdfPageReorder({PDFPageReorderParams? params}) {
    throw UnimplementedError('pdfPageReorder() has not been implemented.');
  }

  Future<String?> pdfPageRotator({PDFPageRotatorParams? params}) {
    throw UnimplementedError('pdfPageRotator() has not been implemented.');
  }

  Future<String?> pdfPageRotatorDeleterReorder({
    PDFPageRotatorDeleterReorderParams? params,
  }) {
    throw UnimplementedError(
      'pdfPageRotatorDeleterReorder() has not been implemented.',
    );
  }

  Future<OperationResult<String?>> pdfCompressor({
    PDFCompressorParams? params,
    ProgressCallback? onProgress,
  }) {
    throw UnimplementedError('pdfCompressor() has not been implemented.');
  }

  Future<String?> pdfOptimizer({PDFOptimizerParams? params}) {
    throw UnimplementedError('pdfOptimizer() has not been implemented.');
  }

  Future<String?> pdfWatermark({PDFWatermarkParams? params}) {
    throw UnimplementedError('pdfWatermark() has not been implemented.');
  }

  Future<List<PageSizeInfo>?> pdfPagesSize({PDFPagesSizeParams? params}) {
    throw UnimplementedError('pdfPagesSize() has not been implemented.');
  }

  Future<PdfValidityAndProtection?> pdfValidityAndProtection({
    PDFValidityAndProtectionParams? params,
  }) {
    throw UnimplementedError(
      'pdfValidityAndProtection() has not been implemented.',
    );
  }

  Future<String?> pdfDecryption({PDFDecryptionParams? params}) {
    throw UnimplementedError('pdfDecryption() has not been implemented.');
  }

  Future<String?> pdfEncryption({PDFEncryptionParams? params}) {
    throw UnimplementedError('pdfEncryption() has not been implemented.');
  }

  Future<String?> pdfCertificateEncryption({
    PDFCertificateEncryptionParams? params,
  }) {
    throw UnimplementedError(
      'pdfCertificateEncryption() has not been implemented.',
    );
  }

  Future<List<String>?> imagesToPdfs({ImagesToPDFsParams? params}) {
    throw UnimplementedError('imagesToPdfs() has not been implemented.');
  }

  Future<String?> cancelManipulations({String? operationId}) {
    throw UnimplementedError('cancelManipulations() has not been implemented.');
  }

  /// Extracts images from the provided PDF file.
  ///
  /// Returns a list of paths to the extracted images.
  /// Throws exception on error.
  Future<List<String>?> extractImagesFromPdf({
    ExtractImageFromPDFParams? params,
  }) {
    throw UnimplementedError(
      'extractImagesFromPdf() has not been implemented.',
    );
  }

  /// Converts PDF pages to images.
  ///
  /// Returns a list of paths to the generated images.
  /// Throws exception on error.
  Future<List<String>?> pdfToImages({PDFToImagesParams? params}) {
    throw UnimplementedError('pdfToImages() has not been implemented.');
  }

  /// Extracts text from PDF pages.
  ///
  /// Returns PDFTextExtractionResult containing page-wise and full text.
  /// Throws exception on error.
  Future<PDFTextExtractionResult?> pdfTextExtraction({
    PDFTextExtractionParams? params,
  }) {
    throw UnimplementedError('pdfTextExtraction() has not been implemented.');
  }

  /// Performs OCR on PDF pages using Google ML Kit.
  ///
  /// Returns PDFOCRResult containing recognized text and confidence scores.
  /// Throws exception on error.
  Future<PDFOCRResult?> pdfOcr({PDFOCRParams? params}) {
    throw UnimplementedError('pdfOcr() has not been implemented.');
  }

  /// Adds a digital signature to PDF using certificate.
  ///
  /// Returns the path to the signed PDF file.
  /// Throws exception on error.
  Future<String?> pdfDigitalSignature({PDFDigitalSignatureParams? params}) {
    throw UnimplementedError('pdfDigitalSignature() has not been implemented.');
  }

  /// Adds annotations to PDF.
  ///
  /// Returns the path to the annotated PDF file.
  /// Throws exception on error.
  Future<String?> pdfAnnotations({PDFAnnotationsParams? params}) {
    throw UnimplementedError('pdfAnnotations() has not been implemented.');
  }

  /// Fills PDF form fields with the provided values.
  ///
  /// Returns the path to the filled PDF file.
  /// Throws exception on error.
  Future<String?> fillFormFields({PDFFormFillParams? params}) {
    throw UnimplementedError('fillFormFields() has not been implemented.');
  }

  /// Extracts PDF form field names, values, types, and options.
  ///
  /// Returns PDFFormFieldData containing field metadata keyed by field name.
  /// Throws exception on error.
  Future<PDFFormFieldData?> extractFormFieldData({
    PDFFormFieldDataParams? params,
  }) {
    throw UnimplementedError(
      'extractFormFieldData() has not been implemented.',
    );
  }

  /// Reads PDF metadata (title, author, subject, keywords, etc.).
  ///
  /// Returns PDFMetadataResult containing metadata information.
  /// Throws exception on error.
  Future<PDFMetadataResult?> pdfMetadataReader({
    PDFMetadataReaderParams? params,
  }) {
    throw UnimplementedError('pdfMetadataReader() has not been implemented.');
  }

  /// Updates PDF metadata (title, author, subject, keywords, etc.).
  ///
  /// Returns the path to the updated PDF file.
  /// Throws exception on error.
  Future<String?> pdfMetadataWriter({PDFMetadataWriterParams? params}) {
    throw UnimplementedError('pdfMetadataWriter() has not been implemented.');
  }

  /// Extracts PDF bookmarks/outlines (table of contents).
  ///
  /// Returns PDFBookmarkData containing hierarchical bookmark structure.
  /// Throws exception on error.
  Future<PDFBookmarkData?> pdfBookmarkReader({
    PDFBookmarkReaderParams? params,
  }) {
    throw UnimplementedError('pdfBookmarkReader() has not been implemented.');
  }

  /// Creates or modifies PDF bookmarks/outlines.
  ///
  /// Returns the path to the updated PDF file.
  /// Throws exception on error.
  Future<String?> pdfBookmarkWriter({PDFBookmarkWriterParams? params}) {
    throw UnimplementedError('pdfBookmarkWriter() has not been implemented.');
  }

  /// Compares two PDFs and highlights differences.
  ///
  /// Returns PDFComparisonResult containing detailed comparison information.
  /// Throws exception on error.
  Future<PDFComparisonResult?> pdfComparison({PDFComparisonParams? params}) {
    throw UnimplementedError('pdfComparison() has not been implemented.');
  }

  /// Attempts to repair a corrupted or damaged PDF file.
  ///
  /// Returns PDFRepairResult containing repair status and recovered content.
  /// Throws exception on error.
  Future<PDFRepairResult?> pdfRepair({PDFRepairParams? params}) {
    throw UnimplementedError('pdfRepair() has not been implemented.');
  }

  Future<String?> createBlankPdf({PDFCreateBlankParams? params}) =>
      throw UnimplementedError('createBlankPdf() has not been implemented.');
  Future<String?> insertBlankPages({PDFInsertBlankPagesParams? params}) =>
      throw UnimplementedError('insertBlankPages() has not been implemented.');
  Future<String?> insertPages({PDFInsertPagesParams? params}) =>
      throw UnimplementedError('insertPages() has not been implemented.');
  Future<String?> replacePages({PDFReplacePagesParams? params}) =>
      throw UnimplementedError('replacePages() has not been implemented.');
  Future<String?> duplicatePages({PDFDuplicatePagesParams? params}) =>
      throw UnimplementedError('duplicatePages() has not been implemented.');
  Future<String?> extractPages({PDFExtractPagesParams? params}) =>
      throw UnimplementedError('extractPages() has not been implemented.');
  Future<String?> cropPages({PDFCropPagesParams? params}) =>
      throw UnimplementedError('cropPages() has not been implemented.');
  Future<String?> resizePages({PDFResizePagesParams? params}) =>
      throw UnimplementedError('resizePages() has not been implemented.');
  Future<String?> addPageNumbers({PDFPageNumbersParams? params}) =>
      throw UnimplementedError('addPageNumbers() has not been implemented.');
  Future<String?> addHeadersFooters({PDFHeadersFootersParams? params}) =>
      throw UnimplementedError('addHeadersFooters() has not been implemented.');
  Future<String?> addBackgrounds({PDFBackgroundsParams? params}) =>
      throw UnimplementedError('addBackgrounds() has not been implemented.');
  Future<String?> addStamps({PDFStampsParams? params}) =>
      throw UnimplementedError('addStamps() has not been implemented.');
  Future<String?> addTextBlocks({PDFTextBlocksParams? params}) =>
      throw UnimplementedError('addTextBlocks() has not been implemented.');
  Future<String?> addImages({PDFImagesParams? params}) =>
      throw UnimplementedError('addImages() has not been implemented.');
  Future<String?> editText({PDFTextEditsParams? params}) =>
      throw UnimplementedError('editText() has not been implemented.');
  Future<String?> editImages({PDFImageEditsParams? params}) =>
      throw UnimplementedError('editImages() has not been implemented.');
  Future<String?> removeAnnotations({PDFRemoveAnnotationsParams? params}) =>
      throw UnimplementedError('removeAnnotations() has not been implemented.');
  Future<String?> modifyAnnotations({PDFAnnotationsParams? params}) =>
      throw UnimplementedError('modifyAnnotations() has not been implemented.');
  Future<String?> flattenAnnotations({PDFFlattenParams? params}) =>
      throw UnimplementedError(
        'flattenAnnotations() has not been implemented.',
      );
  Future<String?> flattenPdf({PDFFlattenParams? params}) =>
      throw UnimplementedError('flattenPdf() has not been implemented.');
  Future<PDFPageOrderValidationResult?> validatePageOrder({
    PDFPageOrderValidationParams? params,
  }) =>
      throw UnimplementedError('validatePageOrder() has not been implemented.');
  Future<List<int>?> movePageOrder({PDFMovePageOrderParams? params}) =>
      throw UnimplementedError('movePageOrder() has not been implemented.');
  Future<List<int>?> swapPageOrder({PDFSwapPageOrderParams? params}) =>
      throw UnimplementedError('swapPageOrder() has not been implemented.');
  Future<List<int>?> reversePageOrder({PDFReversePageOrderParams? params}) =>
      throw UnimplementedError('reversePageOrder() has not been implemented.');

  Future<String?> pdfToWord({PDFDocumentExportParams? params}) =>
      throw UnimplementedError('pdfToWord() has not been implemented.');
  Future<String?> pdfToExcel({PDFDocumentExportParams? params}) =>
      throw UnimplementedError('pdfToExcel() has not been implemented.');
  Future<String?> pdfToPowerPoint({PDFDocumentExportParams? params}) =>
      throw UnimplementedError('pdfToPowerPoint() has not been implemented.');
  Future<String?> pdfToHtml({PDFDocumentExportParams? params}) =>
      throw UnimplementedError('pdfToHtml() has not been implemented.');
  Future<String?> pdfToTextFile({PDFDocumentExportParams? params}) =>
      throw UnimplementedError('pdfToTextFile() has not been implemented.');
  Future<String?> documentToPdf({PDFDocumentToPdfParams? params}) =>
      throw UnimplementedError('documentToPdf() has not been implemented.');
  Future<String?> textToPdf({PDFTextToPdfParams? params}) =>
      throw UnimplementedError('textToPdf() has not been implemented.');
  Future<String?> scannerImagesToPdf({PDFScannerImagesToPdfParams? params}) =>
      throw UnimplementedError(
        'scannerImagesToPdf() has not been implemented.',
      );
  Future<String?> pdfAConversion({PDFArchiveConversionParams? params}) =>
      throw UnimplementedError('pdfAConversion() has not been implemented.');
  Future<PDFArchiveValidationResult?> pdfAValidation({
    PDFArchiveValidationParams? params,
  }) => throw UnimplementedError('pdfAValidation() has not been implemented.');
  Future<PDFEmbeddedImagesExportResult?> exportEmbeddedImages({
    PDFEmbeddedImagesExportParams? params,
  }) => throw UnimplementedError(
    'exportEmbeddedImages() has not been implemented.',
  );
  Future<String?> redactRegions({PDFRedactRegionsParams? params}) =>
      throw UnimplementedError('redactRegions() has not been implemented.');
  Future<String?> redactSearch({PDFRedactSearchParams? params}) =>
      throw UnimplementedError('redactSearch() has not been implemented.');
  Future<String?> redactPatterns({PDFRedactPatternsParams? params}) =>
      throw UnimplementedError('redactPatterns() has not been implemented.');
  Future<String?> sanitizePdf({PDFSanitizeParams? params}) =>
      throw UnimplementedError('sanitizePdf() has not been implemented.');
  Future<String?> ocrToSearchablePdf({PDFSearchableOCRParams? params}) =>
      throw UnimplementedError(
        'ocrToSearchablePdf() has not been implemented.',
      );
  Future<String?> createFormFields({PDFCreateFormFieldsParams? params}) =>
      throw UnimplementedError('createFormFields() has not been implemented.');
  Future<String?> editFormFields({PDFEditFormFieldsParams? params}) =>
      throw UnimplementedError('editFormFields() has not been implemented.');
  Future<PDFXfaInfo?> xfaInfo({PDFXfaParams? params}) =>
      throw UnimplementedError('xfaInfo() has not been implemented.');
  Future<String?> removeXfa({PDFXfaParams? params}) =>
      throw UnimplementedError('removeXfa() has not been implemented.');
  Future<Map<String, dynamic>?> advancedInfo({
    required String method,
    PDFAdvancedParams? params,
  }) => throw UnimplementedError('advancedInfo() has not been implemented.');
  Future<String?> advancedDocument({
    required String method,
    PDFAdvancedParams? params,
  }) =>
      throw UnimplementedError('advancedDocument() has not been implemented.');
}
