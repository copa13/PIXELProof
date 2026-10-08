package com.pixelproof.app.explanation

import com.pixelproof.app.detector.DetectionResult
import com.pixelproof.app.forensics.ForensicResult
import com.pixelproof.app.metadata.MetadataResult
import com.pixelproof.app.provenance.ProvenanceResult
import com.pixelproof.app.risk.RiskAssessment
import com.pixelproof.app.risk.RiskLevel

object EvidenceExplanationEngine {

    fun explain(
        detection: DetectionResult,
        forensic: ForensicResult,
        metadata: MetadataResult,
        provenance: ProvenanceResult,
        risk: RiskAssessment
    ): ExplanationResult {

        val evidence = mutableListOf<String>()

        val detectionLabel =
            detection.label.trim().lowercase()

        val aiConfidence =
            detection.confidence
                .coerceIn(0f, 100f)

        /*
         * AI detection evidence
         */

        when {

            detectionLabel == "ai-generated" ||
            detectionLabel == "fake" -> {

                evidence.add(
                    "The AI detector classified the image as potentially AI-generated with ${aiConfidence.toInt()}% confidence."
                )
            }

            detectionLabel == "real" -> {

                evidence.add(
                    "The AI detector classified the image as likely authentic with ${aiConfidence.toInt()}% confidence."
                )
            }

            else -> {

                evidence.add(
                    "The AI detector did not produce a sufficiently clear authenticity classification."
                )
            }
        }

        /*
         * Forensic evidence
         */

        evidence.addAll(
            forensic.findings
                .filter { it.isNotBlank() }
                .distinct()
        )

        /*
         * Metadata evidence
         */

        evidence.addAll(
            metadata.findings
                .filter { it.isNotBlank() }
                .distinct()
        )

        /*
         * Provenance evidence
         */

        evidence.addAll(
            provenance.findings
                .filter { it.isNotBlank() }
                .distinct()
        )

        /*
         * Build main explanation.
         */

        val detailedExplanation =
            buildDetailedExplanation(
                detection = detection,
                forensic = forensic,
                metadata = metadata,
                provenance = provenance,
                risk = risk
            )

        val summary =
            buildSimpleSummary(
                risk = risk,
                detection = detection
            )

        val confidenceExplanation =
            buildConfidenceExplanation(
                detection = detection,
                risk = risk
            )

        val uncertaintyNote =
            if (
                risk.riskLevel == RiskLevel.UNCERTAIN ||
                risk.confidence < 60
            ) {
                "The available evidence is not strong enough to make a reliable definitive conclusion."
            } else {
                null
            }

        val title =
            when (risk.riskLevel) {

                RiskLevel.HIGH ->
                    "Why this image was rated High Risk"

                RiskLevel.MEDIUM ->
                    "Why this image was rated Medium Risk"

                RiskLevel.LOW ->
                    "Why this image was rated Low Risk"

                RiskLevel.UNCERTAIN ->
                    "Why the result is Uncertain"
            }

        return ExplanationResult(
            title = title,
            summary = summary,
            detailedExplanation = detailedExplanation,
            evidence = evidence.distinct(),
            confidenceExplanation = confidenceExplanation,
            uncertaintyNote = uncertaintyNote
        )
    }

    private fun buildSimpleSummary(
        risk: RiskAssessment,
        detection: DetectionResult
    ): String {

        return when (risk.riskLevel) {

            RiskLevel.HIGH ->
                "Multiple signals indicate that this image may be AI-generated or manipulated."

            RiskLevel.MEDIUM ->
                "Some suspicious signals were detected, but the available evidence is not strong enough for a high-risk conclusion."

            RiskLevel.LOW ->
                "The available signals do not show strong evidence of AI generation or manipulation."

            RiskLevel.UNCERTAIN ->
                "The available signals disagree or are too weak to confidently determine whether the image is authentic."
        }
    }

    private fun buildDetailedExplanation(
        detection: DetectionResult,
        forensic: ForensicResult,
        metadata: MetadataResult,
        provenance: ProvenanceResult,
        risk: RiskAssessment
    ): String {

        val sections = mutableListOf<String>()

        /*
         * Detection explanation
         */

        when {

            detection.label.equals(
                "AI-generated",
                ignoreCase = true
            ) ||
            detection.label.equals(
                "Fake",
                ignoreCase = true
            ) -> {

                sections.add(
                    "The AI detection model found signals associated with AI-generated or manipulated imagery. Its reported confidence is ${detection.confidence.coerceIn(0f, 100f).toInt()}%."
                )
            }

            detection.label.equals(
                "Real",
                ignoreCase = true
            ) -> {

                sections.add(
                    "The AI detection model found signals more consistent with an authentic image. Its reported confidence is ${detection.confidence.coerceIn(0f, 100f).toInt()}%."
                )
            }

            else -> {

                sections.add(
                    "The AI detector did not produce a clear authenticity classification."
                )
            }
        }

        /*
         * Forensic explanation
         */

        if (forensic.findings.isNotEmpty()) {

            sections.add(
                "Forensic analysis contributed additional evidence. The forensic risk score is ${forensic.riskScore}%."
            )
        }

        /*
         * Metadata explanation
         */

        if (metadata.metadataPresent) {

            sections.add(
                "Image metadata was available and was considered as supporting evidence."
            )

        } else {

            sections.add(
                "No supported image metadata was available. Missing metadata alone does not prove that an image is fake."
            )
        }

        /*
         * Editing software
         */

        metadata.software?.let { software ->

            sections.add(
                "The metadata identifies software information associated with '$software'. This is treated as supporting evidence rather than automatic proof of manipulation."
            )
        }

        /*
         * Provenance explanation
         */

        when {

            provenance.credentialsFound &&
            provenance.valid -> {

                sections.add(
                    "Valid Content Credentials were found. This provides stronger provenance evidence and reduces the overall suspicion level."
                )
            }

            provenance.credentialsFound &&
            !provenance.valid -> {

                sections.add(
                    "Content Credentials were detected, but they could not be confirmed as valid. They therefore cannot be treated as trusted provenance."
                )
            }

            else -> {

                sections.add(
                    "No valid Content Credentials were confirmed. This means provenance could not be independently verified through C2PA."
                )
            }
        }

        /*
         * Final risk explanation
         */

        sections.add(
            "After combining the available signals, PixelProof assigned an overall ${risk.riskLevel.name.replace("_", " ")} risk level with ${risk.confidence}% confidence."
        )

        return sections.joinToString(" ")
    }

    private fun buildConfidenceExplanation(
        detection: DetectionResult,
        risk: RiskAssessment
    ): String {

        val aiConfidence =
            detection.confidence
                .coerceIn(0f, 100f)

        return when {

            risk.confidence >= 80 &&
            aiConfidence >= 80 ->
                "Confidence is high because the AI detection signal and the combined evidence are strongly aligned."

            risk.confidence >= 60 ->
                "Confidence is moderate because multiple signals were available, but some uncertainty remains."

            else ->
                "Confidence is low because the available evidence is weak, incomplete, or conflicting."
        }
    }
}
