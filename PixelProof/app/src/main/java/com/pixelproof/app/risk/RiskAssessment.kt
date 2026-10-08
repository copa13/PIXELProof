package com.pixelproof.app.risk

data class RiskAssessment(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val confidence: Int,
    val reasons: List<String>,
    val signals: List<RiskSignal>
)
