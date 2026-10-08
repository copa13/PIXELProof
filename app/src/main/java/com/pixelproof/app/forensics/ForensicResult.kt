// app/src/main/java/com/pixelproof/app/forensics/ForensicResult.kt

package com.pixelproof.app.forensics

data class ForensicResult(
    val riskScore: Int,
    val editingSoftware: String?,
    val cameraMake: String?,
    val cameraModel: String?,
    val dateTaken: String?,
    val gpsPresent: Boolean,
    val metadataPresent: Boolean,
    val sha256: String,
    val findings: List<String>
)
