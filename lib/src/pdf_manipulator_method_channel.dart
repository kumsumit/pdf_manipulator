import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'pdf_manipulator_platform_interface.dart';

/// An implementation of [PdfManipulatorPlatform] that uses method channels.
class MethodChannelPdfManipulator extends PdfManipulatorPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('pdf_manipulator');

  @override
  Future<String?> mergePDFs({PDFMergerParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'mergePDFs', params?.toJson());
    return path;
  }

  @override
  Future<List<String>?> splitPDF({PDFSplitterParams? params}) async {
    final List? paths =
        await methodChannel.invokeMethod<List?>('splitPDF', params?.toJson());
    return paths?.cast<String>();
  }

  @override
  Future<String?> pdfPageDeleter({PDFPageDeleterParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfPageDeleter', params?.toJson());
    return path;
  }

  @override
  Future<String?> pdfPageReorder({PDFPageReorderParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfPageReorder', params?.toJson());
    return path;
  }

  @override
  Future<String?> pdfPageRotator({PDFPageRotatorParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfPageRotator', params?.toJson());
    return path;
  }

  @override
  Future<String?> pdfPageRotatorDeleterReorder(
      {PDFPageRotatorDeleterReorderParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfPageRotatorDeleterReorder', params?.toJson());
    return path;
  }

  @override
  Future<String?> pdfCompressor({PDFCompressorParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfCompressor', params?.toJson());
    return path;
  }

  @override
  Future<String?> pdfWatermark({PDFWatermarkParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfWatermark', params?.toJson());
    return path;
  }

  @override
  Future<List<PageSizeInfo>?> pdfPagesSize({PDFPagesSizeParams? params}) async {
    final List? result = await methodChannel.invokeMethod<List?>(
        'pdfPagesSize', params?.toJson());
    result?.cast<List<double>>();
    if (result == null) {
      return null;
    } else {
      return List<PageSizeInfo>.generate(
          result.length,
          (int index) => PageSizeInfo(
                pageNumber: (result[index][0] as double).toInt(),
                widthOfPage: result[index][1] as double,
                heightOfPage: result[index][2] as double,
              ));
    }
  }

  @override
  Future<PdfValidityAndProtection?> pdfValidityAndProtection(
      {PDFValidityAndProtectionParams? params}) async {
    final List? result = await methodChannel.invokeMethod<List?>(
        'pdfValidityAndProtection', params?.toJson());
    result?.cast<List<bool?>>();
    if (result == null) {
      return null;
    } else {
      return PdfValidityAndProtection(
          isPDFValid: result[0],
          isOwnerPasswordProtected: result[1],
          isOpenPasswordProtected: result[2],
          isPrintingAllowed: result[3],
          isModifyContentsAllowed: result[4]);
    }
  }

  @override
  Future<String?> pdfDecryption({PDFDecryptionParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfDecryption', params?.toJson());
    return path;
  }

  @override
  Future<String?> pdfEncryption({PDFEncryptionParams? params}) async {
    final String? path = await methodChannel.invokeMethod<String?>(
        'pdfEncryption', params?.toJson());
    return path;
  }

  @override
  Future<List<String>?> imagesToPdfs({ImagesToPDFsParams? params}) async {
    final List? paths = await methodChannel.invokeMethod<List?>(
        'imagesToPdfs', params?.toJson());
    return paths?.cast<String>();
  }

  @override
  Future<String?> cancelManipulations() async {
    final String? result =
        await methodChannel.invokeMethod<String?>('cancelManipulations');
    return result;
  }

  /// Extracts images from the provided PDF file.
  ///
  /// Returns a list of paths to the extracted images.
  /// Throws exception on error.
  @override
  Future<List<String>?> extractImagesFromPdf(
      {ExtractImageFromPDFParams? params}) async {
    final List? paths = await methodChannel.invokeMethod<List?>(
        'extractImagesFromPdf', params?.toJson());
    return paths?.cast<String>();
  }

  /// Converts PDF pages to images.
  ///
  /// Returns a list of paths to the generated images.
  /// Throws exception on error.
  @override
  Future<List<String>?> pdfToImages({PDFToImagesParams? params}) async {
    final List? paths = await methodChannel.invokeMethod<List?>(
        'pdfToImages', params?.toJson());
    return paths?.cast<String>();
  }

  /// Extracts text from PDF pages.
  ///
  /// Returns PDFTextExtractionResult containing page-wise and full text.
  /// Throws exception on error.
  @override
  Future<PDFTextExtractionResult?> pdfTextExtraction(
      {PDFTextExtractionParams? params}) async {
    final Map? result = await methodChannel.invokeMethod<Map?>(
        'pdfTextExtraction', params?.toJson());

    if (result == null) return null;

    final Map<int, String> pageTexts = {};
    result['pageTexts']?.forEach((key, value) {
      if (key is String && value is String) {
        pageTexts[int.parse(key)] = value;
      }
    });

    return PDFTextExtractionResult(
      pageTexts: pageTexts,
      fullText: result['fullText'] as String? ?? '',
    );
  }

  /// Performs OCR on PDF pages using Google ML Kit.
  ///
  /// Returns PDFOCRResult containing recognized text and confidence scores.
  /// Throws exception on error.
  @override
  Future<PDFOCRResult?> pdfOcr({PDFOCRParams? params}) async {
    final Map? result =
        await methodChannel.invokeMethod<Map?>('pdfOcr', params?.toJson());

    if (result == null) return null;

    final Map<int, OCRPageResult> pageResults = {};
    final Map<String, dynamic> pageResultsMap =
        result['pageResults'] as Map<String, dynamic>? ?? {};

    pageResultsMap.forEach((key, value) {
      if (value is Map) {
        final pageNum = int.parse(key);
        final pageResult = OCRPageResult(
          text: value['text'] as String? ?? '',
          confidence: (value['confidence'] as num?)?.toDouble() ?? 0.0,
        );
        pageResults[pageNum] = pageResult;
      }
    });

    return PDFOCRResult(
      pageResults: pageResults,
      fullText: result['fullText'] as String? ?? '',
    );
  }

  /// Adds a digital signature to PDF using certificate.
  ///
  /// Returns the path to the signed PDF file.
  /// Throws exception on error.
  @override
  Future<String?> pdfDigitalSignature(
      {PDFDigitalSignatureParams? params}) async {
    final String? result = await methodChannel.invokeMethod<String?>(
        'pdfDigitalSignature', params?.toJson());
    return result;
  }

  /// Adds annotations to PDF.
  ///
  /// Returns the path to the annotated PDF file.
  /// Throws exception on error.
  @override
  Future<String?> pdfAnnotations({PDFAnnotationsParams? params}) async {
    final String? result = await methodChannel.invokeMethod<String?>(
        'pdfAnnotations', params?.toJson());
    return result;
  }

  /// Fills PDF form fields with the provided values.
  ///
  /// Returns the path to the filled PDF file.
  /// Throws exception on error.
  @override
  Future<String?> fillFormFields({PDFFormFillParams? params}) async {
    final String? result = await methodChannel.invokeMethod<String?>(
        'fillFormFields', params?.toJson());
    return result;
  }

  /// Extracts PDF form field names, values, types, and options.
  ///
  /// Returns PDFFormFieldData containing field metadata keyed by field name.
  /// Throws exception on error.
  @override
  Future<PDFFormFieldData?> extractFormFieldData({
    PDFFormFieldDataParams? params,
  }) async {
    final Map? result = await methodChannel.invokeMethod<Map?>(
        'extractFormFieldData', params?.toJson());

    if (result == null) return null;

    return PDFFormFieldData.fromJson(Map<dynamic, dynamic>.from(result));
  }

  /// Reads PDF metadata (title, author, subject, keywords, etc.).
  ///
  /// Returns PDFMetadataResult containing metadata information.
  /// Throws exception on error.
  @override
  Future<PDFMetadataResult?> pdfMetadataReader({
    PDFMetadataReaderParams? params,
  }) async {
    final Map? result = await methodChannel.invokeMethod<Map?>(
        'pdfMetadataReader', params?.toJson());

    if (result == null) return null;

    return PDFMetadataResult.fromJson(Map<dynamic, dynamic>.from(result));
  }

  /// Updates PDF metadata (title, author, subject, keywords, etc.).
  ///
  /// Returns the path to the updated PDF file.
  /// Throws exception on error.
  @override
  Future<String?> pdfMetadataWriter({
    PDFMetadataWriterParams? params,
  }) async {
    final String? result = await methodChannel.invokeMethod<String?>(
        'pdfMetadataWriter', params?.toJson());
    return result;
  }

  /// Extracts PDF bookmarks/outlines (table of contents).
  ///
  /// Returns PDFBookmarkData containing hierarchical bookmark structure.
  /// Throws exception on error.
  @override
  Future<PDFBookmarkData?> pdfBookmarkReader({
    PDFBookmarkReaderParams? params,
  }) async {
    final Map? result = await methodChannel.invokeMethod<Map?>(
        'pdfBookmarkReader', params?.toJson());

    if (result == null) return null;

    return PDFBookmarkData.fromJson(Map<dynamic, dynamic>.from(result));
  }

  /// Creates or modifies PDF bookmarks/outlines.
  ///
  /// Returns the path to the updated PDF file.
  /// Throws exception on error.
  @override
  Future<String?> pdfBookmarkWriter({
    PDFBookmarkWriterParams? params,
  }) async {
    final String? result = await methodChannel.invokeMethod<String?>(
        'pdfBookmarkWriter', params?.toJson());
    return result;
  }

  /// Compares two PDFs and highlights differences.
  ///
  /// Returns PDFComparisonResult containing detailed comparison information.
  /// Throws exception on error.
  @override
  Future<PDFComparisonResult?> pdfComparison({
    PDFComparisonParams? params,
  }) async {
    final Map? result = await methodChannel.invokeMethod<Map?>(
        'pdfComparison', params?.toJson());

    if (result == null) return null;

    return PDFComparisonResult.fromJson(Map<dynamic, dynamic>.from(result));
  }

  /// Attempts to repair a corrupted or damaged PDF file.
  ///
  /// Returns PDFRepairResult containing repair status and recovered content.
  /// Throws exception on error.
  @override
  Future<PDFRepairResult?> pdfRepair({
    PDFRepairParams? params,
  }) async {
    final Map? result = await methodChannel.invokeMethod<Map?>(
        'pdfRepair', params?.toJson());

    if (result == null) return null;

    return PDFRepairResult.fromJson(Map<dynamic, dynamic>.from(result));
  }
}

/// Parameters for the [mergePDFs] method.
class PDFMergerParams {
  /// Provide paths of pdf files to merge.
  final List<String> pdfsPaths;

  /// Create parameters for the [mergePDFs] method.
  const PDFMergerParams({required this.pdfsPaths})
      : assert(pdfsPaths.length > 1, 'provide paths for at least 2 pdfs');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfsPaths': pdfsPaths,
    };
  }
}

class ExtractImageFromPDFParams {
  final String pdfPath;
  final String outputDir;

  ExtractImageFromPDFParams({required this.pdfPath, required this.outputDir});

  Map<String, dynamic> toJson() {
    return <String, dynamic>{'pdfPath': pdfPath, 'outputDir': outputDir};
  }
}

/// Parameters for the [splitPDF] method.
///
/// pageCount parameter with value 1 is used if no other parameter is provided except pdfPath.
class PDFSplitterParams {
  /// Provide path of pdf file to split.
  final String pdfPath;

  /// Provide the splitting page count.
  final int? pageCount;

  /// Provide the splitting byte size.
  ///
  /// It will give some pdf bigger than the byte size if the some individual pages in pdf are bigger than the byte size.
  final int? byteSize;

  /// Provide the splitting page numbers.
  final List<int>? pageNumbers;

  /// Provide the splitting page range list.
  final List<String>? pageRanges;

  /// Provide the splitting page range.
  final String? pageRange;

  /// Create parameters for the [splitPDF] method.
  const PDFSplitterParams(
      {required this.pdfPath,
      this.pageCount,
      this.byteSize,
      this.pageNumbers,
      this.pageRanges,
      this.pageRange})
      : assert(
            pageCount != null
                ? (byteSize == null &&
                    pageNumbers == null &&
                    pageRanges == null &&
                    pageRange == null)
                : byteSize != null
                    ? (pageCount == null &&
                        pageNumbers == null &&
                        pageRanges == null &&
                        pageRange == null)
                    : pageNumbers != null
                        ? (pageCount == null &&
                            byteSize == null &&
                            pageRanges == null &&
                            pageRange == null)
                        : pageRanges != null
                            ? (pageCount == null &&
                                byteSize == null &&
                                pageNumbers == null &&
                                pageRange == null)
                            : pageRange != null
                                ? (pageCount == null &&
                                    byteSize == null &&
                                    pageNumbers == null &&
                                    pageRanges == null)
                                : false,
            'Provide only anyone out of pageCount, byteSize, pageNumbers, pageRanges, pageRange');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pageCount': pageCount,
      'byteSize': byteSize,
      'pageNumbers': pageNumbers,
      'pageRanges': pageRanges,
      'pageRange': pageRange,
    };
  }
}

/// Parameters for the [pdfPageDeleter] method.
class PDFPageDeleterParams {
  /// Provide path of pdf files from which page should be deleted.
  final String pdfPath;

  /// Provide the page numbers to delete.
  final List<int> pageNumbers;

  /// Create parameters for the [pdfPageDeleter] method.
  const PDFPageDeleterParams({required this.pdfPath, required this.pageNumbers})
      : assert(
            pageNumbers.length > 0, 'provide at least 1 page number to delete');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pageNumbers': pageNumbers,
    };
  }
}

