// app/src/main/java/com/pixelproof/app/forensics/ForensicAnalyzer.kt

package com.pixelproof.app.forensics

import android.content.Context
import android.net.Uri

class ForensicAnalyzer(
    private val context: Context
) {

    fun analyze(uri: Uri): ForensicResult {

        val metadata =
            MetadataAnalyzer.analyze(context, uri)

        val hash =
            HashAnalyzer.sha256(context, uri)

        val findings =
            metadata.findings.toMutableList()

        var riskScore = 0

        if (metadata.metadataPresent) {
            riskScore += 10
        }

        if (metadata.gpsPresent) {
            findings.add(
                "Image contains embedded location information."
            )
            riskScore += 5
        }

        if (metadata.software != null) {
            val software =
                metadata.software.lowercase()

            if (
                "photoshop" in software ||
                "lightroom" in software ||
                "snapseed" in software ||
                "gimp" in software
            ) {
                riskScore += 30
            }
        }

        if (!metadata.metadataPresent) {
            findings.add(
                "No useful EXIF metadata was found."
            )
            riskScore += 5
        }

        riskScore =
            riskScore.coerceIn(0, 100)

        if (findings.isEmpty()) {
            findings.add(
                "No significant metadata-based forensic signal found."
            )
        }

        return ForensicResult(
            riskScore = riskScore,
            editingSoftware = metadata.software,
            cameraMake = metadata.cameraMake,
            cameraModel = metadata.cameraModel,
            dateTaken = metadata.dateTaken,
            gpsPresent = metadata.gpsPresent,
            metadataPresent = metadata.metadataPresent,
            sha256 = hash,
            findings = findings
        )
    }
}
