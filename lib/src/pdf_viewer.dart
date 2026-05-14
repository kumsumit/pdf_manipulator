import 'dart:io';

import 'package:flutter/material.dart';

import 'pdf_manipulator.dart';
import 'pdf_manipulator_method_channel.dart';

class PdfManipulatorViewer extends StatefulWidget {
  final String pdfPath;
  final double initialZoom;
  final ImageFormat imageFormat;
  final double renderScale;
  final bool showThumbnails;
  final bool enableTextPanel;

  const PdfManipulatorViewer({
    super.key,
    required this.pdfPath,
    this.initialZoom = 1,
    this.imageFormat = ImageFormat.png,
    this.renderScale = 2,
    this.showThumbnails = true,
    this.enableTextPanel = true,
  });

  @override
  State<PdfManipulatorViewer> createState() => _PdfManipulatorViewerState();
}

class _PdfManipulatorViewerState extends State<PdfManipulatorViewer> {
  final _controller = ScrollController();
  final _searchController = TextEditingController();
  final _manipulator = PdfManipulator();
  List<String> _pageImages = const [];
  PDFTextExtractionResult? _text;
  String? _error;
  bool _loading = true;
  double _zoom = 1;
  int _selectedPage = 0;

  @override
  void initState() {
    super.initState();
    _zoom = widget.initialZoom;
    _load();
  }

  @override
  void dispose() {
    _controller.dispose();
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final images = await _manipulator.pdfToImages(
        params: PDFToImagesParams(
          pdfPath: widget.pdfPath,
          imageFormat: widget.imageFormat,
          scale: widget.renderScale,
        ),
      );
      final text = widget.enableTextPanel
          ? await _manipulator.pdfTextExtraction(
              params: PDFTextExtractionParams(pdfPath: widget.pdfPath),
            )
          : null;
      if (!mounted) return;
      setState(() {
        _pageImages = images ?? const [];
        _text = text;
        _loading = false;
      });
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _error = error.toString();
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());
    if (_error != null) return Center(child: Text(_error!));

    return Column(
      children: [
        _Toolbar(
          zoom: _zoom,
          searchController: _searchController,
          onZoomChanged: (value) => setState(() => _zoom = value),
          onSearch: _jumpToSearchResult,
        ),
        Expanded(
          child: Row(
            children: [
              if (widget.showThumbnails)
                SizedBox(
                  width: 96,
                  child: ListView.builder(
                    itemCount: _pageImages.length,
                    itemBuilder: (context, index) {
                      return InkWell(
                        onTap: () => _jumpToPage(index),
                        child: Container(
                          decoration: BoxDecoration(
                            border: Border.all(
                              color: index == _selectedPage
                                  ? Theme.of(context).colorScheme.primary
                                  : Colors.transparent,
                            ),
                          ),
                          margin: const EdgeInsets.all(6),
                          child: Image.file(File(_pageImages[index])),
                        ),
                      );
                    },
                  ),
                ),
              Expanded(
                child: SingleChildScrollView(
                  controller: _controller,
                  child: Column(
                    children: [
                      for (final image in _pageImages)
                        Padding(
                          padding: const EdgeInsets.all(12),
                          child: InteractiveViewer(
                            minScale: .5,
                            maxScale: 5,
                            child: Transform.scale(
                              scale: _zoom,
                              child: Image.file(File(image)),
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              if (widget.enableTextPanel)
                SizedBox(
                  width: 260,
                  child: ListView(
                    padding: const EdgeInsets.all(12),
                    children: [
                      for (final entry
                          in _text?.pageTexts.entries ??
                              const <MapEntry<int, String>>[])
                        Padding(
                          padding: const EdgeInsets.only(bottom: 16),
                          child: SelectableText(
                            'Page ${entry.key}\n${entry.value}',
                          ),
                        ),
                    ],
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }

  void _jumpToPage(int index) {
    setState(() => _selectedPage = index);
    final estimatedOffset = index * 760.0 * _zoom;
    _controller.animateTo(
      estimatedOffset,
      duration: const Duration(milliseconds: 220),
      curve: Curves.easeOut,
    );
  }

  void _jumpToSearchResult() {
    final query = _searchController.text.trim().toLowerCase();
    if (query.isEmpty || _text == null) return;
    int? match;
    for (final entry in _text!.pageTexts.entries) {
      if (entry.value.toLowerCase().contains(query)) {
        match = entry.key - 1;
        break;
      }
    }
    if (match != null) _jumpToPage(match);
  }
}

class _Toolbar extends StatelessWidget {
  final double zoom;
  final TextEditingController searchController;
  final ValueChanged<double> onZoomChanged;
  final VoidCallback onSearch;

  const _Toolbar({
    required this.zoom,
    required this.searchController,
    required this.onZoomChanged,
    required this.onSearch,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      elevation: 1,
      child: SizedBox(
        height: 56,
        child: Row(
          children: [
            IconButton(
              tooltip: 'Zoom out',
              onPressed: () => onZoomChanged((zoom - .1).clamp(.5, 5)),
              icon: const Icon(Icons.remove),
            ),
            SizedBox(
              width: 140,
              child: Slider(
                value: zoom.clamp(.5, 5),
                min: .5,
                max: 5,
                onChanged: onZoomChanged,
              ),
            ),
            IconButton(
              tooltip: 'Zoom in',
              onPressed: () => onZoomChanged((zoom + .1).clamp(.5, 5)),
              icon: const Icon(Icons.add),
            ),
            const VerticalDivider(width: 24),
            Expanded(
              child: TextField(
                controller: searchController,
                decoration: const InputDecoration(
                  hintText: 'Search text',
                  border: InputBorder.none,
                ),
                onSubmitted: (_) => onSearch(),
              ),
            ),
            IconButton(
              tooltip: 'Search',
              onPressed: onSearch,
              icon: const Icon(Icons.search),
            ),
          ],
        ),
      ),
    );
  }
}