/// Parameters for the [pdfPageReorder] method.
class PDFPageReorderParams {
  /// Provide path of pdf files for which page should be reordered.
  final String pdfPath;

  /// Provide the reordered page numbers.
  final List<int> pageNumbers;

  /// Create parameters for the [pdfPageReorder] method.
  const PDFPageReorderParams({required this.pdfPath, required this.pageNumbers})
      : assert(pageNumbers.length > 0, 'pageNumbers cant be empty');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pageNumbers': pageNumbers,
    };
  }
}

class PageRotationInfo {
  final int pageNumber;
  final int rotationAngle;

  PageRotationInfo({
    required this.pageNumber,
    required this.rotationAngle,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pageNumber': pageNumber,
      'rotationAngle': rotationAngle,
    };
  }

  // Implement toString to make it easier to see information
  // when using the print statement.
  @override
  String toString() {
    return 'PageRotationInfo{pageNumber: $pageNumber, rotationAngle: $rotationAngle}';
  }
}

/// Parameters for the [pdfPageRotator] method.
class PDFPageRotatorParams {
  /// Provide path of pdf files for which page should be reordered.
  final String pdfPath;

  /// Provide the rotation info for pdf pages.
  final List<PageRotationInfo> pagesRotationInfo;

  /// Create parameters for the [pdfPageRotator] method.
  const PDFPageRotatorParams(
      {required this.pdfPath, required this.pagesRotationInfo})
      : assert(pagesRotationInfo.length > 0, 'pageNumbers cant be empty');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pagesRotationInfo': pagesRotationInfo.map((e) => e.toJson()).toList(),
    };
  }
}

