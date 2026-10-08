// app/src/main/java/com/pixelproof/app/forensics/MetadataAnalyzer.kt

package com.pixelproof.app.forensics

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

object MetadataAnalyzer {

    fun analyze(
        context: Context,
        uri: Uri
    ): MetadataResult {

        context.contentResolver.openInputStream(uri).use { input ->

            if (input == null) {
                return MetadataResult(
                    cameraMake = null,
                    cameraModel = null,
                    dateTaken = null,
                    software = null,
                    gpsPresent = false,
                    metadataPresent = false,
                    findings = listOf("Image metadata could not be read.")
                )
            }

            val exif = ExifInterface(input)

            val make =
                exif.getAttribute(ExifInterface.TAG_MAKE)

            val model =
                exif.getAttribute(ExifInterface.TAG_MODEL)

            val date =
                exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

            val software =
                exif.getAttribute(ExifInterface.TAG_SOFTWARE)

            val latitude =
                exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)

            val longitude =
                exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)

            val findings = mutableListOf<String>()

            if (software != null) {
                val lower = software.lowercase()

                if (
                    "photoshop" in lower ||
                    "lightroom" in lower ||
                    "snapseed" in lower ||
                    "gimp" in lower
                ) {
                    findings.add(
                        "Editing software signature detected: $software"
                    )
                }
            }

            if (make != null || model != null) {
                findings.add("Camera/device metadata found.")
            }

            if (latitude != null && longitude != null) {
                findings.add("GPS location metadata is present.")
            }

            return MetadataResult(
                cameraMake = make,
                cameraModel = model,
                dateTaken = date,
                software = software,
                gpsPresent =
                    latitude != null && longitude != null,
                metadataPresent =
                    make != null ||
                    model != null ||
                    date != null ||
                    software != null,
                findings = findings
            )
        }
    }
}

data class MetadataResult(
    val cameraMake: String?,
    val cameraModel: String?,
    val dateTaken: String?,
    val software: String?,
    val gpsPresent: Boolean,
    val metadataPresent: Boolean,
    val findings: List<String>
)
