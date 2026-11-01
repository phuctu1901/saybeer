package com.tem2t.saybeer.platform

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import com.tem2t.saybeer.data.BeerState
import java.io.File

object AchievementShare {
    fun create(context: Context, state: BeerState): android.net.Uri {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, 0f, 1350f, intArrayOf(Color.rgb(43,28,8), Color.rgb(18,13,8)), null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 1080f, 1350f, paint); paint.shader = null
        val amber = Color.rgb(245,166,35)
        fun text(value: String, y: Float, size: Float, color: Int = Color.WHITE) {
            paint.color = color; paint.textSize = size; paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            canvas.drawText(value, 540f, y, paint)
        }
        val mug = context.getDrawable(com.tem2t.saybeer.R.drawable.ic_mug)!!
        mug.setBounds(435, 130, 645, 340); mug.draw(canvas)
        text("SỐNG SÓT", 460f, 56f, Color.LTGRAY)
        // Cap text size to fit even for large history counts.
        text("${state.survived} trận nhậu", 590f, 108f, amber)
        text("%.0f giờ đã khóa máy".format(state.hours), 710f, 46f)
        if (state.streak >= 2) text("Chuỗi ${state.streak} phiên an toàn", 790f, 40f)
        text("Hãy say bia.", 970f, 54f)
        text("Xin đừng “say” bậy.", 1045f, 54f)
        text("saybeer.1labs.space", 1260f, 36f, amber)
        val folder = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(folder, "saybeer-achievement.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    }
    fun share(context: Context, uri: android.net.Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Thành tích SayBeer", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Khoe thành tích với hội nhậu"))
    }
}