/// Parameters for the [pdfPageRotator] method.
class PDFPageRotatorDeleterReorderParams {
  /// Provide path of pdf files for which page should be reordered.
  final String pdfPath;

  /// Provide the rotation info for pdf pages.
  final List<PageRotationInfo>? pagesRotationInfo;

  /// Provide the page numbers to delete.
  final List<int>? pageNumbersForDeleter;

  /// Provide the reordered page numbers.
  final List<int>? pageNumbersForReorder;

  /// Create parameters for the [pdfPageRotator] method.
  const PDFPageRotatorDeleterReorderParams(
      {required this.pdfPath,
      this.pagesRotationInfo,
      this.pageNumbersForDeleter,
      this.pageNumbersForReorder})
      : assert(
            (pagesRotationInfo != null && pagesRotationInfo.length > 0) ||
                (pageNumbersForDeleter != null &&
                    pageNumbersForDeleter.length > 0) ||
                (pageNumbersForReorder != null &&
                    pageNumbersForReorder.length > 0),
            'out of pagesRotationInfo, pageNumbersForDeleter, pageNumbersForReorder provide at least one non empty');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pagesRotationInfo': pagesRotationInfo?.map((e) => e.toJson()).toList(),
      'pageNumbersForDeleter': pageNumbersForDeleter,
      'pageNumbersForReorder': pageNumbersForReorder,
    };
  }
}

/// Parameters for the [pdfCompressor] method.
class PDFCompressorParams {
  /// Provide path of pdf file which should be compressed.
  final String pdfPath;

  /// Provide pdf page images quality greater than 0 and less tan or equal to 100.
  final int imageQuality;

  /// Provide pdf page images scale greater than 0 and less tan or equal to 5.
  final double imageScale;

  /// Provide true to unEmbed all fonts to decrease size further.
  final bool unEmbedFonts;

  /// Advanced compression options.
  final PDFAdvancedCompressionOptions? advancedOptions;

  /// Create parameters for the [pdfCompressor] method.
  const PDFCompressorParams({
    required this.pdfPath,
    required this.imageQuality,
    required this.imageScale,
    this.unEmbedFonts = false,
    this.advancedOptions,
  }) : assert(imageScale > 0 || imageScale <= 5,
            'imageScale should be greater than 0 and less tan or equal to 5'),
        assert(imageQuality > 0 || imageQuality <= 100,
            'imageQuality should be greater than 0 and less tan or equal to 100');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'imageQuality': imageQuality,
      'imageScale': imageScale,
      'unEmbedFonts': unEmbedFonts,
      'advancedOptions': advancedOptions?.toJson(),
    };
  }
}

/// Advanced compression options for PDF optimization.
class PDFAdvancedCompressionOptions {
  /// Enable font subsetting (embed only used characters).
  final bool enableFontSubsetting;

  /// Remove duplicate fonts across the document.
  final bool removeDuplicateFonts;

  /// Compress font streams more aggressively.
  final bool compressFonts;

  /// Remove unused objects and optimize PDF structure.
  final bool optimizeStructure;

  /// Remove unused metadata and non-essential information.
  final bool removeUnusedMetadata;

  /// Merge duplicate images to reduce file size.
  final bool deduplicateImages;

  /// Convert images to more efficient formats when possible.
  final bool optimizeImageFormats;

  /// Compress content streams more aggressively.
  final bool compressStreams;

  /// Flatten form fields to reduce complexity.
  final bool flattenFormFields;

  /// Remove unused named destinations and outlines.
  final bool cleanNamedDestinations;

  /// Create parameters for the [PDFAdvancedCompressionOptions] class.
  const PDFAdvancedCompressionOptions({
    this.enableFontSubsetting = false,
    this.removeDuplicateFonts = false,
    this.compressFonts = false,
    this.optimizeStructure = false,
    this.removeUnusedMetadata = false,
    this.deduplicateImages = false,
    this.optimizeImageFormats = false,
    this.compressStreams = false,
    this.flattenFormFields = false,
    this.cleanNamedDestinations = false,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'enableFontSubsetting': enableFontSubsetting,
      'removeDuplicateFonts': removeDuplicateFonts,
      'compressFonts': compressFonts,
      'optimizeStructure': optimizeStructure,
      'removeUnusedMetadata': removeUnusedMetadata,
      'deduplicateImages': deduplicateImages,
      'optimizeImageFormats': optimizeImageFormats,
      'compressStreams': compressStreams,
      'flattenFormFields': flattenFormFields,
      'cleanNamedDestinations': cleanNamedDestinations,
    };
  }
}

enum WatermarkLayer { underContent, overContent }

enum PositionType {
  topLeft,
  topCenter,
  topRight,
  centerLeft,
  center,
  centerRight,
  bottomLeft,
  bottomCenter,
  bottomRight,
  custom
}

enum ImageFormat { png, jpeg, webp }

/// Parameters for the [pdfWatermark] method.
class PDFWatermarkParams {
  /// Provide path of pdf file which should be compressed.
  final String pdfPath;

  /// Provide watermark text.
  final String text;

  /// Provide watermark text font size.
  final double fontSize;

  /// Provide layer for watermark printing like over or under content.
  final WatermarkLayer watermarkLayer;

  /// Provide watermark text opacity.
  final double opacity;

  /// Provide watermark text rotation Angle.
  final double rotationAngle;

  /// Provide watermark text color.
  final Color watermarkColor;

  /// Provide position of text.
  final PositionType positionType;

  /// Provide custom PositionType X coordinates list.
  final List<double>? customPositionXCoordinatesList;

  /// Provide custom PositionType Y coordinates list.
  final List<double>? customPositionYCoordinatesList;

  /// Create parameters for the [pdfWatermark] method.
  const PDFWatermarkParams({
    required this.pdfPath,
    required this.text,
    this.fontSize = 30,
    this.watermarkLayer = WatermarkLayer.overContent,
    this.opacity = 0.5,
    this.rotationAngle = 45,
    this.watermarkColor = Colors.black,
    this.positionType = PositionType.center,
    this.customPositionXCoordinatesList,
    this.customPositionYCoordinatesList,
  }) : assert(
            positionType == PositionType.custom
                ? (customPositionXCoordinatesList != null &&
                        customPositionXCoordinatesList.length != 0) &&
                    (customPositionYCoordinatesList != null &&
                        customPositionYCoordinatesList.length != 0)
                : true,
            'if positionType == PositionType.custom then customPositionXCoordinatesList and customPositionYCoordinatesList can\'t be null or empty');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'text': text,
      'fontSize': fontSize,
      'watermarkLayer': watermarkLayer.toString(),
      'opacity': opacity,
      'rotationAngle': rotationAngle,
      'watermarkColor': '#${watermarkColor.toARGB32().toRadixString(16)}',
      'positionType': positionType.toString(),
    };
  }
}

class PageSizeInfo {
  /// Pdf page number.
  final int pageNumber;

  /// Pdf page width.
  final double widthOfPage;

  /// Pdf page height.
  final double heightOfPage;

  PageSizeInfo({
    required this.pageNumber,
    required this.widthOfPage,
    required this.heightOfPage,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pageNumber': pageNumber,
      'widthOfPage': widthOfPage,
      'heightOfPage': heightOfPage,
    };
  }

  // Implement toString to make it easier to see information
  // when using the print statement.
  @override
  String toString() {
    return 'PageSizeInfo{pageNumber: $pageNumber, widthOfPage: $widthOfPage, heightOfPage: $heightOfPage}';
  }
}

/// Parameters for the [pdfPagesSize] method.
class PDFPagesSizeParams {
  /// Provide path of pdf file which you want pages size info.
  final String pdfPath;

  /// Create parameters for the [pdfPagesSize] method.
  const PDFPagesSizeParams({
    required this.pdfPath,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
    };
  }
}

