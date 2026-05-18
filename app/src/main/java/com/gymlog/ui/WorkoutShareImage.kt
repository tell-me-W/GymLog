package com.gymlog.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import java.io.File

object WorkoutShareImage {
    fun createImageUri(context: Context, summary: SummaryUiState): android.net.Uri {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(18, 18, 18) }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), background)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(30, 30, 30) }
        canvas.drawRoundRect(RectF(80f, 120f, 1000f, 1230f), 36f, 36f, cardPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(156, 163, 175)
            textSize = 36f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(59, 130, 246)
            textSize = 44f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        canvas.drawText("GymLog", 140f, 230f, titlePaint)
        canvas.drawText("운동 완료", 140f, 300f, accentPaint)
        canvas.drawText("총 볼륨", 140f, 450f, labelPaint)
        canvas.drawText("${summary.totalVolumeKg.toInt()} kg", 140f, 525f, valuePaint)
        canvas.drawText("운동 시간", 140f, 665f, labelPaint)
        canvas.drawText(formatDuration(summary.durationSeconds), 140f, 740f, valuePaint)
        canvas.drawText("운동 구성", 140f, 880f, labelPaint)
        canvas.drawText("${summary.exerciseCount}종목 · ${summary.setCount}세트", 140f, 955f, valuePaint)
        canvas.drawText("로컬에서 안전하게 기록됨", 140f, 1130f, labelPaint)

        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(shareDir, "gymlog-summary.png")
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
