package com.royalenfield.ff.dashprototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Loads the Flying Flea N photo and removes its background, following the design's prepBike():
// flood fill from the edges makes light pixels transparent, then soft edges are blended and the
// result is cropped to the bike.
fun loadBikeCutout(context: Context): ImageBitmap {
    val source = BitmapFactory.decodeResource(context.resources, R.drawable.flea_n)
    return cutoutBike(source).asImageBitmap()
}

private fun cutoutBike(source: Bitmap): Bitmap {
    val w = 900
    val h = (w * source.height.toFloat() / source.width).roundToInt()
    val k = w.toFloat() / source.width

    val scaled = Bitmap.createScaledBitmap(source, w, h, true).copy(Bitmap.Config.ARGB_8888, true)
    // Hides the sketch signature between the wheels.
    android.graphics.Canvas(scaled).drawRect(
        612f * k,
        1012f * k,
        1285f * k,
        h.toFloat(),
        android.graphics.Paint().apply { color = android.graphics.Color.WHITE },
    )

    val px = IntArray(w * h)
    scaled.getPixels(px, 0, w, 0, 0, w, h)
    // Near the ground a lower threshold also removes the soft shadow.
    val bandStart = (1040 * k).roundToInt() * w

    fun isLight(i: Int): Boolean {
        val c = px[i]
        val t = if (i >= bandStart) 120 else 222
        return ((c shr 16) and 0xFF) > t && ((c shr 8) and 0xFF) > t && (c and 0xFF) > t
    }

    val seen = BooleanArray(w * h)
    val stack = ArrayDeque<Int>()
    for (i in 0 until w) {
        stack.addLast(i)
        stack.addLast((h - 1) * w + i)
    }
    for (j in 0 until h) {
        stack.addLast(j * w)
        stack.addLast(j * w + w - 1)
    }
    while (stack.isNotEmpty()) {
        val i = stack.removeLast()
        if (seen[i]) continue
        seen[i] = true
        if (!isLight(i)) continue
        px[i] = px[i] and 0x00FFFFFF
        val x = i % w
        if (x > 0) stack.addLast(i - 1)
        if (x < w - 1) stack.addLast(i + 1)
        if (i >= w) stack.addLast(i - w)
        if (i < w * (h - 1)) stack.addLast(i + w)
    }

    // Soften the edge of the bike where it meets the removed background.
    for (i in w until w * (h - 1)) {
        val c = px[i]
        if (c ushr 24 == 0) continue
        val l = (((c shr 16) and 0xFF) + ((c shr 8) and 0xFF) + (c and 0xFF)) / 3f
        val nearClear = px[i - 1] ushr 24 == 0 || px[i + 1] ushr 24 == 0 ||
            px[i - w] ushr 24 == 0 || px[i + w] ushr 24 == 0
        if (l > 190 && nearClear) {
            val alpha = (255 * max(0f, (235 - l) / 45f)).roundToInt()
            px[i] = (alpha shl 24) or (c and 0x00FFFFFF)
        }
    }

    var x0 = w
    var y0 = h
    var x1 = 0
    var y1 = 0
    for (y in 0 until h) {
        for (x in 0 until w) {
            if ((px[y * w + x] ushr 24) > 20) {
                x0 = min(x0, x)
                x1 = max(x1, x)
                y0 = min(y0, y)
                y1 = max(y1, y)
            }
        }
    }
    scaled.setPixels(px, 0, w, 0, 0, w, h)
    if (x1 < x0 || y1 < y0) return scaled
    return Bitmap.createBitmap(scaled, x0, y0, x1 - x0 + 1, y1 - y0 + 1)
}
