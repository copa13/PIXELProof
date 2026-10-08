package com.pixelproof.app.report

import com.pixelproof.app.detector.DetectionResult
import com.pixelproof.app.forensics.ForensicResult
import com.pixelproof.app.metadata.MetadataResult
import com.pixelproof.app.provenance.ProvenanceResult
import com.pixelproof.app.risk.RiskAssessment
import com.pixelproof.app.explanation.ExplanationResult

object ReportBuilder {

    fun build(
        fileName: String,
        detection: DetectionResult,
        forensic: ForensicResult,
        metadata: MetadataResult,
        provenance: ProvenanceResult,
        risk: RiskAssessment,
        explanation: ExplanationResult
    ): AnalysisReport {

        val evidence = mutableListOf<ReportEvidence>()

        evidence.add(
            ReportEvidence(
                category = "AI Detection",
                finding = "Result: ${detection.label}, confidence: ${detection.confidence}%",
                severity =
                    if (detection.label.equals("AI-generated", true))
                        "High"
                    else
                        "Info"
            )
        )

        forensic.findings.forEach { finding ->
            evidence.add(
                ReportEvidence(
                    category = "Forensics",
                    finding = finding,
                    severity = "Medium"
                )
            )
        }

        metadata.findings.forEach { finding ->
            evidence.add(
                ReportEvidence(
                    category = "Metadata",
                    finding = finding,
                    severity = "Info"
                )
            )
        }

        provenance.findings.forEach { finding ->
            evidence.add(
                ReportEvidence(
                    category = "Provenance",
                    finding = finding,
                    severity =
                        if (provenance.valid)
                            "Verified"
                        else
                            "Warning"
                )
            )
        }

        return AnalysisReport(
            fileName = fileName,
            finalResult = explanation.title,
            confidence = risk.confidence,
            riskLevel = risk.riskLevel.name,
            riskScore = risk.riskScore,

            aiDetection = detection.label,
            aiConfidence = detection.confidence.toInt(),

            forensicRiskScore = forensic.riskScore,

            metadataPresent = metadata.metadataPresent,
            cameraMake = metadata.cameraMake,
            cameraModel = metadata.cameraModel,
            dateTaken = metadata.dateTaken,
            editingSoftware = metadata.software,

            credentialsStatus = provenance.status,
            credentialsValid = provenance.valid,

            explanation = explanation.detailedExplanation,
            evidence = evidence,

            sha256 = forensic.sha256
        )
    }
}