class PdfValidityAndProtection {
  /// Is true if pdf is valid.
  final bool? isPDFValid;

  /// Is true if pdf is owner/permission password protected.
  final bool? isOwnerPasswordProtected;

  /// Is true if pdf is user/open password protected.
  final bool? isOpenPasswordProtected;

  /// Is true if pdf printing is allowed.
  final bool? isPrintingAllowed;

  /// Is true if pdf changes are allowed.
  final bool? isModifyContentsAllowed;

  PdfValidityAndProtection({
    required this.isPDFValid,
    required this.isOwnerPasswordProtected,
    required this.isOpenPasswordProtected,
    required this.isPrintingAllowed,
    required this.isModifyContentsAllowed,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'isPDFValid': isPDFValid,
      'isOwnerPasswordProtected': isOwnerPasswordProtected,
      'isOpenPasswordProtected': isOpenPasswordProtected,
      'isPrintingAllowed': isPrintingAllowed,
      'isModifyContentsAllowed': isModifyContentsAllowed,
    };
  }

  // Implement toString to make it easier to see information
  // when using the print statement.
  @override
  String toString() {
    return 'PdfValidityAndProtection{isPDFValid: $isPDFValid, isOwnerPasswordProtected: $isOwnerPasswordProtected, isOpenPasswordProtected: $isOpenPasswordProtected, isPrintingAllowed: $isPrintingAllowed, isModifyContentsAllowed: $isModifyContentsAllowed}';
  }
}

/// Parameters for the [pdfValidityAndProtection] method.
class PDFValidityAndProtectionParams {
  /// Provide path of pdf file which you want validity and protection info.
  final String pdfPath;

  /// Provide owner or user password.
  final String? password;

  /// Create parameters for the [pdfValidityAndProtection] method.
  const PDFValidityAndProtectionParams({
    required this.pdfPath,
    this.password = "",
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'password': password,
    };
  }
}

/// Parameters for the [pdfDecryption] method.
class PDFDecryptionParams {
  /// Provide path of pdf file which you want decrypted.
  final String pdfPath;

  /// Provide owner or user password.
  final String? password;

  /// Create parameters for the [pdfDecryption] method.
  const PDFDecryptionParams({
    required this.pdfPath,
    this.password = "",
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'password': password,
    };
  }
}

/// Parameters for the [pdfEncryption] method.
class PDFEncryptionParams {
  /// Provide path of pdf file which you want encrypted.
  final String pdfPath;

  /// Provide owner password.
  final String ownerPassword;

  /// Provide user password.
  final String userPassword;

  /// Set true to allow printing permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowPrinting;

  /// Set true to allow modify permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowModifyContents;

  /// Set true to allow copy permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowCopy;

  /// Set true to allow modifying annotations permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowModifyAnnotations;

  /// Set true to allow fill in permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowFillIn;

  /// Set true to allow screen readers permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowScreenReaders;

  /// Set true to allow assembly permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowAssembly;

  /// Set true to allow degraded printing permission.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool allowDegradedPrinting;

  /// Set true to enable StandardEncryptionAES40 encryption. standardEncryptionAES40 implicitly sets doNotEncryptMetadata and encryptEmbeddedFilesOnly as false.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool standardEncryptionAES40;

  /// Set true to enable StandardEncryptionAES128 encryption. standardEncryptionAES128 implicitly sets EncryptionConstants.EMBEDDED_FILES_ONLY as false.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool standardEncryptionAES128;

  /// Set true to enable encryptionAES128 encryption.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool encryptionAES128;

  /// Set true to enable encryptionAES256 encryption.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool encryptionAES256;

  /// Set true to encrypt embedded files only.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool encryptEmbeddedFilesOnly;

  /// Set true to not encrypt metadata.
  ///
  /// Please be aware that the passed encryption types may override permissions.
  final bool doNotEncryptMetadata;

  /// Create parameters for the [pdfEncryption] method.
  const PDFEncryptionParams({
    required this.pdfPath,
    this.ownerPassword = "",
    this.userPassword = "",
    this.allowPrinting = false,
    this.allowModifyContents = false,
    this.allowCopy = false,
    this.allowModifyAnnotations = false,
    this.allowFillIn = false,
    this.allowScreenReaders = false,
    this.allowAssembly = false,
    this.allowDegradedPrinting = false,
    this.standardEncryptionAES40 = false,
    this.standardEncryptionAES128 = false,
    this.encryptionAES128 = false,
    this.encryptionAES256 = false,
    this.encryptEmbeddedFilesOnly = false,
    this.doNotEncryptMetadata = false,
  }) : assert(
            standardEncryptionAES40 == true
                ? (standardEncryptionAES128 == false &&
                    encryptionAES128 == false &&
                    encryptionAES256 == false)
                : standardEncryptionAES128 == true
                    ? (standardEncryptionAES40 == false &&
                        encryptionAES128 == false &&
                        encryptionAES256 == false)
                    : encryptionAES128 == true
                        ? (standardEncryptionAES40 == false &&
                            standardEncryptionAES128 == false &&
                            encryptionAES256 == false)
                        : encryptionAES256 == true
                            ? (standardEncryptionAES40 == false &&
                                standardEncryptionAES128 == false &&
                                encryptionAES128 == false)
                            : false,
            'Set only anyone encryption out of standardEncryptionAES40, standardEncryptionAES128, encryptionAES128, encryptionAES256 true');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'ownerPassword': ownerPassword,
      'userPassword': userPassword,
      'allowPrinting': allowPrinting,
      'allowModifyContents': allowModifyContents,
      'allowCopy': allowCopy,
      'allowModifyAnnotations': allowModifyAnnotations,
      'allowFillIn': allowFillIn,
      'allowScreenReaders': allowScreenReaders,
      'allowAssembly': allowAssembly,
      'allowDegradedPrinting': allowDegradedPrinting,
      'standardEncryptionAES40': standardEncryptionAES40,
      'standardEncryptionAES128': standardEncryptionAES128,
      'encryptionAES128': encryptionAES128,
      'encryptEmbeddedFilesOnly': encryptEmbeddedFilesOnly,
      'doNotEncryptMetadata': doNotEncryptMetadata,
    };
  }
}

/// Parameters for the [imagesToPdfs] method.
class ImagesToPDFsParams {
  /// Provide paths of images to convert to pdfs.
  final List<String> imagesPaths;

  /// Set createSinglePdf = true to pull all images in single pdf.
  final bool createSinglePdf;

  /// Create parameters for the [imagesToPdfs] method.
  const ImagesToPDFsParams(
      {required this.imagesPaths, this.createSinglePdf = false})
      : assert(imagesPaths.length > 0, 'provide path for at least 1 image');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'imagesPaths': imagesPaths,
      'createSinglePdf': createSinglePdf,
    };
  }
}

/// Parameters for the [pdfToImages] method.
class PDFToImagesParams {
  /// Provide path of pdf file to convert to images.
  final String pdfPath;

  /// Provide the page numbers to convert. If empty, all pages will be converted.
  final List<int>? pages;

  /// Provide the image format. Default is PNG.
  final ImageFormat imageFormat;

  /// Provide image quality for JPEG format (1-100). Default is 90.
  final int? quality;

  /// Provide scale factor for image size (0.1-5.0). Default is 1.0.
  final double? scale;

  /// Create parameters for the [pdfToImages] method.
  const PDFToImagesParams({
    required this.pdfPath,
    this.pages,
    this.imageFormat = ImageFormat.png,
    this.quality = 90,
    this.scale = 1.0,
  })  : assert(scale == null || (scale >= 0.1 && scale <= 5.0),
            'scale should be between 0.1 and 5.0'),
        assert(quality == null || (quality >= 1 && quality <= 100),
            'quality should be between 1 and 100');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pages': pages,
      'imageFormat': imageFormat.toString(),
      'quality': quality,
      'scale': scale,
    };
  }
}

