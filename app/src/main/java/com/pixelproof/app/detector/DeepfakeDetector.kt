// app/src/main/java/com/pixelproof/app/detector/DeepfakeDetector.kt

package com.pixelproof.app.detector

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DeepfakeDetector(
    context: Context
) {

    private val interpreter: Interpreter

    init {
        val model = loadModel(context)
        interpreter = Interpreter(model)
    }

    fun detect(bitmap: Bitmap): DetectionResult {

        val input = ImagePreprocessor.process(bitmap)

        val output = Array(1) { FloatArray(2) }

        interpreter.run(input, output)

        val fakeProbability = output[0][0]
        val realProbability = output[0][1]

        return if (fakeProbability >= realProbability) {
            DetectionResult(
                label = "AI-generated",
                confidence = fakeProbability * 100f
            )
        } else {
            DetectionResult(
                label = "Real",
                confidence = realProbability * 100f
            )
        }
    }

    fun close() {
        interpreter.close()
    }

    private fun loadModel(context: Context): MappedByteBuffer {

        val fileDescriptor =
            context.assets.openFd("deepfake_net.tflite")

        FileInputStream(fileDescriptor.fileDescriptor).use { input ->

            return input.channel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }
}
