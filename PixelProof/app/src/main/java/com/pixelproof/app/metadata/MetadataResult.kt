package com.pixelproof.app.metadata

data class MetadataResult(
    val cameraMake: String?,
    val cameraModel: String?,
    val dateTaken: String?,
    val software: String?,
    val gpsPresent: Boolean,
    val metadataPresent: Boolean,
    val exifCount: Int,
    val iptcCount: Int,
    val xmpCount: Int,
    val findings: List<String>
)
