package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.net.toUri
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.*
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File


enum class WatermarkLayer {
    UnderContent, OverContent
}

enum class PositionType {
    TopLeft, TopCenter, TopRight, CenterLeft, Center, CenterRight, BottomLeft, BottomCenter, BottomRight, Custom
}

// For compressing pdf.
suspend fun getWatermarkedPDFPath(
    sourceFilePath: String,
    text: String?,
    imagePath: String?,
    fontSize: Double,
    watermarkLayer: WatermarkLayer,
    opacity: Double,
    rotationAngle: Double,
    watermarkColor: String,
    positionType: PositionType,
    customPositionXCoordinatesList: List<Double>,
    customPositionYCoordinatesList: List<Double>,
    imageWidth: Double?,
    imageHeight: Double?,
    context: Activity,
): String? {

    val resultPDFPath: String?

    withContext(Dispatchers.IO) {

        val utils = Utils()

        val begin = System.nanoTime()

        val contentResolver: ContentResolver = context.contentResolver

        val uri = Utils().getURI(sourceFilePath)

        val pdfReaderFile: File = File.createTempFile("readerTempFile", ".pdf")
        utils.copyDataFromSourceToDestDocument(
            sourceFileUri = uri,
            destinationFileUri = pdfReaderFile.toUri(),
            contentResolver = contentResolver
        )

        val pdfReader = PdfReader(pdfReaderFile).setUnethicalReading(true)
        pdfReader.setMemorySavingMode(true)

        val pdfWriterFile: File = File.createTempFile("writerTempFile", ".pdf")

        val pdfWriter = PdfWriter(pdfWriterFile)

        pdfWriter.setSmartMode(true)
        pdfWriter.compressionLevel = 9

        val pdfDocument = PdfDocument(pdfReader, pdfWriter)

        val memoryManager = MemoryManager()
        memoryManager.logMemoryUsage("Watermark start")

        fun watermark() {

            var position: PositionType = positionType

            if (position == PositionType.Custom) {
                if (!(customPositionXCoordinatesList.size == pdfDocument.numberOfPages && customPositionYCoordinatesList.size == pdfDocument.numberOfPages)) {
                    Log.e(
                        "Warning",
                        "customPositionXCoordinatesList or customPositionYCoordinatesList length is not equal to the total number of pages so assigning positionType to PositionType.center"
                    )
                    position = PositionType.Center
                }
            }

            // Determine if we're using text or image watermark
            val isImageWatermark = !imagePath.isNullOrEmpty()

            // Process pages in chunks for memory efficiency
            val totalPages = pdfDocument.numberOfPages
            val chunkSize = memoryManager.getOptimalChunkSize(totalPages)

            Log.d("MemoryManager", "Processing $totalPages pages in chunks of $chunkSize")

            (1..totalPages step chunkSize).forEach { startPage ->
                val endPage = min(startPage + chunkSize - 1, totalPages)
                memoryManager.logMemoryUsage("Processing pages $startPage-$endPage")

                for (i in startPage..endPage) {
                    yield()

                    // Check memory before processing each page
                    memoryManager.forceGarbageCollectionIfNeeded()

                    try {
                        val pdfPage: PdfPage = pdfDocument.getPage(i)
                        val pageSize: Rectangle = pdfPage.pageSizeWithRotation

                        // When "true": in case the page has a rotation, then new content will be automatically rotated in the
                        // opposite direction. On the rotated page this would look as if new content ignores page rotation.
                        pdfPage.isIgnorePageRotationForContent = true

                        val layer = if (watermarkLayer == WatermarkLayer.UnderContent) {
                            PdfCanvas(
                                pdfPage.newContentStreamBefore(), PdfResources(), pdfDocument
                            )
                        } else {
                            PdfCanvas(pdfPage)
                        }

                        layer.saveState()
                        // Creating a dictionary that maps resource names to graphics state parameter dictionaries
                        val gs1 = PdfExtGState()
                        gs1.fillOpacity = opacity.toFloat()
                        layer.setExtGState(gs1)

                val x: Float
                val y: Float

                when (position) {
                    PositionType.TopLeft -> {
                        x = (0).toFloat()
                        y = pageSize.height
                    }
                    PositionType.TopCenter -> {
                        x = pageSize.width / 2
                        y = pageSize.height
                    }
                    PositionType.TopRight -> {
                        x = pageSize.width
                        y = pageSize.height
                    }
                    PositionType.CenterLeft -> {
                        x = (0).toFloat()
                        y = pageSize.height / 2
                    }
                    PositionType.Center -> {
                        x = pageSize.width / 2
                        y = pageSize.height / 2
                    }
                    PositionType.CenterRight -> {
                        x = pageSize.width
                        y = pageSize.height / 2
                    }
                    PositionType.BottomLeft -> {
                        x = (0).toFloat()
                        y = (0).toFloat()
                    }
                    PositionType.BottomCenter -> {
                        x = pageSize.width / 2
                        y = (0).toFloat()
                    }
                    PositionType.BottomRight -> {
                        x = pageSize.width
                        y = (0).toFloat()
                    }
                    else -> {
                        x = customPositionXCoordinatesList[i - 1].toFloat()
                        y = customPositionYCoordinatesList[i - 1].toFloat()
                    }
                }

                val canvas = Canvas(layer, pdfDocument.defaultPageSize)

                if (isImageWatermark) {
                    // Handle image watermark
                    try {
                        val imageData = ImageDataFactory.create(imagePath!!)
                        val image = Image(imageData)

                        // Set image size if specified
                        if (imageWidth != null && imageHeight != null) {
                            image.setWidth(imageWidth.toFloat())
                            image.setHeight(imageHeight.toFloat())
                        } else if (imageWidth != null) {
                            image.setWidth(imageWidth.toFloat())
                            image.setAutoScaleHeight(true)
                        } else if (imageHeight != null) {
                            image.setHeight(imageHeight.toFloat())
                            image.setAutoScaleWidth(true)
                        }

                        // Center the image on the calculated position
                        val imageWidthActual = image.imageWidth
                        val imageHeightActual = image.imageHeight

                        val adjustedX = x - imageWidthActual / 2
                        val adjustedY = y - imageHeightActual / 2

                        canvas.showTextAligned(
                            image,
                            adjustedX,
                            adjustedY,
                            i,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            rotationAngle.toFloat()
                        )
                    } catch (e: Exception) {
                        Log.e("Watermark", "Error loading image watermark: $e")
                        // Fallback to text watermark if image fails
                        if (!text.isNullOrEmpty()) {
                            addTextWatermark(canvas, text!!, fontSize, watermarkColor, x, y, i, rotationAngle.toFloat())
                        }
                    }
                } else {
                    // Handle text watermark
                    if (!text.isNullOrEmpty()) {
                        addTextWatermark(canvas, text, fontSize, watermarkColor, x, y, i, rotationAngle.toFloat())
                    }
                }

                canvas.close()
                layer.restoreState()
            }
        }

        fun addTextWatermark(
            canvas: Canvas,
            text: String,
            fontSize: Double,
            watermarkColor: String,
            x: Float,
            y: Float,
            pageIndex: Int,
            rotationAngle: Float
        ) {
            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val paragraph = Paragraph(text).setFont(font).setFontSize(fontSize.toFloat())

            val color = try {
                Color.parseColor(watermarkColor)
            } catch (e: Exception) {
                Log.e("Parse", "Error parsing watermarkColor $watermarkColor. $e")
                Color.BLACK
            }

            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            // Set text color
            val deviceRgb = DeviceRgb(red, green, blue)
            paragraph.setFontColor(deviceRgb)

                        canvas.showTextAligned(
                            paragraph,
                            x,
                            y,
                            i,
                            TextAlignment.CENTER,
                            VerticalAlignment.TOP,
                            rotationAngle.toFloat()
                        )
                        } catch (e: OutOfMemoryError) {
                            Log.e("MemoryManager", "Out of memory processing page $i, skipping", e)
                            continue
                        } catch (e: Exception) {
                            Log.w("MemoryManager", "Error processing page $i: ${e.message}")
                            continue
                        }

                        canvas.close()
                        layer.restoreState()
                    }
                }

                // Force cleanup after each chunk
                memoryManager.forceGarbageCollectionIfNeeded()
            }

        watermark()

        memoryManager.logMemoryUsage("After watermarking")
        memoryManager.forceGarbageCollectionIfNeeded()

        pdfDocument.close()

        pdfReader.close()
        pdfWriter.close()

        utils.safeDeleteTempFiles(listOfTempFiles = listOf(pdfReaderFile))

        val end = System.nanoTime()
        println("Elapsed time in nanoseconds: ${end - begin}")

        resultPDFPath = pdfWriterFile.path
    }

    return resultPDFPath
}