// app/src/main/java/com/pixelproof/app/detector/DetectionResultFormatter.kt

package com.pixelproof.app.detector

data class DisplayResult(
    val result: String,
    val confidence: Int
)

object DetectionResultFormatter {

    fun format(detection: DetectionResult): DisplayResult {
        val confidence = detection.confidence
            .coerceIn(0f, 100f)
            .toInt()

        val result = when {
            confidence < 60 -> "Uncertain"
            detection.label.equals("AI-generated", ignoreCase = true) ->
                "AI-generated"
            detection.label.equals("Fake", ignoreCase = true) ->
                "AI-generated"
            detection.label.equals("Real", ignoreCase = true) ->
                "Real"
            else -> "Uncertain"
        }

        return DisplayResult(
            result = result,
            confidence = confidence
        )
    }
}
