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

  Future<void> _extractImageFromPDF(
      ExtractImageFromPDFParams params) async {
    // List<String>? result;
    try {
       await _pdfManipulatorPlugin
          .extractImagesFromPdf(params: params);
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

  Future<PDFTextExtractionResult?> _pdfTextExtraction(PDFTextExtractionParams params) async {
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
                                 final  outputDir = await getApplicationSupportDirectory();
                                      final params =
                                          ExtractImageFromPDFParams(
                                        pdfPath:
                                            _pickedFilePathForExtractImageFromPDF!,
                                            outputDir: outputDir.path //"/storage/emulated/0/DCIM/Camera",
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

                                  List<String>? result = await _pdfToImages(params);

                                  if (result != null && result.isNotEmpty) {
                                    setState(() {
                                      _pdfToImagesPaths = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: "Generated ${result?.length ?? 0} images");
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
                                      _pickedFilePathForTextExtraction = result[0];
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

                                  PDFTextExtractionResult? result = await _pdfTextExtraction(params);

                                  if (result != null) {
                                    setState(() {
                                      _textExtractionResult = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: "Extracted text from ${result?.pageTexts.length ?? 0} pages");
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
                                        text: "OCR completed on ${result?.pageResults.length ?? 0} pages");
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
                                      _pickedFilePathForDigitalSignature = result[0];
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
                          onPressed: _pickedFilePathForDigitalSignature == null ||
                              _pickedCertificatePath == null
                              ? null
                              : () async {
                                  final params = PDFDigitalSignatureParams(
                                    pdfPath: _pickedFilePathForDigitalSignature!,
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

                                  String? result = await _pdfDigitalSignature(params);

                                  if (result != null) {
                                    setState(() {
                                      _signedPdfPath = result;
                                    });
                                  }

                                  if (mounted && context.mounted) {
                                    callSnackBar(
                                        context: context,
                                        text: result != null ? "PDF signed successfully" : "Failed to sign PDF");
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