/// Parameters for the [pdfTextExtraction] method.
class PDFTextExtractionParams {
  /// Provide path of pdf file to extract text from.
  final String pdfPath;

  /// Provide the page numbers to extract text from. If empty, all pages will be processed.
  final List<int>? pages;

  /// Create parameters for the [pdfTextExtraction] method.
  const PDFTextExtractionParams({
    required this.pdfPath,
    this.pages,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pages': pages,
    };
  }
}

/// Result class for text extraction containing page-wise text.
class PDFTextExtractionResult {
  /// Map of page numbers to extracted text content.
  final Map<int, String> pageTexts;

  /// Full text content from all pages concatenated.
  final String fullText;

  PDFTextExtractionResult({
    required this.pageTexts,
    required this.fullText,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pageTexts': pageTexts,
      'fullText': fullText,
    };
  }
}

/// Parameters for the [fillFormFields] method.
class PDFFormFillParams {
  /// Provide path of pdf file containing form fields.
  final String pdfPath;

  /// Field values keyed by PDF form field name.
  final Map<String, dynamic> fieldValues;

  /// Set true to flatten fields after filling.
  final bool flatten;

  /// Create parameters for the [fillFormFields] method.
  const PDFFormFillParams({
    required this.pdfPath,
    required this.fieldValues,
    this.flatten = false,
  }) : assert(fieldValues.length > 0, 'provide at least 1 form field value');

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'fieldValues': fieldValues,
      'flatten': flatten,
    };
  }
}

/// Parameters for the [extractFormFieldData] method.
class PDFFormFieldDataParams {
  /// Provide path of pdf file containing form fields.
  final String pdfPath;

  /// Create parameters for the [extractFormFieldData] method.
  const PDFFormFieldDataParams({
    required this.pdfPath,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
    };
  }
}

/// Extracted PDF form field data.
class PDFFormFieldData {
  /// Form fields keyed by field name.
  final Map<String, PDFFormField> fields;

  PDFFormFieldData({
    required this.fields,
  });

  factory PDFFormFieldData.fromJson(Map<dynamic, dynamic> json) {
    final fieldsJson = json['fields'] as Map<dynamic, dynamic>? ?? {};
    final fields = <String, PDFFormField>{};

    fieldsJson.forEach((key, value) {
      if (value is Map) {
        fields[key.toString()] =
            PDFFormField.fromJson(Map<dynamic, dynamic>.from(value));
      }
    });

    return PDFFormFieldData(fields: fields);
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'fields': fields.map((key, value) => MapEntry(key, value.toJson())),
    };
  }
}

/// PDF form field metadata.
class PDFFormField {
  /// Field name used when filling this field.
  final String name;

  /// Current field value.
  final String value;

  /// Field type as a readable string.
  final String type;

  /// Available export/display options for choice, checkbox, and radio fields.
  final List<String> options;

  /// Whether the field is marked required in the PDF.
  final bool isRequired;

  PDFFormField({
    required this.name,
    required this.value,
    required this.type,
    this.options = const [],
    this.isRequired = false,
  });

  factory PDFFormField.fromJson(Map<dynamic, dynamic> json) {
    return PDFFormField(
      name: json['name'] as String? ?? '',
      value: json['value'] as String? ?? '',
      type: json['type'] as String? ?? 'unknown',
      options: (json['options'] as List?)?.map((e) => e.toString()).toList() ??
          const [],
      isRequired: json['isRequired'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'name': name,
      'value': value,
      'type': type,
      'options': options,
      'isRequired': isRequired,
    };
  }

  @override
  String toString() {
    return 'PDFFormField{name: $name, value: $value, type: $type, options: $options, isRequired: $isRequired}';
  }
}

/// Parameters for the [pdfOcr] method.
class PDFOCRParams {
  /// Provide path of pdf file to perform OCR on.
  final String pdfPath;

  /// Provide the page numbers to perform OCR on. If empty, all pages will be processed.
  final List<int>? pages;

  /// Provide the language code for OCR (e.g., 'en', 'es', 'fr'). Default is 'en'.
  final String languageCode;

  /// Create parameters for the [pdfOcr] method.
  const PDFOCRParams({
    required this.pdfPath,
    this.pages,
    this.languageCode = 'en',
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'pages': pages,
      'languageCode': languageCode,
    };
  }
}

/// Result class for OCR containing recognized text and confidence.
class PDFOCRResult {
  /// Map of page numbers to OCR text results.
  final Map<int, OCRPageResult> pageResults;

  /// Full OCR text content from all pages concatenated.
  final String fullText;

  PDFOCRResult({
    required this.pageResults,
    required this.fullText,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pageResults': pageResults
          .map((key, value) => MapEntry(key.toString(), value.toJson())),
      'fullText': fullText,
    };
  }
}

/// OCR result for a single page.
class OCRPageResult {
  /// Recognized text from the page.
  final String text;

  /// Confidence score of the OCR recognition (0.0 to 1.0).
  final double confidence;

  OCRPageResult({
    required this.text,
    required this.confidence,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'text': text,
      'confidence': confidence,
    };
  }
}

/// Signature appearance options.
class SignatureAppearance {
  /// Text to display in the signature field.
  final String? text;

  /// Font size for the signature text.
  final double fontSize;

  /// X coordinate of the signature field (in points).
  final double x;

  /// Y coordinate of the signature field (in points).
  final double y;

  /// Width of the signature field (in points).
  final double width;

  /// Height of the signature field (in points).
  final double height;

  /// Page number to place the signature on.
  final int pageNumber;

  SignatureAppearance({
    this.text,
    this.fontSize = 12.0,
    required this.x,
    required this.y,
    required this.width,
    required this.height,
    this.pageNumber = 1,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'text': text,
      'fontSize': fontSize,
      'x': x,
      'y': y,
      'width': width,
      'height': height,
      'pageNumber': pageNumber,
    };
  }
}

/// Parameters for the [pdfDigitalSignature] method.
class PDFDigitalSignatureParams {
  /// Provide path of pdf file to sign.
  final String pdfPath;

  /// Provide path to the certificate file (.p12 or .pfx).
  final String certificatePath;

  /// Provide the password for the certificate.
  final String certificatePassword;

  /// Provide the reason for signing.
  final String? reason;

  /// Provide the location of signing.
  final String? location;

  /// Provide the contact information.
  final String? contact;

  /// Provide signature appearance options.
  final SignatureAppearance? appearance;

  /// Create parameters for the [pdfDigitalSignature] method.
  const PDFDigitalSignatureParams({
    required this.pdfPath,
    required this.certificatePath,
    required this.certificatePassword,
    this.reason,
    this.location,
    this.contact,
    this.appearance,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'certificatePath': certificatePath,
      'certificatePassword': certificatePassword,
      'reason': reason,
      'location': location,
      'contact': contact,
      'appearance': appearance?.toJson(),
    };
  }
}

/// Base class for PDF annotations.
abstract class PDFAnnotation {
  /// Page number where the annotation appears.
  final int pageNumber;

  /// Rectangle defining the annotation's position and size (x, y, width, height).
  final List<double> rect;

  /// Optional title/author of the annotation.
  final String? title;

  /// Optional contents/text of the annotation.
  final String? contents;

  /// Optional color of the annotation (RGB values 0-1).
  final List<double>? color;

  PDFAnnotation({
    required this.pageNumber,
    required this.rect,
    this.title,
    this.contents,
    this.color,
  });

  Map<String, dynamic> toJson();
}

/// Text annotation (sticky note).
class TextAnnotation extends PDFAnnotation {
  /// Icon name for the annotation ('Comment', 'Help', 'Insert', 'Key', 'NewParagraph', 'Note', 'Paragraph').
  final String? iconName;

