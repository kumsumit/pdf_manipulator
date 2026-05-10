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
  Future<String?> pdfPageRotatorDeleterReorder(
      {PDFPageRotatorDeleterReorderParams? params}) {
    return PdfManipulatorPlatform.instance
        .pdfPageRotatorDeleterReorder(params: params);
  }

  /// Compresses provided pdf file.
  ///
  /// Returns OperationResult containing the path or uri of the resultant file or null if operation was cancelled.
  /// Throws exception on error.
  Future<OperationResult<String?>> pdfCompressor({PDFCompressorParams? params, ProgressCallback? onProgress}) {
    return PdfManipulatorPlatform.instance.pdfCompressor(params: params, onProgress: onProgress);
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
  /// Returns List<PageSizeInfo> for pages size info or null if operation was cancelled.
  /// Throws exception on error.
  Future<List<PageSizeInfo>?> pdfPagesSize({PDFPagesSizeParams? params}) {
    return PdfManipulatorPlatform.instance.pdfPagesSize(params: params);
  }

  /// Extracts images from the provided PDF file.
  ///
  /// Returns a list of paths to the extracted images.
  /// Throws exception on error.
  Future<List<String>?> extractImagesFromPdf(
      {required ExtractImageFromPDFParams params}) {
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
  Future<PDFTextExtractionResult?> pdfTextExtraction(
      {required PDFTextExtractionParams params}) {
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
  Future<String?> pdfDigitalSignature(
      {required PDFDigitalSignatureParams params}) {
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
  Future<PdfValidityAndProtection?> pdfValidityAndProtection(
      {PDFValidityAndProtectionParams? params}) {
    return PdfManipulatorPlatform.instance
        .pdfValidityAndProtection(params: params);
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
    return PdfManipulatorPlatform.instance.cancelManipulations(operationId: operationId);
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
  Future<String?> pdfMetadataWriter({
    required PDFMetadataWriterParams params,
  }) {
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
  Future<String?> pdfBookmarkWriter({
    required PDFBookmarkWriterParams params,
  }) {
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
  Future<PDFRepairResult?> pdfRepair({
    required PDFRepairParams params,
  }) {
    return PdfManipulatorPlatform.instance.pdfRepair(params: params);
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
        results.add(PDFBatchOperationResult(
          type: operation.type,
          id: operation.id,
          success: true,
          result: result,
        ));
      } catch (error) {
        results.add(PDFBatchOperationResult(
          type: operation.type,
          id: operation.id,
          success: false,
          error: error.toString(),
        ));

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
