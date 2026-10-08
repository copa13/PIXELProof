package com.pixelproof.app.report

data class AnalysisReport(
    val fileName: String,
    val finalResult: String,
    val confidence: Int,
    val riskLevel: String,
    val riskScore: Int,
    val aiDetection: String,
    val aiConfidence: Int,
    val forensicRiskScore: Int,
    val metadataPresent: Boolean,
    val cameraMake: String?,
    val cameraModel: String?,
    val dateTaken: String?,
    val editingSoftware: String?,
    val credentialsStatus: String,
    val credentialsValid: Boolean,
    val explanation: String,
    val evidence: List<ReportEvidence>,
    val sha256: String
)
