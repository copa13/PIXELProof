// app/src/main/java/com/pixelproof/app/detector/ImagePreprocessor.kt

package com.pixelproof.app.detector

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImagePreprocessor {

    private const val SIZE = 299

    fun process(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, true)

        val buffer = ByteBuffer
            .allocateDirect(1 * SIZE * SIZE * 3 * 4)
            .order(ByteOrder.nativeOrder())

        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                val pixel = resized.getPixel(x, y)

                buffer.putFloat(Color.red(pixel) / 255f)
                buffer.putFloat(Color.green(pixel) / 255f)
                buffer.putFloat(Color.blue(pixel) / 255f)
            }
        }

        buffer.rewind()
        return buffer
    }
}
