package com.pixelproof.app.metadata

import android.content.Context
import android.net.Uri

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.iptc.IptcDirectory
import com.drew.metadata.xmp.XmpDirectory

object MetadataAnalyzer {

    fun analyze(
        context: Context,
        uri: Uri
    ): MetadataResult {

        val findings = mutableListOf<String>()

        val metadata = context.contentResolver
            .openInputStream(uri)
            ?.use { input ->
                ImageMetadataReader.readMetadata(input)
            }
            ?: return MetadataResult(
                cameraMake = null,
                cameraModel = null,
                dateTaken = null,
                software = null,
                gpsPresent = false,
                metadataPresent = false,
                exifCount = 0,
                iptcCount = 0,
                xmpCount = 0,
                findings = listOf("Image metadata could not be read.")
            )

        var cameraMake: String? = null
        var cameraModel: String? = null
        var dateTaken: String? = null
        var software: String? = null

        metadata.getDirectoriesOfType(
            ExifIFD0Directory::class.java
        ).forEach { directory ->

            cameraMake =
                directory.getString(ExifIFD0Directory.TAG_MAKE)

            cameraModel =
                directory.getString(ExifIFD0Directory.TAG_MODEL)

            software =
                directory.getString(ExifIFD0Directory.TAG_SOFTWARE)
        }

        metadata.getDirectoriesOfType(
            ExifSubIFDDirectory::class.java
        ).forEach { directory ->

            dateTaken =
                directory.getString(
                    ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL
                )
        }

        val gpsPresent =
            metadata
                .getDirectoriesOfType(GpsDirectory::class.java)
                .isNotEmpty()

        val exifCount =
            metadata
                .getDirectoriesOfType(ExifIFD0Directory::class.java)
                .size +
            metadata
                .getDirectoriesOfType(ExifSubIFDDirectory::class.java)
                .size

        val iptcCount =
            metadata
                .getDirectoriesOfType(IptcDirectory::class.java)
                .size

        val xmpCount =
            metadata
                .getDirectoriesOfType(XmpDirectory::class.java)
                .size

        if (cameraMake != null || cameraModel != null) {
            findings.add("Camera/device metadata found.")
        }

        if (dateTaken != null) {
            findings.add("Original capture date metadata found.")
        }

        if (software != null) {

            val lower = software.lowercase()

            if (
                "photoshop" in lower ||
                "lightroom" in lower ||
                "snapseed" in lower ||
                "gimp" in lower
            ) {
                findings.add(
                    "Image-editing software signature detected: $software"
                )
            } else {
                findings.add(
                    "Software metadata found: $software"
                )
            }
        }

        if (gpsPresent) {
            findings.add(
                "GPS/location metadata is present."
            )
        }

        if (iptcCount > 0) {
            findings.add(
                "IPTC metadata is present."
            )
        }

        if (xmpCount > 0) {
            findings.add(
                "XMP metadata is present."
            )
        }

        val metadataPresent =
            exifCount > 0 ||
            iptcCount > 0 ||
            xmpCount > 0

        if (!metadataPresent) {
            findings.add(
                "No supported metadata was found."
            )
        }

        return MetadataResult(
            cameraMake = cameraMake,
            cameraModel = cameraModel,
            dateTaken = dateTaken,
            software = software,
            gpsPresent = gpsPresent,
            metadataPresent = metadataPresent,
            exifCount = exifCount,
            iptcCount = iptcCount,
            xmpCount = xmpCount,
            findings = findings
        )
    }
}
