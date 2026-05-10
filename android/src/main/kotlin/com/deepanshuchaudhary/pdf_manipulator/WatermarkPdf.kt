package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Color
import android.util.Log
import androidx.core.net.toUri
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfResources
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class WatermarkLayer {
    UnderContent, OverContent
}

enum class PositionType {
    TopLeft, TopCenter, TopRight, CenterLeft, Center, CenterRight, BottomLeft, BottomCenter, BottomRight, Custom
}

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
    return withContext(Dispatchers.IO) {
        val utils = Utils()
        val contentResolver: ContentResolver = context.contentResolver
        val sourceUri = utils.getURI(sourceFilePath)

        val pdfReaderFile = File.createTempFile("readerTempFile", ".pdf")
        utils.copyDataFromSourceToDestDocument(
            sourceFileUri = sourceUri,
            destinationFileUri = pdfReaderFile.toUri(),
            contentResolver = contentResolver
        )

        val pdfWriterFile = File.createTempFile("writerTempFile", ".pdf")

        val pdfReader = PdfReader(pdfReaderFile).setUnethicalReading(true)
        pdfReader.setMemorySavingMode(true)
        val pdfWriter = PdfWriter(pdfWriterFile).apply {
            setSmartMode(true)
            compressionLevel = 9
        }

        PdfDocument(pdfReader, pdfWriter).use { pdfDocument ->
            val resolvedPosition = resolvePosition(
                positionType,
                customPositionXCoordinatesList,
                customPositionYCoordinatesList,
                pdfDocument.numberOfPages
            )

            for (pageIndex in 1..pdfDocument.numberOfPages) {
                addWatermarkToPage(
                    pdfDocument = pdfDocument,
                    pageIndex = pageIndex,
                    text = text,
                    imagePath = imagePath,
                    fontSize = fontSize,
                    watermarkLayer = watermarkLayer,
                    opacity = opacity,
                    rotationAngle = rotationAngle.toFloat(),
                    watermarkColor = watermarkColor,
                    positionType = resolvedPosition,
                    customPositionXCoordinatesList = customPositionXCoordinatesList,
                    customPositionYCoordinatesList = customPositionYCoordinatesList,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            }
        }

        pdfReader.close()
        pdfWriter.close()
        utils.safeDeleteTempFiles(listOf(pdfReaderFile))

        pdfWriterFile.path
    }
}

private fun resolvePosition(
    positionType: PositionType,
    customPositionXCoordinatesList: List<Double>,
    customPositionYCoordinatesList: List<Double>,
    numberOfPages: Int
): PositionType {
    if (positionType != PositionType.Custom) return positionType

    return if (
        customPositionXCoordinatesList.size == numberOfPages &&
        customPositionYCoordinatesList.size == numberOfPages
    ) {
        PositionType.Custom
    } else {
        Log.w("Watermark", "Custom coordinates count does not match page count; using center")
        PositionType.Center
    }
}

private fun addWatermarkToPage(
    pdfDocument: PdfDocument,
    pageIndex: Int,
    text: String?,
    imagePath: String?,
    fontSize: Double,
    watermarkLayer: WatermarkLayer,
    opacity: Double,
    rotationAngle: Float,
    watermarkColor: String,
    positionType: PositionType,
    customPositionXCoordinatesList: List<Double>,
    customPositionYCoordinatesList: List<Double>,
    imageWidth: Double?,
    imageHeight: Double?
) {
    val pdfPage: PdfPage = pdfDocument.getPage(pageIndex)
    val pageSize: Rectangle = pdfPage.pageSizeWithRotation
    pdfPage.isIgnorePageRotationForContent = true

    val layer = if (watermarkLayer == WatermarkLayer.UnderContent) {
        PdfCanvas(pdfPage.newContentStreamBefore(), PdfResources(), pdfDocument)
    } else {
        PdfCanvas(pdfPage)
    }

    layer.saveState()
    try {
        layer.setExtGState(PdfExtGState().apply { fillOpacity = opacity.toFloat() })
        val (x, y) = calculatePosition(
            positionType,
            pageSize,
            pageIndex,
            customPositionXCoordinatesList,
            customPositionYCoordinatesList
        )

        Canvas(layer, pageSize).use { canvas ->
            if (!imagePath.isNullOrEmpty()) {
                addImageWatermark(canvas, imagePath, imageWidth, imageHeight, x, y, pageIndex, rotationAngle)
            } else if (!text.isNullOrEmpty()) {
                addTextWatermark(canvas, text, fontSize, watermarkColor, x, y, pageIndex, rotationAngle)
            }
        }
    } finally {
        layer.restoreState()
    }
}

private fun calculatePosition(
    positionType: PositionType,
    pageSize: Rectangle,
    pageIndex: Int,
    customPositionXCoordinatesList: List<Double>,
    customPositionYCoordinatesList: List<Double>
): Pair<Float, Float> {
    return when (positionType) {
        PositionType.TopLeft -> 0f to pageSize.height
        PositionType.TopCenter -> pageSize.width / 2 to pageSize.height
        PositionType.TopRight -> pageSize.width to pageSize.height
        PositionType.CenterLeft -> 0f to pageSize.height / 2
        PositionType.Center -> pageSize.width / 2 to pageSize.height / 2
        PositionType.CenterRight -> pageSize.width to pageSize.height / 2
        PositionType.BottomLeft -> 0f to 0f
        PositionType.BottomCenter -> pageSize.width / 2 to 0f
        PositionType.BottomRight -> pageSize.width to 0f
        PositionType.Custom -> customPositionXCoordinatesList[pageIndex - 1].toFloat() to
            customPositionYCoordinatesList[pageIndex - 1].toFloat()
    }
}

private fun addImageWatermark(
    canvas: Canvas,
    imagePath: String,
    imageWidth: Double?,
    imageHeight: Double?,
    x: Float,
    y: Float,
    pageIndex: Int,
    rotationAngle: Float
) {
    val image = Image(ImageDataFactory.create(imagePath))

    when {
        imageWidth != null && imageHeight != null -> {
            image.setWidth(imageWidth.toFloat())
            image.setHeight(imageHeight.toFloat())
        }
        imageWidth != null -> {
            image.setWidth(imageWidth.toFloat())
            image.setAutoScaleHeight(true)
        }
        imageHeight != null -> {
            image.setHeight(imageHeight.toFloat())
            image.setAutoScaleWidth(true)
        }
    }

    image.setFixedPosition(pageIndex, x - image.imageWidth / 2, y - image.imageHeight / 2)
    image.setRotationAngle(rotationAngle.toDouble())
    canvas.add(image)
}

private fun addTextWatermark(
    canvas: Canvas,
    text: String,
    fontSize: Double,
    watermarkColor: String,
    x: Float,
    y: Float,
    pageIndex: Int,
    rotationAngle: Float
) {
    val color = try {
        Color.parseColor(watermarkColor)
    } catch (e: Exception) {
        Log.e("Watermark", "Error parsing watermark color $watermarkColor", e)
        Color.BLACK
    }

    val paragraph = Paragraph(text)
        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
        .setFontSize(fontSize.toFloat())
        .setFontColor(DeviceRgb(Color.red(color), Color.green(color), Color.blue(color)))

    canvas.showTextAligned(
        paragraph,
        x,
        y,
        pageIndex,
        TextAlignment.CENTER,
        VerticalAlignment.TOP,
        rotationAngle
    )
}
