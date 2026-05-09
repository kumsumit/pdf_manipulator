import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'pdf_manipulator_method_channel.dart';

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

  Future<String?> pdfPageRotatorDeleterReorder(
      {PDFPageRotatorDeleterReorderParams? params}) {
    throw UnimplementedError(
        'pdfPageRotatorDeleterReorder() has not been implemented.');
  }

  Future<String?> pdfCompressor({PDFCompressorParams? params}) {
    throw UnimplementedError('pdfCompressor() has not been implemented.');
  }

  Future<String?> pdfWatermark({PDFWatermarkParams? params}) {
    throw UnimplementedError('pdfWatermark() has not been implemented.');
  }

  Future<List<PageSizeInfo>?> pdfPagesSize({PDFPagesSizeParams? params}) {
    throw UnimplementedError('pdfPagesSize() has not been implemented.');
  }

  Future<PdfValidityAndProtection?> pdfValidityAndProtection(
      {PDFValidityAndProtectionParams? params}) {
    throw UnimplementedError(
        'pdfValidityAndProtection() has not been implemented.');
  }

  Future<String?> pdfDecryption({PDFDecryptionParams? params}) {
    throw UnimplementedError('pdfDecryption() has not been implemented.');
  }

  Future<String?> pdfEncryption({PDFEncryptionParams? params}) {
    throw UnimplementedError('pdfEncryption() has not been implemented.');
  }

  Future<List<String>?> imagesToPdfs({ImagesToPDFsParams? params}) {
    throw UnimplementedError('imagesToPdfs() has not been implemented.');
  }

  Future<String?> cancelManipulations() {
    throw UnimplementedError('cancelManipulations() has not been implemented.');
  }

  /// Extracts images from the provided PDF file.
  ///
  /// Returns a list of paths to the extracted images.
  /// Throws exception on error.
  Future<List<String>?> extractImagesFromPdf({ExtractImageFromPDFParams? params}) {
     throw UnimplementedError('extractImagesFromPdf() has not been implemented.');
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
  Future<PDFTextExtractionResult?> pdfTextExtraction({PDFTextExtractionParams? params}) {
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
}
