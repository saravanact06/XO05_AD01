package com.thambithappu

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.thambithappu.detection.FaceData

/**
 * Crops the peeker's face out of the frame and stamps "THAMBI THAPPU" across the bottom
 * of it - no template, no frame, just the photo itself. Nothing is ever written to disk.
 */
object MemeGenerator {

    fun generate(frameBitmap: Bitmap, peekerFace: FaceData): Bitmap {
        val pad = (peekerFace.boundingBoxWidth * 0.25f).toInt()
        val left = (peekerFace.boundingBoxLeft - pad).coerceIn(0, frameBitmap.width - 1)
        val top = (peekerFace.boundingBoxTop - pad).coerceIn(0, frameBitmap.height - 1)
        val right = (peekerFace.boundingBoxLeft + peekerFace.boundingBoxWidth + pad)
            .coerceIn(left + 1, frameBitmap.width)
        val bottom = (peekerFace.boundingBoxTop + peekerFace.boundingBoxHeight + pad)
            .coerceIn(top + 1, frameBitmap.height)

        // .copy(...) guarantees a mutable bitmap to draw on, regardless of whether the
        // cropped sub-bitmap Android handed back happened to be mutable already.
        val crop = Bitmap.createBitmap(frameBitmap, left, top, right - left, bottom - top)
            .copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(crop)
        val bandHeight = crop.height * 0.18f

        val bandPaint = Paint().apply { color = Color.parseColor("#CC292522") }
        canvas.drawRect(0f, crop.height - bandHeight, crop.width.toFloat(), crop.height.toFloat(), bandPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            textSize = bandHeight * 0.5f
        }
        canvas.drawText(
            "THAMBI THAPPU",
            crop.width / 2f,
            crop.height - bandHeight / 2f + textPaint.textSize * 0.35f,
            textPaint
        )

        return crop
    }
}