  /// Whether the annotation should be initially open.
  final bool isOpen;

  TextAnnotation({
    required super.pageNumber,
    required super.rect,
    super.title,
    super.contents,
    super.color,
    this.iconName,
    this.isOpen = false,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'text',
      'pageNumber': pageNumber,
      'rect': rect,
      'title': title,
      'contents': contents,
      'color': color,
      'iconName': iconName,
      'isOpen': isOpen,
    };
  }
}

/// Highlight annotation.
class HighlightAnnotation extends PDFAnnotation {
  /// List of quadrilaterals defining the highlighted area.
  final List<List<double>> quads;

  HighlightAnnotation({
    required super.pageNumber,
    required super.rect,
    required this.quads,
    super.title,
    super.contents,
    super.color,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'highlight',
      'pageNumber': pageNumber,
      'rect': rect,
      'quads': quads,
      'title': title,
      'contents': contents,
      'color': color,
    };
  }
}

/// Underline annotation.
class UnderlineAnnotation extends PDFAnnotation {
  /// List of quadrilaterals defining the underlined area.
  final List<List<double>> quads;

  UnderlineAnnotation({
    required super.pageNumber,
    required super.rect,
    required this.quads,
    super.title,
    super.contents,
    super.color,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'underline',
      'pageNumber': pageNumber,
      'rect': rect,
      'quads': quads,
      'title': title,
      'contents': contents,
      'color': color,
    };
  }
}

/// Strike-through annotation.
class StrikeThroughAnnotation extends PDFAnnotation {
  /// List of quadrilaterals defining the strike-through area.
  final List<List<double>> quads;

  StrikeThroughAnnotation({
    required super.pageNumber,
    required super.rect,
    required this.quads,
    super.title,
    super.contents,
    super.color,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'strikeThrough',
      'pageNumber': pageNumber,
      'rect': rect,
      'quads': quads,
      'title': title,
      'contents': contents,
      'color': color,
    };
  }
}

/// Squiggly underline annotation.
class SquigglyAnnotation extends PDFAnnotation {
  /// List of quadrilaterals defining the squiggly area.
  final List<List<double>> quads;

  SquigglyAnnotation({
    required super.pageNumber,
    required super.rect,
    required this.quads,
    super.title,
    super.contents,
    super.color,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'squiggly',
      'pageNumber': pageNumber,
      'rect': rect,
      'quads': quads,
      'title': title,
      'contents': contents,
      'color': color,
    };
  }
}

/// Link annotation.
class LinkAnnotation extends PDFAnnotation {
  /// URL or named destination for the link.
  final String url;

  LinkAnnotation({
    required super.pageNumber,
    required super.rect,
    required this.url,
    super.title,
    super.contents,
    super.color,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'link',
      'pageNumber': pageNumber,
      'rect': rect,
      'url': url,
      'title': title,
      'contents': contents,
      'color': color,
    };
  }
}

/// Ink annotation (freehand drawing).
class InkAnnotation extends PDFAnnotation {
  /// List of ink strokes, each stroke is a list of points [x1,y1,x2,y2,...].
  final List<List<double>> inkList;

  InkAnnotation({
    required super.pageNumber,
    required super.rect,
    required this.inkList,
    super.title,
    super.contents,
    super.color,
  });

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': 'ink',
      'pageNumber': pageNumber,
      'rect': rect,
      'inkList': inkList,
      'title': title,
      'contents': contents,
      'color': color,
    };
  }
}

/// Parameters for the [pdfAnnotations] method.
class PDFAnnotationsParams {
  /// Provide path of pdf file to annotate.
  final String pdfPath;

  /// List of annotations to add to the PDF.
  final List<PDFAnnotation> annotations;

  /// Create parameters for the [pdfAnnotations] method.
  const PDFAnnotationsParams({
    required this.pdfPath,
    required this.annotations,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'annotations': annotations.map((a) => a.toJson()).toList(),
    };
  }
}

/// Parameters for the [pdfMetadataReader] method.
class PDFMetadataReaderParams {
  /// Provide path of pdf file to read metadata from.
  final String pdfPath;

  /// Create parameters for the [pdfMetadataReader] method.
  const PDFMetadataReaderParams({
    required this.pdfPath,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
    };
  }
}

/// Result class for PDF metadata information.
class PDFMetadataResult {
  /// PDF title.
  final String? title;

  /// PDF author.
  final String? author;

  /// PDF subject.
  final String? subject;

  /// PDF keywords.
  final String? keywords;

  /// PDF creator.
  final String? creator;

  /// PDF producer.
  final String? producer;

  /// PDF creation date (ISO 8601 format).
  final String? creationDate;

  /// PDF modification date (ISO 8601 format).
  final String? modificationDate;

  PDFMetadataResult({
    this.title,
    this.author,
    this.subject,
    this.keywords,
    this.creator,
    this.producer,
    this.creationDate,
    this.modificationDate,
  });

  factory PDFMetadataResult.fromJson(Map<dynamic, dynamic> json) {
    return PDFMetadataResult(
      title: json['title'] as String?,
      author: json['author'] as String?,
      subject: json['subject'] as String?,
      keywords: json['keywords'] as String?,
      creator: json['creator'] as String?,
      producer: json['producer'] as String?,
      creationDate: json['creationDate'] as String?,
      modificationDate: json['modificationDate'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'title': title,
      'author': author,
      'subject': subject,
      'keywords': keywords,
      'creator': creator,
      'producer': producer,
      'creationDate': creationDate,
      'modificationDate': modificationDate,
    };
  }
}

/// Parameters for the [pdfMetadataWriter] method.
class PDFMetadataWriterParams {
  /// Provide path of pdf file to update metadata.
  final String pdfPath;

  /// PDF title to set (optional).
  final String? title;

  /// PDF author to set (optional).
  final String? author;

  /// PDF subject to set (optional).
  final String? subject;

  /// PDF keywords to set (optional).
  final String? keywords;

  /// PDF creator to set (optional).
  final String? creator;

  /// PDF producer to set (optional).
  final String? producer;

  /// PDF creation date to set (ISO 8601 format, optional).
  final String? creationDate;

  /// PDF modification date to set (ISO 8601 format, optional).
  final String? modificationDate;

  /// Create parameters for the [pdfMetadataWriter] method.
  const PDFMetadataWriterParams({
    required this.pdfPath,
    this.title,
    this.author,
    this.subject,
    this.keywords,
    this.creator,
    this.producer,
    this.creationDate,
    this.modificationDate,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'title': title,
      'author': author,
      'subject': subject,
      'keywords': keywords,
      'creator': creator,
      'producer': producer,
      'creationDate': creationDate,
      'modificationDate': modificationDate,
    };
  }
}

/// Parameters for the [pdfBookmarkReader] method.
class PDFBookmarkReaderParams {
  /// Provide path of pdf file to read bookmarks from.
  final String pdfPath;

  /// Create parameters for the [pdfBookmarkReader] method.
  const PDFBookmarkReaderParams({
    required this.pdfPath,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
    };
  }
}

/// PDF bookmark entry containing title, page, and child bookmarks.
class PDFBookmark {
  /// Bookmark title/text.
  final String title;

  /// Page number where the bookmark points (1-indexed).
  final int? pageNumber;

  /// Named destination or page reference.
  final String? page;

  /// X coordinate for page position.
  final double? x;

  /// Y coordinate for page position.
  final double? y;

  /// Zoom level for page view.
  final double? zoom;

  /// Child bookmarks (for hierarchical structure).
  final List<PDFBookmark> children;

  PDFBookmark({
    required this.title,
    this.pageNumber,
    this.page,
    this.x,
    this.y,
    this.zoom,
    this.children = const [],
  });

