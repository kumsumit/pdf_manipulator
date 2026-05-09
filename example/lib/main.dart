import 'dart:developer';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pdf_manipulator/pdf_manipulator.dart';
import 'package:pick_or_save/pick_or_save.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'PDF Manipulator example',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(useMaterial3: true),
      darkTheme: ThemeData.dark(useMaterial3: true),
      themeMode: ThemeMode.system,
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final _pdfManipulatorPlugin = PdfManipulator();
  final _pickOrSavePlugin = PickOrSave();

  bool _isBusy = false;
  final bool _localOnly = false;
  final bool getCachedFilePath = false;
  List<bool> isSelected = [true, false];

  List<String>? _pickedFilesPathsForMerge;
  String? _mergedPDFsPath;

  String? _pickedFilePathForSplit;
  List<String>? _splitPDFPaths;
  int pageCount = 2;
  int byteSize = 1000000;
  List<int> pageNumbers = [2, 5, 9];
  String pageRange = "1-3,5-8";

  String? _pickedFilePathForRotateDeleteReorder;
  String? _pdfPageRotatorDeleterReorderPath;
  List<PageRotationInfo> pagesRotationInfo = [
    PageRotationInfo(pageNumber: 1, rotationAngle: 90)
  ];
  List<int> pageNumbersForDeleter = [2, 4];
  List<int>? pageNumbersForReorder = [9, 8, 7, 6, 5, 4, 3, 2, 1, 10];

  String? _pickedFilePathForCompressingPDF;
  String? _compressedPDFPath;
  int imageQuality = 70;
  double imageScale = 0.5;

  String? _pickedFilePathForWatermarkingPDF;
  String? _watermarkedPDFPath;
  String watermarkText = "My Watermark";

  String? _pickedFilePathForEncryptingPDF;
  String? _encryptedPDFPath;
  String userPassword = "userpw";
  bool standardEncryptionAES128 = true;

  String? _pickedFilePathForDecryptingPDF;
  String? _decryptedPDFPath;
  String userOrOwnerPassword = "userpw";

  List<String>? _pickedFilePathsForImagesToPDF;
  List<String>? _imagesToPDFPath;
  bool createSinglePdf = true;

  String? _pickedFilePathForPageSizeInfoOfPDF;

  String? _pickedFilePathForValidityAndProtectionInfoOfPDF;

  String? _pickedFilePathForExtractImageFromPDF;

  String? _pickedFilePathForPdfToImages;
  List<String>? _pdfToImagesPaths;
  ImageFormat _imageFormat = ImageFormat.png;
  int _imageQuality = 90;
  double _imageScale = 1.0;

  String? _pickedFilePathForTextExtraction;
  PDFTextExtractionResult? _textExtractionResult;

  String? _pickedFilePathForOcr;
  PDFOCRResult? _ocrResult;
  String _ocrLanguageCode = 'en';

  String? _pickedFilePathForDigitalSignature;
  String? _pickedCertificatePath;
  String? _signedPdfPath;
  String _certificatePassword = 'password';

  String? _pickedFilePathForAnnotations;
  String? _annotatedPdfPath;

  String? _pickedFilePathForFormFields;
  String? _formFilledPdfPath;
  PDFFormFieldData? _formFieldData;
  final Map<String, dynamic> _sampleFormValues = {
    'name': 'Jane Doe',
    'email': 'jane.doe@example.com',
  };

  String? _pickedFilePathForMetadataReader;
  PDFMetadataResult? _metadataResult;

  String? _pickedFilePathForMetadataWriter;
  String? _metadataUpdatedPdfPath;
  final Map<String, String?> _sampleMetadataValues = {
    'title': 'Updated PDF Title',
    'author': 'New Author',
    'subject': 'Updated Subject',
    'keywords': 'keyword1, keyword2, keyword3',
    'creator': 'My App',
    'producer': 'PDF Manipulator Plugin',
  };

  String? _pickedFilePathForBookmarkReader;
  PDFBookmarkData? _bookmarkData;

  String? _pickedFilePathForBookmarkWriter;
  String? _bookmarkUpdatedPdfPath;
  final List<PDFBookmark> _sampleBookmarks = [
    PDFBookmark(
      title: "Introduction",
      pageNumber: 1,
    ),
    PDFBookmark(
      title: "Chapter 1",
      pageNumber: 2,
      children: [
        PDFBookmark(title: "Section 1.1", pageNumber: 3),
        PDFBookmark(title: "Section 1.2", pageNumber: 5),
      ],
    ),
    PDFBookmark(
      title: "Chapter 2",
      pageNumber: 8,
      children: [
        PDFBookmark(title: "Section 2.1", pageNumber: 9),
        PDFBookmark(title: "Section 2.2", pageNumber: 12),
      ],
    ),
    PDFBookmark(
      title: "Conclusion",
      pageNumber: 15,
    ),
  ];

  String? _pickedFilePathForComparison1;
  String? _pickedFilePathForComparison2;
  PDFComparisonResult? _comparisonResult;

  String? _pickedFilePathForRepair;
  PDFRepairResult? _repairResult;

  Future<String?> _cancelTask() async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.cancelManipulations();
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<List<String>?> _filePicker(FilePickerParams params) async {
    List<String>? result;
    try {
      setState(() {
        _isBusy = true;
      });
      result = await _pickOrSavePlugin.filePicker(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    if (!mounted) return result;
    setState(() {
      _isBusy = false;
    });
    return result;
  }

  Future<List<String>?> _fileSaver(FileSaverParams params) async {
    List<String>? result;
    try {
      setState(() {
        _isBusy = true;
      });
      result = await _pickOrSavePlugin.fileSaver(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    if (!mounted) return result;
    setState(() {
      _isBusy = false;
    });
    return result;
  }

  Future<String?> _mergePDFs(PDFMergerParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.mergePDFs(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<List<String>?> _splitPDF(PDFSplitterParams params) async {
    List<String>? result;
    try {
      result = await _pdfManipulatorPlugin.splitPDF(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfPageRotatorDeleterReorder(
      PDFPageRotatorDeleterReorderParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfPageRotatorDeleterReorder(
          params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfCompressor(PDFCompressorParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfCompressor(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfWatermark(PDFWatermarkParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfWatermark(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfEncryption(PDFEncryptionParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfEncryption(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfDecryption(PDFDecryptionParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfDecryption(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<List<String>?> _imagesToPdf(ImagesToPDFsParams params) async {
    List<String>? result;
    try {
      result = await _pdfManipulatorPlugin.imagesToPdfs(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<List<PageSizeInfo>?> _pdfPagesSize(PDFPagesSizeParams params) async {
    List<PageSizeInfo>? result;
    try {
      result = await _pdfManipulatorPlugin.pdfPagesSize(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PdfValidityAndProtection?> _pdfValidityAndProtection(
      PDFValidityAndProtectionParams params) async {
    PdfValidityAndProtection? result;
    try {
      result =
          await _pdfManipulatorPlugin.pdfValidityAndProtection(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<void> _extractImageFromPDF(ExtractImageFromPDFParams params) async {
    // List<String>? result;
    try {
      await _pdfManipulatorPlugin.extractImagesFromPdf(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    // return result;
  }

  Future<List<String>?> _pdfToImages(PDFToImagesParams params) async {
    List<String>? result;
    try {
      result = await _pdfManipulatorPlugin.pdfToImages(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFTextExtractionResult?> _pdfTextExtraction(
      PDFTextExtractionParams params) async {
    PDFTextExtractionResult? result;
    try {
      result = await _pdfManipulatorPlugin.pdfTextExtraction(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFOCRResult?> _pdfOcr(PDFOCRParams params) async {
    PDFOCRResult? result;
    try {
      result = await _pdfManipulatorPlugin.pdfOcr(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfDigitalSignature(PDFDigitalSignatureParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfDigitalSignature(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfAnnotations(PDFAnnotationsParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfAnnotations(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _fillFormFields(PDFFormFillParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.fillFormFields(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFFormFieldData?> _extractFormFieldData(
      PDFFormFieldDataParams params) async {
    PDFFormFieldData? result;
    try {
      result = await _pdfManipulatorPlugin.extractFormFieldData(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFMetadataResult?> _pdfMetadataReader(
      PDFMetadataReaderParams params) async {
    PDFMetadataResult? result;
    try {
      result = await _pdfManipulatorPlugin.pdfMetadataReader(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfMetadataWriter(
      PDFMetadataWriterParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfMetadataWriter(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFBookmarkData?> _pdfBookmarkReader(
      PDFBookmarkReaderParams params) async {
    PDFBookmarkData? result;
    try {
      result = await _pdfManipulatorPlugin.pdfBookmarkReader(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<String?> _pdfBookmarkWriter(
      PDFBookmarkWriterParams params) async {
    String? result;
    try {
      result = await _pdfManipulatorPlugin.pdfBookmarkWriter(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFComparisonResult?> _pdfComparison(
      PDFComparisonParams params) async {
    PDFComparisonResult? result;
    try {
      result = await _pdfManipulatorPlugin.pdfComparison(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<PDFRepairResult?> _pdfRepair(
      PDFRepairParams params) async {
    PDFRepairResult? result;
    try {
      result = await _pdfManipulatorPlugin.pdfRepair(params: params);
    } on PlatformException catch (e) {
      log(e.toString());
    } catch (e) {
      log(e.toString());
    }
    return result;
  }

  List<Widget> buildBookmarkTree(List<PDFBookmark> bookmarks, int level) {
    List<Widget> widgets = [];
    for (var bookmark in bookmarks) {
      widgets.add(
        Padding(
          padding: EdgeInsets.only(left: level * 16.0),
          child: Text(
            '• ${bookmark.title}${bookmark.pageNumber != null ? ' (Page ${bookmark.pageNumber})' : ''}',
            style: TextStyle(
              fontWeight: level == 0 ? FontWeight.bold : FontWeight.normal,
            ),
          ),
        ),
      );
      if (bookmark.children.isNotEmpty) {
        widgets.addAll(buildBookmarkTree(bookmark.children, level + 1));
      }
    }
    return widgets;
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        ScaffoldMessenger.of(context).hideCurrentSnackBar();
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('PDF Manipulator example'),
        ),
        body: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                "Picking Result Type",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              ToggleButtons(
                onPressed: (int index) {
                  setState(() {
                    for (int buttonIndex = 0;
                        buttonIndex < isSelected.length;
                        buttonIndex++) {
                      if (buttonIndex == index) {
                        isSelected[buttonIndex] = true;
                      } else {
                        isSelected[buttonIndex] = false;
                      }
                    }
                  });
                },
                isSelected: isSelected,
                children: const <Widget>[
                  Text(" URI "),
                  Text(" Cached path "),
                ],
              ),
              const SizedBox(height: 16),
              Text(
                "Cancel All Tasks",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              CustomButton(
                onPressed: () async {
                  await _cancelTask();
                },
                buttonText: "Cancel all tasks ",
              ),
              const SizedBox(height: 16),
              Text(
                "Merging multiple PDFs",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick multiple PDF files',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    enableMultipleSelection: true,
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilesPathsForMerge = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Merge',
                          onPressed: _pickedFilesPathsForMerge == null
                              ? null
                              : _pickedFilesPathsForMerge!.length < 2
                                  ? null
                                  : () async {
                                      final params = PDFMergerParams(
                                        pdfsPaths: _pickedFilesPathsForMerge!,
                                      );

                                      String? result = await _mergePDFs(params);

                                      if (result != null && result.isNotEmpty) {
                                        setState(() {
                                          _mergedPDFsPath = result;
                                        });
                                      }

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                      CustomButton(
                          buttonText: 'Save merged PDF',
                          onPressed: _mergedPDFsPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                          localOnly: _localOnly,
                                          saveFiles: [
                                            SaveFileInfo(
                                                filePath: _mergedPDFsPath,
                                                fileName: "Merged PDF.pdf")
                                          ]);

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Splitting PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForSplit = result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      const Divider(),
                      CustomButton(
                          buttonText: 'Split PDF by page count',
                          onPressed: _pickedFilePathForSplit == null
                              ? null
                              : () async {
                                  final params = PDFSplitterParams(
                                    pdfPath: _pickedFilePathForSplit!,
                                    pageCount: pageCount,
                                  );

                                  List<String>? result =
                                      await _splitPDF(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _splitPDFPaths = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "pageCount: $pageCount",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      const Divider(),
                      CustomButton(
                          buttonText: 'Split PDF by byte size',
                          onPressed: _pickedFilePathForSplit == null
                              ? null
                              : () async {
                                  final params = PDFSplitterParams(
                                    pdfPath: _pickedFilePathForSplit!,
                                    byteSize: byteSize,
                                  );

                                  List<String>? result =
                                      await _splitPDF(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _splitPDFPaths = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "byteSize: $byteSize",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Split PDF by page numbers',
                          onPressed: _pickedFilePathForSplit == null
                              ? null
                              : () async {
                                  final params = PDFSplitterParams(
                                    pdfPath: _pickedFilePathForSplit!,
                                    pageNumbers: pageNumbers,
                                  );

                                  List<String>? result =
                                      await _splitPDF(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _splitPDFPaths = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "pageNumbers: $pageNumbers",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      const Divider(),
                      CustomButton(
                          buttonText: 'Split PDF by page numbers',
                          onPressed: _pickedFilePathForSplit == null
                              ? null
                              : () async {
                                  final params = PDFSplitterParams(
                                    pdfPath: _pickedFilePathForSplit!,
                                    pageRange: pageRange,
                                  );

                                  List<String>? result =
                                      await _splitPDF(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _splitPDFPaths = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "pageRange: $pageRange",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      const Divider(),
                      CustomButton(
                          buttonText: 'Save split PDFs',
                          onPressed: _splitPDFPaths == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: List.generate(
                                            _splitPDFPaths!.length,
                                            (index) => SaveFileInfo(
                                                filePath:
                                                    _splitPDFPaths![index],
                                                fileName:
                                                    "Split PDF ${index + 1}.pdf")),
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Rotate, Delete, Reorder PDF pages",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForRotateDeleteReorder =
                                          result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Rotate, Delete, Reorder PDF pages',
                          onPressed:
                              _pickedFilePathForRotateDeleteReorder == null
                                  ? null
                                  : () async {
                                      final params =
                                          PDFPageRotatorDeleterReorderParams(
                                        pdfPath:
                                            _pickedFilePathForRotateDeleteReorder!,
                                        pagesRotationInfo: pagesRotationInfo,
                                        pageNumbersForDeleter:
                                            pageNumbersForDeleter,
                                        pageNumbersForReorder:
                                            pageNumbersForReorder,
                                      );

                                      String? result =
                                          await _pdfPageRotatorDeleterReorder(
                                              params);

                                      if (result != null && result.isNotEmpty) {
                                        setState(() {
                                          _pdfPageRotatorDeleterReorderPath =
                                              result;
                                        });
                                      }

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                      Text(
                        "pagesRotationInfo: $pagesRotationInfo\npageNumbersForDeleter: $pageNumbersForDeleter\npageNumbersForReorder: $pageNumbersForReorder",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Save rotated, deleted, reordered PDF',
                          onPressed: _pdfPageRotatorDeleterReorderPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath:
                                                  _pdfPageRotatorDeleterReorderPath,
                                              fileName:
                                                  "Rotated, Deleted, Reordered PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Compressing PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForCompressingPDF =
                                          result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Compress PDF',
                          onPressed: _pickedFilePathForCompressingPDF == null
                              ? null
                              : () async {
                                  final params = PDFCompressorParams(
                                    pdfPath: _pickedFilePathForCompressingPDF!,
                                    imageQuality: imageQuality,
                                    imageScale: imageScale,
                                  );

                                  String? result = await _pdfCompressor(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _compressedPDFPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "imageQuality: $imageQuality, imageScale: $imageScale",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Save compressed PDF',
                          onPressed: _compressedPDFPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _compressedPDFPath,
                                              fileName: "Compressed PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Watermarking PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForWatermarkingPDF =
                                          result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Watermark PDF',
                          onPressed: _pickedFilePathForWatermarkingPDF == null
                              ? null
                              : () async {
                                  final params = PDFWatermarkParams(
                                    pdfPath: _pickedFilePathForWatermarkingPDF!,
                                    text: watermarkText,
                                  );

                                  String? result = await _pdfWatermark(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _watermarkedPDFPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "watermarkText: $watermarkText",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Save watermarked PDF',
                          onPressed: _watermarkedPDFPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _watermarkedPDFPath,
                                              fileName: "Watermarked PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Encrypt PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForEncryptingPDF =
                                          result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Encrypt PDF',
                          onPressed: _pickedFilePathForEncryptingPDF == null
                              ? null
                              : () async {
                                  final params = PDFEncryptionParams(
                                    pdfPath: _pickedFilePathForEncryptingPDF!,
                                    userPassword: userPassword,
                                    standardEncryptionAES128:
                                        standardEncryptionAES128,
                                  );

                                  String? result = await _pdfEncryption(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _encryptedPDFPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "userPassword: $userPassword, standardEncryptionAES128: $standardEncryptionAES128",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Save encrypted PDF',
                          onPressed: _encryptedPDFPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _encryptedPDFPath,
                                              fileName: "Encrypted PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Decrypt PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForDecryptingPDF =
                                          result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Decrypt PDF',
                          onPressed: _pickedFilePathForDecryptingPDF == null
                              ? null
                              : () async {
                                  final params = PDFDecryptionParams(
                                    pdfPath: _pickedFilePathForDecryptingPDF!,
                                    password: userOrOwnerPassword,
                                  );

                                  String? result = await _pdfDecryption(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _decryptedPDFPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "userOrOwnerPassword: $userOrOwnerPassword",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Save decrypted PDF',
                          onPressed: _decryptedPDFPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _decryptedPDFPath,
                                              fileName: "Decrypted PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Converting images to PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick multiple image files',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: [
                                      "image/jpeg",
                                      "image/jp2",
                                      "image/png",
                                      "image/bmp",
                                      "image/wmf",
                                      "image/tiff",
                                      "image/g3fax",
                                      "image/x-jbig2"
                                    ],
                                    enableMultipleSelection: true,
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathsForImagesToPDF = result;
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Convert images to PDF',
                          onPressed: _pickedFilePathsForImagesToPDF == null
                              ? null
                              : () async {
                                  final params = ImagesToPDFsParams(
                                    imagesPaths:
                                        _pickedFilePathsForImagesToPDF!,
                                    createSinglePdf: createSinglePdf,
                                  );

                                  List<String>? result =
                                      await _imagesToPdf(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _imagesToPDFPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      Text(
                        "createSinglePdf: $createSinglePdf",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      CustomButton(
                          buttonText: 'Save images converted to PDF',
                          onPressed: _imagesToPDFPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: List.generate(
                                            _imagesToPDFPath!.length,
                                            (index) => SaveFileInfo(
                                                filePath:
                                                    _imagesToPDFPath![index],
                                                fileName:
                                                    "Image ${index + 1} PDF.pdf")),
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "PDF page size info",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForPageSizeInfoOfPDF =
                                          result[0];
                                    });
                                  }
                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Display page size info',
                          onPressed: _pickedFilePathForPageSizeInfoOfPDF == null
                              ? null
                              : () async {
                                  final params = PDFPagesSizeParams(
                                    pdfPath:
                                        _pickedFilePathForPageSizeInfoOfPDF!,
                                  );

                                  List<PageSizeInfo>? result =
                                      await _pdfPagesSize(params);

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                    ],
                  ),
                ),
              ),
              Text(
                "PDF validity & protection info",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForValidityAndProtectionInfoOfPDF =
                                          result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Display PDF validity & protection info',
                          onPressed:
                              _pickedFilePathForValidityAndProtectionInfoOfPDF ==
                                      null
                                  ? null
                                  : () async {
                                      final params =
                                          PDFValidityAndProtectionParams(
                                        pdfPath:
                                            _pickedFilePathForValidityAndProtectionInfoOfPDF!,
                                      );

                                      PdfValidityAndProtection? result =
                                          await _pdfValidityAndProtection(
                                              params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              Text(
                "Extract Image from PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForExtractImageFromPDF =
                                          result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Extract Image from PDF',
                          onPressed:
                              _pickedFilePathForExtractImageFromPDF == null
                                  ? null
                                  : () async {
                                      final outputDir =
                                          await getApplicationSupportDirectory();
                                      final params = ExtractImageFromPDFParams(
                                          pdfPath:
                                              _pickedFilePathForExtractImageFromPDF!,
                                          outputDir: outputDir
                                              .path //"/storage/emulated/0/DCIM/Camera",
                                          // pageNumber: 1,
                                          // imageFormat: ImageFormat.png,
                                          );

                                      await _extractImageFromPDF(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: "Image Extracted");
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Convert PDF to Images",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForPdfToImages = result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Convert PDF to Images',
                          onPressed: _pickedFilePathForPdfToImages == null
                              ? null
                              : () async {
                                  final params = PDFToImagesParams(
                                    pdfPath: _pickedFilePathForPdfToImages!,
                                    imageFormat: _imageFormat,
                                    quality: _imageQuality,
                                    scale: _imageScale,
                                  );

                                  List<String>? result =
                                      await _pdfToImages(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pdfToImagesPaths = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text:
                                            "Generated ${result?.length ?? 0} images");
                                  }
                                }),
                      Text(
                        "Format: $_imageFormat, Quality: $_imageQuality, Scale: $_imageScale",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Extract Text from PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForTextExtraction =
                                          result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Extract Text from PDF',
                          onPressed: _pickedFilePathForTextExtraction == null
                              ? null
                              : () async {
                                  final params = PDFTextExtractionParams(
                                    pdfPath: _pickedFilePathForTextExtraction!,
                                  );

                                  PDFTextExtractionResult? result =
                                      await _pdfTextExtraction(params);

                                  if (result != null) {
                                    setState(() {
                                      _textExtractionResult = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text:
                                            "Extracted text from ${result?.pageTexts.length ?? 0} pages");
                                  }
                                }),
                      if (_textExtractionResult != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                "Full Text (first 200 chars):",
                                style: Theme.of(context).textTheme.labelMedium,
                              ),
                              Text(
                                _textExtractionResult!.fullText.length > 200
                                    ? "${_textExtractionResult!.fullText.substring(0, 200)}..."
                                    : _textExtractionResult!.fullText,
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                            ],
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Perform OCR on PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick single PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForOcr = result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Perform OCR on PDF',
                          onPressed: _pickedFilePathForOcr == null
                              ? null
                              : () async {
                                  final params = PDFOCRParams(
                                    pdfPath: _pickedFilePathForOcr!,
                                    languageCode: _ocrLanguageCode,
                                  );

                                  PDFOCRResult? result = await _pdfOcr(params);

                                  if (result != null) {
                                    setState(() {
                                      _ocrResult = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text:
                                            "OCR completed on ${result?.pageResults.length ?? 0} pages");
                                  }
                                }),
                      Text(
                        "Language: $_ocrLanguageCode",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      if (_ocrResult != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                "Full OCR Text (first 200 chars):",
                                style: Theme.of(context).textTheme.labelMedium,
                              ),
                              Text(
                                _ocrResult!.fullText.length > 200
                                    ? "${_ocrResult!.fullText.substring(0, 200)}..."
                                    : _ocrResult!.fullText,
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                              const SizedBox(height: 8),
                              Text(
                                "Average Confidence: ${(_ocrResult!.pageResults.values.map((r) => r.confidence).reduce((a, b) => a + b) / _ocrResult!.pageResults.length * 100).toStringAsFixed(1)}%",
                                style: Theme.of(context).textTheme.labelMedium,
                              ),
                            ],
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "PDF form fields",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick fillable PDF file',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForFormFields = result[0];
                                      _formFieldData = null;
                                      _formFilledPdfPath = null;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Extract form field data',
                          onPressed: _pickedFilePathForFormFields == null
                              ? null
                              : () async {
                                  final params = PDFFormFieldDataParams(
                                    pdfPath: _pickedFilePathForFormFields!,
                                  );

                                  PDFFormFieldData? result =
                                      await _extractFormFieldData(params);

                                  if (result != null) {
                                    setState(() {
                                      _formFieldData = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text:
                                            "Found ${result?.fields.length ?? 0} form fields");
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Fill form fields',
                          onPressed: _pickedFilePathForFormFields == null
                              ? null
                              : () async {
                                  final fields = _formFieldData?.fields ?? {};
                                  final values = <String, dynamic>{};

                                  for (final field in fields.values) {
                                    if (field.type == 'signature') continue;
                                    if (field.type == 'checkbox') {
                                      values[field.name] = true;
                                    } else if ((field.type == 'radio' ||
                                            field.type == 'combo' ||
                                            field.type == 'list') &&
                                        field.options.isNotEmpty) {
                                      values[field.name] = field.options.first;
                                    } else {
                                      values[field.name] =
                                          'Sample ${field.name}';
                                    }
                                  }

                                  final params = PDFFormFillParams(
                                    pdfPath: _pickedFilePathForFormFields!,
                                    fieldValues: values.isEmpty
                                        ? _sampleFormValues
                                        : values,
                                  );

                                  String? result =
                                      await _fillFormFields(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _formFilledPdfPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result != null
                                            ? "PDF form filled"
                                            : "No PDF form was filled");
                                  }
                                }),
                      if (_formFieldData != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                "Fields:",
                                style: Theme.of(context).textTheme.labelMedium,
                              ),
                              Text(
                                _formFieldData!.fields.values
                                    .take(5)
                                    .map((field) =>
                                        "${field.name} (${field.type}) = ${field.value}")
                                    .join("\n"),
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                            ],
                          ),
                        ),
                      CustomButton(
                          buttonText: 'Save filled PDF',
                          onPressed: _formFilledPdfPath == null
                              ? null
                              : _isBusy
                                  ? null
                                  : () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _formFilledPdfPath,
                                              fileName: "Filled Form PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "PDF Metadata Management",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      // Metadata Reader Section
                      Text(
                        "Read PDF Metadata",
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Pick PDF file to read metadata',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForMetadataReader = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForMetadataReader != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Selected: ${_pickedFilePathForMetadataReader!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Expanded(
                            child: CustomButton(
                                buttonText: 'Read Metadata',
                                onPressed: _pickedFilePathForMetadataReader == null
                                    ? null
                                    : () async {
                                        final params = PDFMetadataReaderParams(
                                          pdfPath: _pickedFilePathForMetadataReader!,
                                        );

                                        PDFMetadataResult? result =
                                            await _pdfMetadataReader(params);

                                        if (mounted) {
                                          setState(() {
                                            _metadataResult = result;
                                          });
                                        }
                                      }),
                          ),
                          if (_metadataResult != null)
                            IconButton(
                              onPressed: () {
                                setState(() {
                                  _metadataResult = null;
                                });
                              },
                              icon: const Icon(Icons.clear),
                            ),
                        ],
                      ),
                      if (_metadataResult != null) ...[
                        const SizedBox(height: 8),
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text("Title: ${_metadataResult!.title ?? 'N/A'}"),
                              Text("Author: ${_metadataResult!.author ?? 'N/A'}"),
                              Text("Subject: ${_metadataResult!.subject ?? 'N/A'}"),
                              Text("Keywords: ${_metadataResult!.keywords ?? 'N/A'}"),
                              Text("Creator: ${_metadataResult!.creator ?? 'N/A'}"),
                              Text("Producer: ${_metadataResult!.producer ?? 'N/A'}"),
                              Text("Creation Date: ${_metadataResult!.creationDate ?? 'N/A'}"),
                              Text("Modification Date: ${_metadataResult!.modificationDate ?? 'N/A'}"),
                            ],
                          ),
                        ),
                      ],

                      const Divider(height: 32),

                      // Metadata Writer Section
                      Text(
                        "Write PDF Metadata",
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Pick PDF file to update metadata',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForMetadataWriter = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForMetadataWriter != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Selected: ${_pickedFilePathForMetadataWriter!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Update Metadata',
                          onPressed: _pickedFilePathForMetadataWriter == null
                              ? null
                              : () async {
                                  final params = PDFMetadataWriterParams(
                                    pdfPath: _pickedFilePathForMetadataWriter!,
                                    title: _sampleMetadataValues['title'],
                                    author: _sampleMetadataValues['author'],
                                    subject: _sampleMetadataValues['subject'],
                                    keywords: _sampleMetadataValues['keywords'],
                                    creator: _sampleMetadataValues['creator'],
                                    producer: _sampleMetadataValues['producer'],
                                  );

                                  String? result =
                                      await _pdfMetadataWriter(params);

                                  if (mounted) {
                                    setState(() {
                                      _metadataUpdatedPdfPath = result;
                                    });
                                  }
                                }),
                      if (_metadataUpdatedPdfPath != null)
                        Row(
                          children: [
                            Expanded(
                              child: CustomButton(
                                  buttonText: 'Save Updated PDF',
                                  onPressed: () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _metadataUpdatedPdfPath,
                                              fileName: "Updated Metadata PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                            ),
                            IconButton(
                              onPressed: () {
                                setState(() {
                                  _metadataUpdatedPdfPath = null;
                                });
                              },
                              icon: const Icon(Icons.clear),
                            ),
                          ],
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "PDF Bookmark Management",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      // Bookmark Reader Section
                      Text(
                        "Read PDF Bookmarks",
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Pick PDF file to read bookmarks',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForBookmarkReader = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForBookmarkReader != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Selected: ${_pickedFilePathForBookmarkReader!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Expanded(
                            child: CustomButton(
                                buttonText: 'Read Bookmarks',
                                onPressed: _pickedFilePathForBookmarkReader == null
                                    ? null
                                    : () async {
                                        final params = PDFBookmarkReaderParams(
                                          pdfPath: _pickedFilePathForBookmarkReader!,
                                        );

                                        PDFBookmarkData? result =
                                            await _pdfBookmarkReader(params);

                                        if (mounted) {
                                          setState(() {
                                            _bookmarkData = result;
                                          });
                                        }
                                      }),
                          ),
                          if (_bookmarkData != null)
                            IconButton(
                              onPressed: () {
                                setState(() {
                                  _bookmarkData = null;
                                });
                              },
                              icon: const Icon(Icons.clear),
                            ),
                        ],
                      ),
                      if (_bookmarkData != null) ...[
                        const SizedBox(height: 8),
                        Container(
                          constraints: const BoxConstraints(maxHeight: 200),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: SingleChildScrollView(
                            child: Padding(
                              padding: const EdgeInsets.all(8),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: buildBookmarkTree(_bookmarkData!.bookmarks, 0),
                              ),
                            ),
                          ),
                        ),
                      ],

                      const Divider(height: 32),

                      // Bookmark Writer Section
                      Text(
                        "Write PDF Bookmarks",
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Pick PDF file to add bookmarks',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForBookmarkWriter = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForBookmarkWriter != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Selected: ${_pickedFilePathForBookmarkWriter!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Add Sample Bookmarks',
                          onPressed: _pickedFilePathForBookmarkWriter == null
                              ? null
                              : () async {
                                  final params = PDFBookmarkWriterParams(
                                    pdfPath: _pickedFilePathForBookmarkWriter!,
                                    bookmarks: _sampleBookmarks,
                                  );

                                  String? result =
                                      await _pdfBookmarkWriter(params);

                                  if (mounted) {
                                    setState(() {
                                      _bookmarkUpdatedPdfPath = result;
                                    });
                                  }
                                }),
                      if (_bookmarkUpdatedPdfPath != null)
                        Row(
                          children: [
                            Expanded(
                              child: CustomButton(
                                  buttonText: 'Save Bookmarked PDF',
                                  onPressed: () async {
                                      final params = FileSaverParams(
                                        localOnly: _localOnly,
                                        saveFiles: [
                                          SaveFileInfo(
                                              filePath: _bookmarkUpdatedPdfPath,
                                              fileName: "Bookmarked PDF.pdf")
                                        ],
                                      );

                                      List<String>? result =
                                          await _fileSaver(params);

                                      if (mounted && context.mounted) {
                                        callSnackBar(
                                            context: context,
                                            text: result.toString());
                                      }
                                    }),
                            ),
                            IconButton(
                              onPressed: () {
                                setState(() {
                                  _bookmarkUpdatedPdfPath = null;
                                });
                              },
                              icon: const Icon(Icons.clear),
                            ),
                          ],
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "PDF Comparison",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      Text(
                        "Compare Two PDFs",
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      // First PDF picker
                      CustomButton(
                          buttonText: 'Pick First PDF to compare',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForComparison1 = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForComparison1 != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "First PDF: ${_pickedFilePathForComparison1!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),

                      const SizedBox(height: 8),

                      // Second PDF picker
                      CustomButton(
                          buttonText: 'Pick Second PDF to compare',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForComparison2 = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForComparison2 != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Second PDF: ${_pickedFilePathForComparison2!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),

                      const SizedBox(height: 8),

                      // Compare button
                      Row(
                        children: [
                          Expanded(
                            child: CustomButton(
                                buttonText: 'Compare PDFs',
                                onPressed: (_pickedFilePathForComparison1 == null ||
                                           _pickedFilePathForComparison2 == null)
                                    ? null
                                    : () async {
                                        final params = PDFComparisonParams(
                                          pdfPath1: _pickedFilePathForComparison1!,
                                          pdfPath2: _pickedFilePathForComparison2!,
                                          compareText: true,
                                          compareMetadata: true,
                                          compareStructure: true,
                                        );

                                        PDFComparisonResult? result =
                                            await _pdfComparison(params);

                                        if (mounted) {
                                          setState(() {
                                            _comparisonResult = result;
                                          });
                                        }
                                      }),
                          ),
                          if (_comparisonResult != null)
                            IconButton(
                              onPressed: () {
                                setState(() {
                                  _comparisonResult = null;
                                });
                              },
                              icon: const Icon(Icons.clear),
                            ),
                        ],
                      ),

                      // Results display
                      if (_comparisonResult != null) ...[
                        const SizedBox(height: 16),
                        Container(
                          constraints: const BoxConstraints(maxHeight: 300),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: SingleChildScrollView(
                            child: Padding(
                              padding: const EdgeInsets.all(8),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  // Overall similarity
                                  Text(
                                    "Overall Similarity: ${(_comparisonResult!.overallSimilarity * 100).toInt()}%",
                                    style: Theme.of(context).textTheme.titleMedium,
                                  ),
                                  const SizedBox(height: 8),

                                  // Summary
                                  Text(
                                    "Summary:",
                                    style: Theme.of(context).textTheme.titleSmall,
                                  ),
                                  ..._comparisonResult!.summary.map((item) =>
                                    Text("• $item")
                                  ),

                                  // Detailed results
                                  if (_comparisonResult!.structureComparison != null) ...[
                                    const SizedBox(height: 12),
                                    Text(
                                      "Structure Comparison:",
                                      style: Theme.of(context).textTheme.titleSmall,
                                    ),
                                    Text("PDF 1: ${_comparisonResult!.structureComparison!.pageCount1} pages"),
                                    Text("PDF 2: ${_comparisonResult!.structureComparison!.pageCount2} pages"),
                                    Text("Page count equal: ${_comparisonResult!.structureComparison!.pageCountEqual}"),
                                    if (_comparisonResult!.structureComparison!.differences.isNotEmpty)
                                      ..._comparisonResult!.structureComparison!.differences.map((diff) =>
                                        Text("• $diff", style: TextStyle(color: Colors.red))
                                      ),
                                  ],

                                  if (_comparisonResult!.metadataComparison != null) ...[
                                    const SizedBox(height: 12),
                                    Text(
                                      "Metadata Differences:",
                                      style: Theme.of(context).textTheme.titleSmall,
                                    ),
                                    if (_comparisonResult!.metadataComparison!.differences.isEmpty)
                                      const Text("• No metadata differences found")
                                    else
                                      ..._comparisonResult!.metadataComparison!.differences.map((diff) =>
                                        Text("• ${diff.field}: '${diff.value1}' vs '${diff.value2}'",
                                             style: TextStyle(color: Colors.orange))
                                      ),
                                  ],

                                  if (_comparisonResult!.textComparison != null) ...[
                                    const SizedBox(height: 12),
                                    Text(
                                      "Text Comparison:",
                                      style: Theme.of(context).textTheme.titleSmall,
                                    ),
                                    Text("Text similarity: ${(_comparisonResult!.textComparison!.similarity * 100).toInt()}%"),
                                    Text("Differences found: ${_comparisonResult!.textComparison!.differences.length}"),
                                  ],
                                ],
                              ),
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Add Digital Signature to PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick PDF file to sign',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForDigitalSignature =
                                          result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Pick certificate file (.p12/.pfx)',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/x-pkcs12"],
                                    allowedExtensions: [".p12", ".pfx"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedCertificatePath = result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Add Digital Signature',
                          onPressed: _pickedFilePathForDigitalSignature ==
                                      null ||
                                  _pickedCertificatePath == null
                              ? null
                              : () async {
                                  final params = PDFDigitalSignatureParams(
                                    pdfPath:
                                        _pickedFilePathForDigitalSignature!,
                                    certificatePath: _pickedCertificatePath!,
                                    certificatePassword: _certificatePassword,
                                    reason: "Document approval",
                                    location: "Test Location",
                                    contact: "test@example.com",
                                    appearance: SignatureAppearance(
                                      text: "Test Signature\nApproved",
                                      x: 100,
                                      y: 100,
                                      width: 200,
                                      height: 100,
                                      pageNumber: 1,
                                    ),
                                  );

                                  String? result =
                                      await _pdfDigitalSignature(params);

                                  if (result != null) {
                                    setState(() {
                                      _signedPdfPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result != null
                                            ? "PDF signed successfully"
                                            : "Failed to sign PDF");
                                  }
                                }),
                      Text(
                        "Certificate password: $_certificatePassword",
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                      if (_signedPdfPath != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Signed PDF: ${_signedPdfPath!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text(
                "Add Annotations to PDF",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      CustomButton(
                          buttonText: 'Pick PDF file to annotate',
                          onPressed: _isBusy
                              ? null
                              : () async {
                                  final params = FilePickerParams(
                                    localOnly: _localOnly,
                                    getCachedFilePath: isSelected[1],
                                    mimeTypesFilter: ["application/pdf"],
                                    allowedExtensions: [".pdf"],
                                  );

                                  List<String>? result =
                                      await _filePicker(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pickedFilePathForAnnotations = result[0];
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result.toString());
                                  }
                                }),
                      CustomButton(
                          buttonText: 'Add Annotations to PDF',
                          onPressed: _pickedFilePathForAnnotations == null
                              ? null
                              : () async {
                                  final params = PDFAnnotationsParams(
                                    pdfPath: _pickedFilePathForAnnotations!,
                                    annotations: [
                                      // Text annotation (sticky note)
                                      TextAnnotation(
                                        pageNumber: 1,
                                        rect: [100, 200, 200, 250],
                                        contents: "This is a sample note",
                                        title: "Sample Note",
                                        color: [1.0, 1.0, 0.0], // Yellow
                                      ),
                                      // Highlight annotation
                                      HighlightAnnotation(
                                        pageNumber: 1,
                                        rect: [50, 150, 300, 170],
                                        quads: [
                                          [50, 170, 300, 170, 50, 150, 300, 150]
                                        ],
                                        contents: "Highlighted text",
                                        color: [1.0, 1.0, 0.0], // Yellow
                                      ),
                                      // Link annotation
                                      LinkAnnotation(
                                        pageNumber: 1,
                                        rect: [150, 100, 250, 120],
                                        url: "https://flutter.dev",
                                        contents: "Flutter Website",
                                      ),
                                    ],
                                  );

                                  String? result = await _pdfAnnotations(params);

                                  if (result != null) {
                                    setState(() {
                                      _annotatedPdfPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result != null
                                            ? "Annotations added successfully"
                                            : "Failed to add annotations");
                                  }
                                }),
                      if (_annotatedPdfPath != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Annotated PDF: ${_annotatedPdfPath!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class CustomButton extends StatelessWidget {
  const CustomButton({super.key, required this.buttonText, this.onPressed});

  final String buttonText;
  final void Function()? onPressed;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: OutlinedButton(
              onPressed: onPressed,
              child: Text(buttonText, textAlign: TextAlign.center)),
        ),
      ],
    );
  }
}

callSnackBar({required BuildContext context, required String text}) {
  ScaffoldMessenger.of(context).hideCurrentSnackBar();
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
    content: Text(text),
  ));
}

// null
//                                   ? null
//                                   : () async {
//                                       final params =
//                                           PDFValidityAndProtectionParams(
//                                         pdfPath:
//                                             _pickedFilePathForValidityAndProtectionInfoOfPDF!,
//                                       );

//                                       PdfValidityAndProtection? result =
//                                           await _pdfValidityAndProtection(
//                                               params);

//                                       if (mounted && context.mounted) {
//                                         callSnackBar(
//                                             context: context,
//                                             text: result.toString());
//                                       }
//                                     }),
