package com.pixelproof.app.risk

import com.pixelproof.app.detector.DetectionResult
import com.pixelproof.app.forensics.ForensicResult
import com.pixelproof.app.metadata.MetadataResult
import com.pixelproof.app.provenance.ProvenanceResult

object RiskAssessmentEngine {

    fun assess(
        detection: DetectionResult,
        forensic: ForensicResult,
        metadata: MetadataResult,
        provenance: ProvenanceResult
    ): RiskAssessment {

        val signals = mutableListOf<RiskSignal>()

        /*
         * Part #2 — AI detector
         *
         * AI-generated / Fake increases suspicion.
         * Real decreases suspicion.
         */

        val aiScore = when {
            detection.label.equals("AI-generated", true) ->
                detection.confidence.coerceIn(0f, 100f) / 100f

            detection.label.equals("Fake", true) ->
                detection.confidence.coerceIn(0f, 100f) / 100f

            detection.label.equals("Real", true) ->
                1f - detection.confidence.coerceIn(0f, 100f) / 100f

            else -> 0.5f
        }

        val aiConfidence =
            detection.confidence.coerceIn(0f, 100f) / 100f

        signals.add(
            RiskSignal(
                name = "AI Detection",
                score = aiScore,
                confidence = aiConfidence,
                reason = "AI image detection result: ${detection.label}"
            )
        )

        /*
         * Part #4 — Forensic analysis
         */

        val forensicScore =
            forensic.riskScore.coerceIn(0, 100) / 100f

        signals.add(
            RiskSignal(
                name = "Forensic Analysis",
                score = forensicScore,
                confidence = 0.85f,
                reason = "Forensic risk score: ${forensic.riskScore}%"
            )
        )

        /*
         * Part #5 — Metadata analysis
         *
         * Metadata itself is not proof of manipulation.
         * We therefore keep its influence limited.
         */

        var metadataScore = 0f

        if (metadata.software != null) {
            val software =
                metadata.software.lowercase()

            if (
                "photoshop" in software ||
                "lightroom" in software ||
                "snapseed" in software ||
                "gimp" in software
            ) {
                metadataScore = 0.65f
            }
        }

        signals.add(
            RiskSignal(
                name = "Metadata Analysis",
                score = metadataScore,
                confidence = 0.70f,
                reason = if (metadata.software != null) {
                    "Software metadata found: ${metadata.software}"
                } else {
                    "No suspicious editing software metadata detected."
                }
            )
        )

        /*
         * Part #6 — C2PA / Content Credentials
         *
         * Valid credentials are high-trust evidence.
         */

        if (provenance.credentialsFound && provenance.valid) {

            signals.add(
                RiskSignal(
                    name = "Content Credentials",
                    score = 0f,
                    confidence = 1f,
                    reason = "Valid C2PA Content Credentials found."
                )
            )

        } else {

            signals.add(
                RiskSignal(
                    name = "Content Credentials",
                    score = 0.5f,
                    confidence = 0.40f,
                    reason = provenance.status
                )
            )
        }

        /*
         * Weighted ensemble
         *
         * AI detection       = 35%
         * Forensics           = 30%
         * Metadata            = 15%
         * Provenance          = 20%
         */

        val weights = mapOf(
            "AI Detection" to 0.35f,
            "Forensic Analysis" to 0.30f,
            "Metadata Analysis" to 0.15f,
            "Content Credentials" to 0.20f
        )

        var weightedScore = 0f
        var totalWeight = 0f

        for (signal in signals) {

            val weight =
                weights[signal.name] ?: 0f

            val effectiveWeight =
                weight * signal.confidence

            weightedScore +=
                signal.score * effectiveWeight

            totalWeight += effectiveWeight
        }

        var finalScore =
            if (totalWeight > 0f) {
                weightedScore / totalWeight
            } else {
                0.5f
            }

        /*
         * High-trust C2PA override
         *
         * Valid provenance should strongly reduce risk.
         */

        if (provenance.credentialsFound &&
            provenance.valid
        ) {
            finalScore =
                minOf(finalScore, 0.15f)
        }

        /*
         * Multi-signal consensus
         *
         * Count strong suspicious signals.
         */

        val suspiciousSignals =
            signals.count {
                it.score >= 0.65f &&
                it.confidence >= 0.70f
            }

        if (suspiciousSignals >= 3) {
            finalScore =
                maxOf(finalScore, 0.60f)
        }

        /*
         * Calculate percentage.
         */

        val riskPercentage =
            (finalScore * 100f)
                .coerceIn(0f, 100f)
                .toInt()

        /*
         * Final classification.
         */

        val riskLevel = when {

            aiConfidence < 0.45f &&
            suspiciousSignals == 0 ->
                RiskLevel.UNCERTAIN

            finalScore < 0.30f ->
                RiskLevel.LOW

            finalScore < 0.60f ->
                RiskLevel.MEDIUM

            else ->
                RiskLevel.HIGH
        }

        /*
         * Overall confidence.
         */

        val averageConfidence =
            signals
                .map { it.confidence }
                .average()
                .toFloat()

        val confidence =
            (averageConfidence * 100f)
                .coerceIn(0f, 100f)
                .toInt()

        /*
         * Human-readable reasons.
         */

        val reasons =
            signals
                .filter {
                    it.score >= 0.45f
                }
                .map {
                    it.reason
                }
                .toMutableList()

        if (reasons.isEmpty()) {
            reasons.add(
                "No strong suspicious evidence was found."
            )
        }

        return RiskAssessment(
            riskScore = riskPercentage,
            riskLevel = riskLevel,
            confidence = confidence,
            reasons = reasons,
            signals = signals
        )
    }
}