  factory PDFBookmark.fromJson(Map<dynamic, dynamic> json) {
    final childrenJson = json['children'] as List? ?? [];
    final children = childrenJson.map((child) =>
        PDFBookmark.fromJson(Map<dynamic, dynamic>.from(child))).toList();

    return PDFBookmark(
      title: json['title'] as String? ?? '',
      pageNumber: json['pageNumber'] as int?,
      page: json['page'] as String?,
      x: (json['x'] as num?)?.toDouble(),
      y: (json['y'] as num?)?.toDouble(),
      zoom: (json['zoom'] as num?)?.toDouble(),
      children: children,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'title': title,
      'pageNumber': pageNumber,
      'page': page,
      'x': x,
      'y': y,
      'zoom': zoom,
      'children': children.map((child) => child.toJson()).toList(),
    };
  }
}

/// Extracted PDF bookmark data.
class PDFBookmarkData {
  /// Root level bookmarks.
  final List<PDFBookmark> bookmarks;

  PDFBookmarkData({
    required this.bookmarks,
  });

  factory PDFBookmarkData.fromJson(Map<dynamic, dynamic> json) {
    final bookmarksJson = json['bookmarks'] as List? ?? [];
    final bookmarks = bookmarksJson.map((bookmark) =>
        PDFBookmark.fromJson(Map<dynamic, dynamic>.from(bookmark))).toList();

    return PDFBookmarkData(bookmarks: bookmarks);
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'bookmarks': bookmarks.map((bookmark) => bookmark.toJson()).toList(),
    };
  }
}

/// Parameters for the [pdfBookmarkWriter] method.
class PDFBookmarkWriterParams {
  /// Provide path of pdf file to add/modify bookmarks.
  final String pdfPath;

  /// List of bookmarks to add to the PDF.
  final List<PDFBookmark> bookmarks;

  /// Create parameters for the [pdfBookmarkWriter] method.
  const PDFBookmarkWriterParams({
    required this.pdfPath,
    required this.bookmarks,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
      'bookmarks': bookmarks.map((b) => b.toJson()).toList(),
    };
  }
}

/// Parameters for the [pdfComparison] method.
class PDFComparisonParams {
  /// Provide path of first PDF file to compare.
  final String pdfPath1;

  /// Provide path of second PDF file to compare.
  final String pdfPath2;

  /// Set true to compare text content.
  final bool compareText;

  /// Set true to compare metadata.
  final bool compareMetadata;

  /// Set true to compare page count and basic structure.
  final bool compareStructure;

  /// Create parameters for the [pdfComparison] method.
  const PDFComparisonParams({
    required this.pdfPath1,
    required this.pdfPath2,
    this.compareText = true,
    this.compareMetadata = true,
    this.compareStructure = true,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath1': pdfPath1,
      'pdfPath2': pdfPath2,
      'compareText': compareText,
      'compareMetadata': compareMetadata,
      'compareStructure': compareStructure,
    };
  }
}

/// Result of text comparison between two PDFs.
class PDFTextComparison {
  /// Text content from first PDF.
  final String text1;

  /// Text content from second PDF.
  final String text2;

  /// Similarity score (0.0 to 1.0).
  final double similarity;

  /// List of text differences.
  final List<TextDifference> differences;

  PDFTextComparison({
    required this.text1,
    required this.text2,
    required this.similarity,
    required this.differences,
  });

  factory PDFTextComparison.fromJson(Map<dynamic, dynamic> json) {
    final differencesJson = json['differences'] as List? ?? [];
    final differences = differencesJson.map((diff) =>
        TextDifference.fromJson(Map<dynamic, dynamic>.from(diff))).toList();

    return PDFTextComparison(
      text1: json['text1'] as String? ?? '',
      text2: json['text2'] as String? ?? '',
      similarity: (json['similarity'] as num?)?.toDouble() ?? 0.0,
      differences: differences,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'text1': text1,
      'text2': text2,
      'similarity': similarity,
      'differences': differences.map((diff) => diff.toJson()).toList(),
    };
  }
}

/// Represents a text difference between two PDFs.
class TextDifference {
  /// Type of difference ('added', 'removed', 'modified').
  final String type;

  /// Position in first PDF text.
  final int position1;

  /// Position in second PDF text.
  final int position2;

  /// Length of the difference.
  final int length;

  /// The differing text content.
  final String content;

  TextDifference({
    required this.type,
    required this.position1,
    required this.position2,
    required this.length,
    required this.content,
  });

  factory TextDifference.fromJson(Map<dynamic, dynamic> json) {
    return TextDifference(
      type: json['type'] as String? ?? 'modified',
      position1: json['position1'] as int? ?? 0,
      position2: json['position2'] as int? ?? 0,
      length: json['length'] as int? ?? 0,
      content: json['content'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': type,
      'position1': position1,
      'position2': position2,
      'length': length,
      'content': content,
    };
  }
}

/// Result of metadata comparison between two PDFs.
class PDFMetadataComparison {
  /// Metadata from first PDF.
  final PDFMetadataResult metadata1;

  /// Metadata from second PDF.
  final PDFMetadataResult metadata2;

  /// List of metadata differences.
  final List<MetadataDifference> differences;

  PDFMetadataComparison({
    required this.metadata1,
    required this.metadata2,
    required this.differences,
  });

  factory PDFMetadataComparison.fromJson(Map<dynamic, dynamic> json) {
    final differencesJson = json['differences'] as List? ?? [];
    final differences = differencesJson.map((diff) =>
        MetadataDifference.fromJson(Map<dynamic, dynamic>.from(diff))).toList();

    return PDFMetadataComparison(
      metadata1: PDFMetadataResult.fromJson(Map<dynamic, dynamic>.from(json['metadata1'])),
      metadata2: PDFMetadataResult.fromJson(Map<dynamic, dynamic>.from(json['metadata2'])),
      differences: differences,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'metadata1': metadata1.toJson(),
      'metadata2': metadata2.toJson(),
      'differences': differences.map((diff) => diff.toJson()).toList(),
    };
  }
}

/// Represents a metadata difference between two PDFs.
class MetadataDifference {
  /// Metadata field name.
  final String field;

  /// Value in first PDF.
  final String? value1;

  /// Value in second PDF.
  final String? value2;

  MetadataDifference({
    required this.field,
    this.value1,
    this.value2,
  });

  factory MetadataDifference.fromJson(Map<dynamic, dynamic> json) {
    return MetadataDifference(
      field: json['field'] as String? ?? '',
      value1: json['value1'] as String?,
      value2: json['value2'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'field': field,
      'value1': value1,
      'value2': value2,
    };
  }
}

/// Result of structure comparison between two PDFs.
class PDFStructureComparison {
  /// Page count of first PDF.
  final int pageCount1;

  /// Page count of second PDF.
  final int pageCount2;

  /// Whether page counts are equal.
  final bool pageCountEqual;

  /// List of structural differences.
  final List<String> differences;

  PDFStructureComparison({
    required this.pageCount1,
    required this.pageCount2,
    required this.pageCountEqual,
    required this.differences,
  });

  factory PDFStructureComparison.fromJson(Map<dynamic, dynamic> json) {
    final differencesJson = json['differences'] as List? ?? [];
    final differences = differencesJson.map((diff) => diff.toString()).toList();

    return PDFStructureComparison(
      pageCount1: json['pageCount1'] as int? ?? 0,
      pageCount2: json['pageCount2'] as int? ?? 0,
      pageCountEqual: json['pageCountEqual'] as bool? ?? false,
      differences: differences,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pageCount1': pageCount1,
      'pageCount2': pageCount2,
      'pageCountEqual': pageCountEqual,
      'differences': differences,
    };
  }
}

/// Overall result of PDF comparison.
class PDFComparisonResult {
  /// Text comparison results (if requested).
  final PDFTextComparison? textComparison;

