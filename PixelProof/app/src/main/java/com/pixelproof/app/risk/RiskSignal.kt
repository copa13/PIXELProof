package com.pixelproof.app.risk

data class RiskSignal(
    val name: String,
    val score: Float,
    val confidence: Float,
    val reason: String
)
