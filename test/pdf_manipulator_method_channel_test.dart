import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pdf_manipulator/pdf_manipulator.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('pdf_manipulator');
  final calls = <MethodCall>[];

  setUp(() {
    calls.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (methodCall) async {
      calls.add(methodCall);

      return switch (methodCall.method) {
        'mergePDFs' => '/tmp/merged.pdf',
        'splitPDF' => <String>['/tmp/page-1.pdf', '/tmp/page-2.pdf'],
        'pdfCompressor' => '/tmp/compressed.pdf',
        _ => null,
      };
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('mergePDFs forwards parameters to the method channel', () async {
    final result = await PdfManipulator().mergePDFs(
      params: PDFMergerParams(pdfsPaths: ['/tmp/a.pdf', '/tmp/b.pdf']),
    );

    expect(result, '/tmp/merged.pdf');
    expect(calls.single.method, 'mergePDFs');
    expect(calls.single.arguments, {
      'pdfsPaths': ['/tmp/a.pdf', '/tmp/b.pdf'],
    });
  });

  test('splitPDF casts returned paths', () async {
    final result = await PdfManipulator().splitPDF(
      params: const PDFSplitterParams(pdfPath: '/tmp/a.pdf', pageCount: 1),
    );

    expect(result, ['/tmp/page-1.pdf', '/tmp/page-2.pdf']);
    expect(calls.single.method, 'splitPDF');
  });

  test('pdfCompressor returns result and generated operation id', () async {
    final result = await PdfManipulator().pdfCompressor(
      params: const PDFCompressorParams(
        pdfPath: '/tmp/a.pdf',
        imageQuality: 80,
        imageScale: 0.5,
      ),
    );

    expect(result.result, '/tmp/compressed.pdf');
    expect(result.operationId, isNotEmpty);
    expect(calls.single.method, 'pdfCompressor');
    expect(calls.single.arguments, containsPair('pdfPath', '/tmp/a.pdf'));
    expect(calls.single.arguments, contains('operationId'));
  });

  test('batchProcess runs operations sequentially and reports progress', () async {
    final progress = <String>[];

    final result = await PdfManipulator().batchProcess(
      params: PDFBatchProcessorParams(
        operations: [
          PDFBatchOperation(
            id: 'merge-docs',
            type: PDFBatchOperationType.merge,
            params: PDFMergerParams(pdfsPaths: ['/tmp/a.pdf', '/tmp/b.pdf']),
          ),
          const PDFBatchOperation(
            id: 'split-doc',
            type: PDFBatchOperationType.split,
            params: PDFSplitterParams(pdfPath: '/tmp/merged.pdf', pageCount: 1),
          ),
        ],
      ),
      onProgress: (completed, total, operation) {
        progress.add('$completed/$total:${operation.id}');
      },
    );

    expect(result.success, isTrue);
    expect(result.results, hasLength(2));
    expect(result.results.first.result, '/tmp/merged.pdf');
    expect(result.results.last.result, ['/tmp/page-1.pdf', '/tmp/page-2.pdf']);
    expect(calls.map((call) => call.method), ['mergePDFs', 'splitPDF']);
    expect(progress, ['1/2:merge-docs', '2/2:split-doc']);
  });

  test('batchProcess stops after the first error by default', () async {
    final result = await PdfManipulator().batchProcess(
      params: PDFBatchProcessorParams(
        operations: [
          const PDFBatchOperation(
            id: 'bad-op',
            type: PDFBatchOperationType.split,
            params: 'wrong params',
          ),
          PDFBatchOperation(
            id: 'merge-docs',
            type: PDFBatchOperationType.merge,
            params: PDFMergerParams(pdfsPaths: ['/tmp/a.pdf', '/tmp/b.pdf']),
          ),
        ],
      ),
    );

    expect(result.success, isFalse);
    expect(result.results, hasLength(1));
    expect(result.results.single.id, 'bad-op');
    expect(result.results.single.success, isFalse);
    expect(calls, isEmpty);
  });
}