  /// Metadata comparison results (if requested).
  final PDFMetadataComparison? metadataComparison;

  /// Structure comparison results (if requested).
  final PDFStructureComparison? structureComparison;

  /// Overall similarity score (0.0 to 1.0).
  final double overallSimilarity;

  /// Summary of all differences found.
  final List<String> summary;

  PDFComparisonResult({
    this.textComparison,
    this.metadataComparison,
    this.structureComparison,
    required this.overallSimilarity,
    required this.summary,
  });

  factory PDFComparisonResult.fromJson(Map<dynamic, dynamic> json) {
    return PDFComparisonResult(
      textComparison: json['textComparison'] != null
          ? PDFTextComparison.fromJson(Map<dynamic, dynamic>.from(json['textComparison']))
          : null,
      metadataComparison: json['metadataComparison'] != null
          ? PDFMetadataComparison.fromJson(Map<dynamic, dynamic>.from(json['metadataComparison']))
          : null,
      structureComparison: json['structureComparison'] != null
          ? PDFStructureComparison.fromJson(Map<dynamic, dynamic>.from(json['structureComparison']))
          : null,
      overallSimilarity: (json['overallSimilarity'] as num?)?.toDouble() ?? 0.0,
      summary: (json['summary'] as List?)?.map((s) => s.toString()).toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'textComparison': textComparison?.toJson(),
      'metadataComparison': metadataComparison?.toJson(),
      'structureComparison': structureComparison?.toJson(),
      'overallSimilarity': overallSimilarity,
      'summary': summary,
    };
  }
}

/// Parameters for the [pdfRepair] method.
class PDFRepairParams {
  /// Provide path of potentially corrupted PDF file to repair.
  final String pdfPath;

  /// Create parameters for the [pdfRepair] method.
  const PDFRepairParams({
    required this.pdfPath,
  });

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pdfPath': pdfPath,
    };
  }
}

/// Result of PDF repair operation.
class PDFRepairResult {
  /// Whether the PDF was successfully repaired.
  final bool wasRepaired;

  /// Path to the repaired PDF file (if repair was successful).
  final String? repairedPdfPath;

  /// Original PDF corruption status.
  final PDFCorruptionStatus originalStatus;

  /// Repair operation status.
  final PDFRepairStatus repairStatus;

  /// Issues found during repair attempt.
  final List<String> issues;

  /// Information about recovered content.
  final PDFRecoveredContent? recoveredContent;

  PDFRepairResult({
    required this.wasRepaired,
    this.repairedPdfPath,
    required this.originalStatus,
    required this.repairStatus,
    required this.issues,
    this.recoveredContent,
  });

  factory PDFRepairResult.fromJson(Map<dynamic, dynamic> json) {
    return PDFRepairResult(
      wasRepaired: json['wasRepaired'] as bool? ?? false,
      repairedPdfPath: json['repairedPdfPath'] as String?,
      originalStatus: PDFCorruptionStatus.fromJson(Map<dynamic, dynamic>.from(json['originalStatus'])),
      repairStatus: PDFRepairStatus.fromJson(Map<dynamic, dynamic>.from(json['repairStatus'])),
      issues: (json['issues'] as List?)?.map((issue) => issue.toString()).toList() ?? [],
      recoveredContent: json['recoveredContent'] != null
          ? PDFRecoveredContent.fromJson(Map<dynamic, dynamic>.from(json['recoveredContent']))
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'wasRepaired': wasRepaired,
      'repairedPdfPath': repairedPdfPath,
      'originalStatus': originalStatus.toJson(),
      'repairStatus': repairStatus.toJson(),
      'issues': issues,
      'recoveredContent': recoveredContent?.toJson(),
    };
  }
}

/// Status of PDF corruption analysis.
class PDFCorruptionStatus {
  /// Whether the PDF can be opened.
  final bool canOpen;

  /// Whether the PDF has valid structure.
  final bool hasValidStructure;

  /// Whether the PDF has readable content.
  final bool hasReadableContent;

  /// Severity level of corruption (0.0 = no corruption, 1.0 = completely corrupted).
  final double corruptionLevel;

  /// List of detected corruption issues.
  final List<String> detectedIssues;

  PDFCorruptionStatus({
    required this.canOpen,
    required this.hasValidStructure,
    required this.hasReadableContent,
    required this.corruptionLevel,
    required this.detectedIssues,
  });

  factory PDFCorruptionStatus.fromJson(Map<dynamic, dynamic> json) {
    return PDFCorruptionStatus(
      canOpen: json['canOpen'] as bool? ?? false,
      hasValidStructure: json['hasValidStructure'] as bool? ?? false,
      hasReadableContent: json['hasReadableContent'] as bool? ?? false,
      corruptionLevel: (json['corruptionLevel'] as num?)?.toDouble() ?? 0.0,
      detectedIssues: (json['detectedIssues'] as List?)?.map((issue) => issue.toString()).toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'canOpen': canOpen,
      'hasValidStructure': hasValidStructure,
      'hasReadableContent': hasReadableContent,
      'corruptionLevel': corruptionLevel,
      'detectedIssues': detectedIssues,
    };
  }
}

/// Status of PDF repair operation.
class PDFRepairStatus {
  /// Whether repair operation completed successfully.
  final bool completed;

  /// Whether any content was recovered.
  final bool contentRecovered;

  /// Whether the repaired PDF is fully functional.
  final bool fullyFunctional;

  /// Repair method used.
  final String repairMethod;

  /// Additional repair information.
  final List<String> repairInfo;

  PDFRepairStatus({
    required this.completed,
    required this.contentRecovered,
    required this.fullyFunctional,
    required this.repairMethod,
    required this.repairInfo,
  });

  factory PDFRepairStatus.fromJson(Map<dynamic, dynamic> json) {
    return PDFRepairStatus(
      completed: json['completed'] as bool? ?? false,
      contentRecovered: json['contentRecovered'] as bool? ?? false,
      fullyFunctional: json['fullyFunctional'] as bool? ?? false,
      repairMethod: json['repairMethod'] as String? ?? '',
      repairInfo: (json['repairInfo'] as List?)?.map((info) => info.toString()).toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'completed': completed,
      'contentRecovered': contentRecovered,
      'fullyFunctional': fullyFunctional,
      'repairMethod': repairMethod,
      'repairInfo': repairInfo,
    };
  }
}

/// Information about content recovered during PDF repair.
class PDFRecoveredContent {
  /// Number of pages recovered.
  final int pagesRecovered;

  /// Total text content recovered (character count).
  final int textContentLength;

  /// Number of images recovered.
  final int imagesRecovered;

  /// Whether metadata was preserved.
  final bool metadataPreserved;

  /// List of recovered elements.
  final List<String> recoveredElements;

  PDFRecoveredContent({
    required this.pagesRecovered,
    required this.textContentLength,
    required this.imagesRecovered,
    required this.metadataPreserved,
    required this.recoveredElements,
  });

  factory PDFRecoveredContent.fromJson(Map<dynamic, dynamic> json) {
    return PDFRecoveredContent(
      pagesRecovered: json['pagesRecovered'] as int? ?? 0,
      textContentLength: json['textContentLength'] as int? ?? 0,
      imagesRecovered: json['imagesRecovered'] as int? ?? 0,
      metadataPreserved: json['metadataPreserved'] as bool? ?? false,
      recoveredElements: (json['recoveredElements'] as List?)?.map((element) => element.toString()).toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'pagesRecovered': pagesRecovered,
      'textContentLength': textContentLength,
      'imagesRecovered': imagesRecovered,
      'metadataPreserved': metadataPreserved,
      'recoveredElements': recoveredElements,
    };
  }
}
