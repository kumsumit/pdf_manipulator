I checked our repo against current PDF editor feature sets from Adobe Acrobat, Foxit, and Nitro. Those products consistently advertise: edit text/images, organize pages, export/convert, OCR, forms, e-sign/signature workflows, protect/encrypt, redact, compare, accessibility, and PDF standards support. Sources: [Adobe Acrobat features](https://www.adobe.com/acrobat/features.html), [Foxit PDF Editor](https://www.foxit.com/PDF-editor/), [Foxit protect/sign/redact/OCR/accessibility](https://www.foxit.com/pdf-editor/protect-sign-pdf/), [Nitro PDF Pro guide](https://www.gonitro.com/user-guide/mac/article/introduction).

**Already Supported**
This package already has a strong manipulation core: merge, split, rotate, delete, reorder, compress, optimize, watermark, encrypt/decrypt, certificate encryption, images to PDF, PDF to images, text extraction, OCR text result, digital signing, annotations, form filling, form field extraction, metadata read/write, bookmarks read/write, comparison, repair, page size info, validity/protection info, cancellation, and batch processing.

**Basic Features Still Lacking**
- PDF viewer/rendering UI: page preview, zoom, scroll, thumbnails, search/select text.
- Create blank PDF from scratch.
- Add/insert blank pages.
- Insert pages from another PDF at a specific position.
- Replace pages.
- Duplicate pages.
- Extract selected pages as one new PDF with clearer API naming.
- Crop pages.
- Resize pages / change page boxes / normalize page sizes.
- Add page numbers.
- Add headers and footers.
- Add backgrounds.
- Add stamps.
- Edit existing PDF text.
- Edit existing PDF images.
- Add arbitrary text blocks/content to pages.
- Add arbitrary images to pages.
- Remove existing annotations.
- Modify existing annotations.
- Flatten annotations.
- Flatten an entire PDF.
- Basic print/share helpers, if this plugin wants app-level utility features.
- Better page rearrange API helpers: move page, swap pages, reverse pages, page order validation.

**Conversion Features Lacking**
- PDF to Word/docx.
- PDF to Excel/xlsx.
- PDF to PowerPoint/pptx.
- PDF to HTML.
- PDF to plain text file.
- Word/Excel/PowerPoint/HTML/text to PDF.
- Scanner/camera image pipeline to PDF with cleanup.
- PDF/A conversion.
- PDF/A validation.
- More image input formats for image-to-PDF: WebP, HEIC/HEIF, TIFF.
- Export selected embedded images with metadata and format controls.

**Advanced Editor Features Lacking**
- True redaction: permanently remove text/images/regions.
- Search-based redaction.
- Pattern redaction: emails, phone numbers, SSNs, account numbers.
- Sanitization/remove hidden information: metadata, hidden text, embedded files, JavaScript, comments, thumbnails.
- OCR-to-searchable-PDF: current OCR returns recognized text, but does not appear to write an invisible text layer back into the PDF.
- OCR cleanup: deskew, despeckle, auto-rotate, contrast cleanup.
- OCR language packs / multi-language OCR controls.
- Form creation: add text fields, checkboxes, radio buttons, dropdowns, signature fields.
- Form editing/removing fields.
- XFA form support.
- Signature verification.
- Signature certificate chain validation.
- Timestamp/LTV signature support.
- Multiple signatures / incremental signing workflow.
- E-sign workflow: request signatures, signer fields, status tracking.
- PDF attachments: add, list, extract, remove embedded files.
- PDF portfolios/packages.
- Layers / optional content groups.
- Article threads.
- Named destinations.
- Page labels.
- Link editing: add/remove internal and external links.
- Table extraction.
- Structured text extraction with coordinates, font info, bounding boxes.
- Visual diff output PDF. Current comparison returns result data; advanced editors usually generate highlighted comparison output.
- Accessibility: tag tree read/write, reading order, alt text, headings, table structure.
- PDF/UA validation and auto-fix.
- Bates numbering.
- Document legal workflows: numbering, exhibit labels, batch stamping.
- Advanced bookmark actions: URLs, named destinations, fit modes, colors/styles.
- JavaScript/action support: inspect/remove document actions.
- Embedded media/rich content handling.
- Incremental save / append mode where possible.
- Linearized “fast web view” output.
- Digital rights / permission inspection beyond current basic protection info.

**Platform/Quality Gaps**
- iOS support is missing; repo only has Android native implementation.
- Web/desktop support is missing.
- Some old `todo.md` items are stale, but still flags useful gaps: PDF/A, PDF/UA, image extraction quality, progress/error handling, large-PDF memory handling.
- Error model could be richer: typed errors, page-number validation errors, password errors, corruption errors.
- More progress callbacks across all long-running operations, not just compression.
- More cancellation coverage across all operations.

**Highest-Value Next Features**
1. Crop/resize/insert/replace/duplicate/extract pages.
2. Edit text/images and add text/images.
3. Redaction plus sanitization.
4. Searchable OCR PDF output.
5. Signature verification.
6. Form creation/editing.
7. PDF/A and PDF/UA validation/conversion.
8. iOS implementation.
9. PDF to Office/HTML/text conversion.
10. Visual highlighted comparison PDF.