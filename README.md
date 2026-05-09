[![pub package](https://img.shields.io/pub/v/pdf_manipulator.svg)](https://pub.dev/packages/pdf_manipulator) [![wakatime](https://wakatime.com/badge/user/83f3b15d-49de-4c01-b8de-bbc132f11be1/project/db0907ad-0c7e-49cb-bbbb-a0fba05b6bc9.svg)](https://wakatime.com/badge/user/83f3b15d-49de-4c01-b8de-bbc132f11be1/project/db0907ad-0c7e-49cb-bbbb-a0fba05b6bc9)

## Word from creator

**Hello👋, This package is completely compatible with flutter and it also supports using Android Uri of picked file to work with which offers some real benefits such as manipulating them without caching or validating them without caching.**

**Yes, without a doubt, giving a free 👍 or ⭐ will encourage me to keep working on this plugin.**

## Package description

A flutter plugin for doing various manipulations such as merge, split, compress and many more on PDF easily.

**Note:** This project utilises itext7 for various operations involving PDFs and since itext7 AGPL V3 License is used in this plugin, it is also licenced under this licence. The project/plugin developer, the owner of the copyright, and the contributors are not accountable or liable for any damage resulting from this project/plugin.

## Features

- Works on Android 5.0 (API level 21) or later.
- Works with both absolute file path and Android native Uri.
- Supports merging multiple PDFs.
- Supports splitting PDF.
- Supports rotating PDF pages.
- Supports deleting PDF pages.
- Supports reordering PDF pages.
- Supports rotating, deleting, reordering PDF pages at the same time for more efficiency.
- Supports compressing PDF.
- Supports watermarking PDF.
- Supports encrypting PDF.
- Supports decrypting PDF.
- Supports converting images to PDF.
- Supports getting PDF validity and protection info.
- Supports getting PDF page size info.
- Supports converting PDF pages to images.
- Supports extracting text from PDF pages.
- Supports OCR (Optical Character Recognition) on PDF pages.
- Supports adding digital signatures to PDFs.
- Supports adding annotations to PDFs (text, highlight, underline, etc.).
- Supports filling PDF form fields.
- Supports extracting PDF form field data.
- Supports reading and modifying PDF metadata (title, author, subject, keywords, creation date, modification date).
- Supports creating, modifying, and extracting PDF bookmarks/outlines (table of contents).
- Supports comparing two PDFs and highlighting differences (text, layout, structure).
- Supports repairing corrupted or damaged PDF files.

**Note:** To use it in realease mode you will need to create a file named proguard-rules.pro in your project Android->App->proguard-rules.pro. In that file you need to add the below block of text at the end of the file.
```
# To use iText in release mode Otherwise we get
# PlatformException AbstractITextEvent is only for internal usage.
-keep public class com.itextpdf.**
-keep public class org.apache.**
```
For reference see this [project file](https://github.com/chaudharydeepanshu/files_tools/blob/main/android/app/proguard-rules.pro).

Then, setup your App->build.gradle buildTypes release config as done in this [project file](https://github.com/chaudharydeepanshu/files_tools/blob/54774ecf28d37c2a27d4fc666f7b178ad84ac46b/android/app/build.gradle#L77).
If you don't do all of this then you will get PlatformException: AbstractITextEvent is only for internal usage in Release build.

**Note:** If you are getting errors in you IDE after updating this plugin to newer version and the error contains works like Redeclaration, Conflicting declarations, Overload resolution ambiguity then to fix that you probably need to remove the older version of plugin from pub cache `C:\Users\username\AppData\Local\Pub\Cache\hosted\pub.dev\older_version` or simply run `flutter clean`.

## Getting started

- In pubspec.yaml, add this dependency:

```yaml
pdf_manipulator: 
```

- Add this package to your project:

```dart
import 'package:pdf_manipulator/pdf_manipulator.dart';
```

## Basic Usage

### Merging multiple PDFs

```dart
String? mergedPdfPath = await PdfManipulator().mergePDFs(
  params: PDFMergerParams(pdfsPaths: [pdfPath1, padfPath2]),
);
```

### Spliting PDF

```dart
String? mergedPdfPath = await PdfManipulator().mergePDFs(
  params: PDFMergerParams(pdfsPaths: [pdfPath1, padfPath2]),
);
```

#### Split PDF by page count

```dart
List<String>? splitPdfPaths = await PdfManipulator().splitPDF(
  params: PDFSplitterParams(pdfPath: pdfPath, pageCount: 2),
);
```

#### Split PDF by size

```dart
List<String>? splitPdfPaths = await PdfManipulator().splitPDF(
  params: PDFSplitterParams(pdfPath: pdfPath, byteSize: splitSize),
);
```

#### Split PDF by page numbers

```dart
List<String>? splitPdfPaths = await PdfManipulator().splitPDF(
  params: PDFSplitterParams(pdfPath: pdfPath, pageNumbers: [2, 5]),
);
```

#### Extract PDF pages by page range

```dart
List<String>? splitPdfPaths = await PdfManipulator().splitPDF(
  params: PDFSplitterParams(pdfPath: pdfPath, pageRanges: ["2", "5-10"]),
);
```

### Rotaing PDF pages

```dart
String? rotatedPagesPdfPath = await PdfManipulator().pdfPageRotator(
  params: PDFPageRotatorParams(pdfPath: pdfPath, pagesRotationInfo: [PageRotationInfo(pageNumber: 1, rotationAngle: 180)]),
);
```

### Deleting PDF pages

```dart
String? deletedPagesPdfPath = await PdfManipulator().pdfPageDeleter(
  params: PDFPageDeleterParams(pdfPath: pdfPath, pageNumbers: [1, 2, 3]),
);
```

### Reordering PDF pages

```dart
String? reorderedPagesPdfPath = await PdfManipulator().pdfPageReorder(
  params: PDFPageReorderParams(pdfPath: pdfPath, pageNumbers: [4, 1]),
);
```

### Rotating, Deleting, Reordering PDF pages at the same time

```dart
String? rotatedDeletedReorderedPagesPdfPath = await PdfManipulator().pdfPageRotatorDeleterReorder(
  params: PDFPageRotatorDeleterReorderParams(
      pdfPath: pdfPath,
      pagesRotationInfo: [PageRotationInfo(pageNumber: 1, rotationAngle: 180)],
      pageNumbersForReorder: [4, 3, 2, 1],
      pageNumbersForDeleter: [3, 2]),
);
```

### Compressing PDF

```dart
String? compressedPdfPath = await PdfManipulator().pdfCompressor(
  params: PDFCompressorParams(pdfPath: pdfPath, imageQuality: 100, imageScale: 1),
);
```

### Watermarking PDF

```dart
String? watermarkedPdfPath = await PdfManipulator().pdfWatermark(
  params: PDFWatermarkParams(
      pdfPath: pdfPath,
      text: "Watermark Text",
      watermarkColor: Colors.red,
      fontSize: 50,
      watermarkLayer: WatermarkLayer.overContent,
      opacity: 0.7,
      positionType: PositionType.center),
);
```

**Note:** When using `PositionType.custom` you need to provide `customPositionXCoordinatesList` and `customPositionYCoordinatesList`.

### Encrypting PDF

```dart
String? encryptedPdfPath = await PdfManipulator().pdfEncryption(
  params: PDFEncryptionParams(
      pdfPath: pdfPath,
      ownerPassword: "ownerpw",
      userPassword: "userpw",
      encryptionAES256: true // Set true to enable encryptionAES256 encryption.
);
```

`PDFEncryptionParams` other parameters with their default values is as follows:-
- `bool allowPrinting = false` Set true to allow printing permission.
- `bool allowModifyContents = false` Set true to allow modify permission.
- `bool allowCopy = false` Set true to allow copy permission.
- `bool allowModifyAnnotations = false` Set true to allow modifying annotations permission.
- `bool allowFillIn = false` Set true to allow fill in permission.
- `bool allowScreenReaders = false` Set true to allow screen readers permission.
- `bool allowAssembly = false` Set true to allow assembly permission.
- `bool allowDegradedPrinting = false` Set true to allow degraded printing permission.
- `bool standardEncryptionAES40 = false` Set true to enable StandardEncryptionAES40 encryption. standardEncryptionAES40 implicitly sets doNotEncryptMetadata and encryptEmbeddedFilesOnly as false.
- `bool standardEncryptionAES128 = false` Set true to enable StandardEncryptionAES128 encryption. standardEncryptionAES128 implicitly sets EncryptionConstants.EMBEDDED_FILES_ONLY as false.
- `bool encryptionAES128 = false` Set true to enable encryptionAES128 encryption.
- `bool encryptEmbeddedFilesOnly = false` Set true to encrypt embedded files only.
- `bool doNotEncryptMetadata = false` Set true to not encrypt metadata.

**Note:** Please be aware that the passed encryption types may override permissions.

### Decrypting PDF

```dart
String? decryptedPdfPath = await PdfManipulator().pdfDecryption(
  params: PDFDecryptionParams(
      pdfPath: pdfPath,
      password: ownerOrUserPassword,
);
```

### Converting images to PDF

```dart
List<String>? pdfsPaths = await PdfManipulator().imagesToPdfs(
  params: ImagesToPDFsParams(
      imagesPaths: imagesPaths,
      createSinglePdf: false,
);
```

Images in JPEG, JPEG2000, GIF, PNG, BMP, WMF, TIFF, CCITT and JBIG2 formats are supported.

### PDF validity and protection info

```dart
PdfValidityAndProtection? pdfValidityAndProtectionInfo = await PdfManipulator().pdfValidityAndProtection(
  params: PDFValidityAndProtectionParams(
      pdfPath: pdfPath,
);

/// Getting info.
bool? isPDFValid = pdfValidityAndProtectionInfo?.isPDFValid;
bool? isOwnerPasswordProtected = pdfValidityAndProtectionInfo?.isOwnerPasswordProtected;
bool? isOpenPasswordProtected = pdfValidityAndProtectionInfo?.isOpenPasswordProtected;
bool? isPrintingAllowed = pdfValidityAndProtectionInfo?.isPrintingAllowed;
bool? isModifyContentsAllowed = pdfValidityAndProtectionInfo?.isModifyContentsAllowed;
```
Don't provide password if you just want to check validity and protection. Only provide password if you want to check if that password is correct or not.

**Note:** If you only want to check validity and protection then I suggest to use [pdf_bitmaps](https://pub.dev/packages/pdf_bitmaps) as that is fast and requires less memory.

### PDF page size info

```dart
List<PageSizeInfo>? pdfPagesSizeInfo = await PdfManipulator().pdfPagesSize(
  params: PDFPagesSizeParams(
      pdfPath: pdfPath,
);

/// Getting 1st page info.
double? widthOfPage = pdfPagesSizeInfo[0]?.widthOfPage;
double? heightOfPage = pdfPagesSizeInfo[0]?.heightOfPage;
```

**Note:** If you only want to get page size then I suggest to use [pdf_bitmaps](https://pub.dev/packages/pdf_bitmaps) as that is fast and requires less memory.

### Converting PDF pages to images

```dart
List<String>? imagePaths = await PdfManipulator().pdfToImages(
  params: PDFToImagesParams(
      pdfPath: pdfPath,
      pages: [1, 2, 3], // Optional: specific pages, empty for all pages
      imageFormat: ImageFormat.png, // Optional: png, jpeg, webp
      quality: 90, // Optional: JPEG/WebP quality (1-100)
      scale: 1.0, // Optional: scale factor (0.1-5.0)
);
```

Supported image formats: PNG, JPEG, WebP.

### Extracting text from PDF

```dart
PDFTextExtractionResult? textResult = await PdfManipulator().pdfTextExtraction(
  params: PDFTextExtractionParams(
      pdfPath: pdfPath,
      pages: [1, 2, 3], // Optional: specific pages, empty for all pages
);
 
/// Getting extracted text
String? fullText = textResult?.fullText; // All text concatenated
Map<int, String>? pageTexts = textResult?.pageTexts; // Text per page
String? page1Text = pageTexts?[1]; // Text from page 1
```

### Performing OCR on PDF

```dart
PDFOCRResult? ocrResult = await PdfManipulator().pdfOcr(
  params: PDFOCRParams(
      pdfPath: pdfPath,
      pages: [1, 2, 3], // Optional: specific pages, empty for all pages
      languageCode: 'en', // Optional: language code, default 'en'
);
 
/// Getting OCR results
String? fullOcrText = ocrResult?.fullText; // All OCR text concatenated
Map<int, OCRPageResult>? pageOcrResults = ocrResult?.pageResults; // OCR results per page
OCRPageResult? page1Result = pageOcrResults?[1]; // OCR result for page 1
String? page1Text = page1Result?.text; // Recognized text
double? page1Confidence = page1Result?.confidence; // Confidence score (0.0-1.0)
```

### Adding digital signatures to PDF

```dart
String? signedPdfPath = await PdfManipulator().pdfDigitalSignature(
  params: PDFDigitalSignatureParams(
      pdfPath: pdfPath,
      certificatePath: certificatePath, // Path to .p12 or .pfx certificate file
      certificatePassword: "certificate_password",
      reason: "Document approval", // Optional
      location: "New York", // Optional
      contact: "john@example.com", // Optional
      appearance: SignatureAppearance( // Optional
        text: "John Doe\nApproved",
        x: 100,
        y: 100,
        width: 200,
        height: 100,
        pageNumber: 1,
      ),
);
```

### Filling PDF form fields

```dart
String? filledPdfPath = await PdfManipulator().fillFormFields(
  params: PDFFormFillParams(
      pdfPath: pdfPath,
      fieldValues: {
        "name": "Jane Doe",
        "email": "jane.doe@example.com",
        "subscribe": true, // Checkbox fields can use bool values.
      },
      flatten: false, // Optional: set true to make fields non-editable
);
```

### Extracting PDF form field data

```dart
PDFFormFieldData? formData = await PdfManipulator().extractFormFieldData(
  params: PDFFormFieldDataParams(pdfPath: pdfPath),
);

/// Getting form field info.
Map<String, PDFFormField>? fields = formData?.fields;
PDFFormField? nameField = fields?["name"];
String? currentValue = nameField?.value;
String? fieldType = nameField?.type; // text, checkbox, radio, combo, list, etc.
List<String>? options = nameField?.options; // Choice/checkbox/radio options.
bool? isRequired = nameField?.isRequired;
```

### Adding annotations to PDF

```dart
String? annotatedPdfPath = await PdfManipulator().pdfAnnotations(
  params: PDFAnnotationsParams(
      pdfPath: pdfPath,
      annotations: [
        // Text annotation (sticky note)
        TextAnnotation(
          pageNumber: 1,
          rect: [100, 200, 200, 250], // x, y, width, height
          contents: "This is a note",
          title: "Note",
          color: [1.0, 1.0, 0.0], // Yellow
        ),
        // Highlight annotation
        HighlightAnnotation(
          pageNumber: 1,
          rect: [50, 150, 300, 170],
          quads: [[50, 170, 300, 170, 50, 150, 300, 150]], // Highlight area
          contents: "Important text",
          color: [1.0, 1.0, 0.0], // Yellow
        ),
        // Link annotation
        LinkAnnotation(
          pageNumber: 1,
          rect: [150, 100, 250, 120],
          url: "https://example.com",
          contents: "Click here",
        ),
      ],
);
```

### Reading PDF metadata

```dart
PDFMetadataResult? metadata = await PdfManipulator().pdfMetadataReader(
  params: PDFMetadataReaderParams(pdfPath: pdfPath),
);

String? title = metadata?.title;
String? author = metadata?.author;
String? subject = metadata?.subject;
String? keywords = metadata?.keywords;
String? creator = metadata?.creator;
String? producer = metadata?.producer;
String? creationDate = metadata?.creationDate; // ISO 8601 format
String? modificationDate = metadata?.modificationDate; // ISO 8601 format
```

### Modifying PDF metadata

```dart
String? updatedPdfPath = await PdfManipulator().pdfMetadataWriter(
  params: PDFMetadataWriterParams(
    pdfPath: pdfPath,
    title: "Updated Title",
    author: "New Author",
    subject: "Updated Subject",
    keywords: "keyword1, keyword2, keyword3",
    creator: "My App",
    producer: "PDF Manipulator Plugin",
    creationDate: "2024-01-01T10:00:00Z", // ISO 8601 format
    modificationDate: "2024-12-31T23:59:59Z", // ISO 8601 format
  ),
);
```

### Reading PDF bookmarks

```dart
PDFBookmarkData? bookmarkData = await PdfManipulator().pdfBookmarkReader(
  params: PDFBookmarkReaderParams(pdfPath: pdfPath),
);

for (var bookmark in bookmarkData?.bookmarks ?? []) {
  print('Bookmark: ${bookmark.title} -> Page ${bookmark.pageNumber}');
  // Process child bookmarks recursively
  processBookmarkChildren(bookmark.children);
}

void processBookmarkChildren(List<PDFBookmark> children) {
  for (var child in children) {
    print('  Child: ${child.title} -> Page ${child.pageNumber}');
    processBookmarkChildren(child.children);
  }
}
```

### Creating PDF bookmarks

```dart
String? bookmarkedPdfPath = await PdfManipulator().pdfBookmarkWriter(
  params: PDFBookmarkWriterParams(
    pdfPath: pdfPath,
    bookmarks: [
      PDFBookmark(
        title: "Chapter 1",
        pageNumber: 1,
        children: [
          PDFBookmark(title: "Section 1.1", pageNumber: 2),
          PDFBookmark(title: "Section 1.2", pageNumber: 5),
        ],
      ),
      PDFBookmark(
        title: "Chapter 2",
        pageNumber: 10,
        children: [
          PDFBookmark(title: "Section 2.1", pageNumber: 12),
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
                "PDF Repair",
                style: Theme.of(context).textTheme.titleLarge,
              ),
              Card(
                margin: EdgeInsets.zero,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Column(
                    children: [
                      Text(
                        "Repair Corrupted PDF",
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Pick corrupted PDF to repair',
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
                                      _pickedFilePathForRepair = result[0];
                                    });
                                  }
                                }),
                      if (_pickedFilePathForRepair != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 8.0),
                          child: Text(
                            "Selected: ${_pickedFilePathForRepair!.split('/').last}",
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ),
                      const SizedBox(height: 8),
                      CustomButton(
                          buttonText: 'Attempt Repair',
                          onPressed: _pickedFilePathForRepair == null
                              ? null
                              : () async {
                                  final params = PDFRepairParams(
                                    pdfPath: _pickedFilePathForRepair!,
                                  );

                                  PDFRepairResult? result =
                                      await _pdfRepair(params);

                                  if (mounted) {
                                    setState(() {
                                      _repairResult = result;
                                    });
                                  }
                                }),
                      if (_repairResult != null)
                        Row(
                          children: [
                            Expanded(
                              child: CustomButton(
                                  buttonText: 'Save Repaired PDF',
                                  onPressed: _repairResult!.repairedPdfPath == null
                                      ? null
                                      : () async {
                                          final params = FileSaverParams(
                                            localOnly: _localOnly,
                                            saveFiles: [
                                              SaveFileInfo(
                                                  filePath: _repairResult!.repairedPdfPath,
                                                  fileName: "Repaired PDF.pdf")
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
                                  _repairResult = null;
                                });
                              },
                              icon: const Icon(Icons.clear),
                            ),
                          ],
                        ),

                      // Results display
                      if (_repairResult != null) ...[
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
                                  // Repair status
                                  Text(
                                    "Repair Status: ${_repairResult!.wasRepaired ? 'SUCCESS' : 'FAILED'}",
                                    style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      color: _repairResult!.wasRepaired ? Colors.green : Colors.red,
                                    ),
                                  ),
                                  const SizedBox(height: 8),

                                  // Corruption analysis
                                  Text(
                                    "Original PDF Status:",
                                    style: Theme.of(context).textTheme.titleSmall,
                                  ),
                                  Text("Can Open: ${_repairResult!.originalStatus.canOpen}"),
                                  Text("Valid Structure: ${_repairResult!.originalStatus.hasValidStructure}"),
                                  Text("Readable Content: ${_repairResult!.originalStatus.hasReadableContent}"),
                                  Text("Corruption Level: ${(_repairResult!.originalStatus.corruptionLevel * 100).toInt()}%"),

                                  if (_repairResult!.originalStatus.detectedIssues.isNotEmpty) ...[
                                    const SizedBox(height: 8),
                                    Text(
                                      "Detected Issues:",
                                      style: Theme.of(context).textTheme.titleSmall,
                                    ),
                                    ..._repairResult!.originalStatus.detectedIssues.map((issue) =>
                                      Text("• $issue", style: TextStyle(color: Colors.red))
                                    ),
                                  ],

                                  // Repair status
                                  const SizedBox(height: 12),
                                  Text(
                                    "Repair Operation:",
                                    style: Theme.of(context).textTheme.titleSmall,
                                  ),
                                  Text("Completed: ${_repairResult!.repairStatus.completed}"),
                                  Text("Content Recovered: ${_repairResult!.repairStatus.contentRecovered}"),
                                  Text("Fully Functional: ${_repairResult!.repairStatus.fullyFunctional}"),
                                  Text("Method: ${_repairResult!.repairStatus.repairMethod}"),

                                  if (_repairResult!.repairStatus.repairInfo.isNotEmpty) ...[
                                    const SizedBox(height: 8),
                                    ..._repairResult!.repairStatus.repairInfo.map((info) =>
                                      Text("• $info", style: TextStyle(color: Colors.blue))
                                    ),
                                  ],

                                  // Recovered content
                                  if (_repairResult!.recoveredContent != null) ...[
                                    const SizedBox(height: 12),
                                    Text(
                                      "Recovered Content:",
                                      style: Theme.of(context).textTheme.titleSmall,
                                    ),
                                    Text("Pages: ${_repairResult!.recoveredContent!.pagesRecovered}"),
                                    Text("Text Length: ${_repairResult!.recoveredContent!.textContentLength} characters"),
                                    Text("Images: ${_repairResult!.recoveredContent!.imagesRecovered}"),
                                    Text("Metadata Preserved: ${_repairResult!.recoveredContent!.metadataPreserved}"),
                                  ],

                                  // General issues
                                  if (_repairResult!.issues.isNotEmpty) ...[
                                    const SizedBox(height: 12),
                                    Text(
                                      "Repair Issues:",
                                      style: Theme.of(context).textTheme.titleSmall,
                                    ),
                                    ..._repairResult!.issues.map((issue) =>
                                      Text("• $issue", style: TextStyle(color: Colors.orange))
                                    ),
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
);
```

### Comparing two PDFs

```dart
PDFComparisonResult? comparisonResult = await PdfManipulator().pdfComparison(
  params: PDFComparisonParams(
    pdfPath1: pdfPath1,
    pdfPath2: pdfPath2,
    compareText: true,        // Compare text content
    compareMetadata: true,    // Compare metadata
    compareStructure: true,   // Compare page count and structure
  ),
);

// Overall similarity score
double similarity = comparisonResult!.overallSimilarity; // 0.0 to 1.0

// Summary of differences
List<String> summary = comparisonResult.summary;

// Detailed results
PDFTextComparison? textComparison = comparisonResult.textComparison;
PDFMetadataComparison? metadataComparison = comparisonResult.metadataComparison;
PDFStructureComparison? structureComparison = comparisonResult.structureComparison;

// Text comparison details
if (textComparison != null) {
  double textSimilarity = textComparison.similarity;
  List<TextDifference> textDifferences = textComparison.differences;
}

// Metadata comparison details
if (metadataComparison != null) {
  List<MetadataDifference> metadataDifferences = metadataComparison.differences;
  for (var diff in metadataDifferences) {
    print('${diff.field}: "${diff.value1}" vs "${diff.value2}"');
  }
}

// Structure comparison details
if (structureComparison != null) {
  bool pageCountEqual = structureComparison.pageCountEqual;
  int pageCount1 = structureComparison.pageCount1;
  int pageCount2 = structureComparison.pageCount2;
}
```

### Repairing corrupted PDFs

```dart
PDFRepairResult? repairResult = await PdfManipulator().pdfRepair(
  params: PDFRepairParams(pdfPath: corruptedPdfPath),
);

// Check if repair was successful
bool wasRepaired = repairResult!.wasRepaired;

// Get path to repaired PDF (if successful)
String? repairedPdfPath = repairResult.repairedPdfPath;

// Analyze original corruption status
PDFCorruptionStatus originalStatus = repairResult.originalStatus;
bool canOpen = originalStatus.canOpen;
double corruptionLevel = originalStatus.corruptionLevel; // 0.0 to 1.0
List<String> detectedIssues = originalStatus.detectedIssues;

// Check repair operation status
PDFRepairStatus repairStatus = repairResult.repairStatus;
bool repairCompleted = repairStatus.completed;
bool contentRecovered = repairStatus.contentRecovered;
bool fullyFunctional = repairStatus.fullyFunctional;
String repairMethod = repairStatus.repairMethod;

// Get information about recovered content
PDFRecoveredContent? recoveredContent = repairResult.recoveredContent;
if (recoveredContent != null) {
  int pagesRecovered = recoveredContent.pagesRecovered;
  int textLength = recoveredContent.textContentLength;
  int imagesRecovered = recoveredContent.imagesRecovered;
  bool metadataPreserved = recoveredContent.metadataPreserved;
}

// Get any issues encountered during repair
List<String> repairIssues = repairResult.issues;
```

Note: To try the demos shown in below images run the example included in this plugin.

<img src="https://user-images.githubusercontent.com/85361211/201522048-1f0c9cd3-e25e-4304-bae9-3673097bfbf1.jpeg" width="20%"></img> <img src="https://user-images.githubusercontent.com/85361211/201522051-db1595b6-d229-4e46-a765-1d09728a5f8c.jpeg" width="20%"></img> <img src="https://user-images.githubusercontent.com/85361211/201522053-7fbf7531-9264-4831-97aa-6d068ee18c4a.jpeg" width="20%"></img> <img src="https://user-images.githubusercontent.com/85361211/201522055-161fd60b-b656-4db3-b310-0b2c1dcb7e67.jpeg" width="20%"></img>
