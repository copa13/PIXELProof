package com.pixelproof.app.explanation

data class ExplanationResult(
    val title: String,
    val summary: String,
    val detailedExplanation: String,
    val evidence: List<String>,
    val confidenceExplanation: String,
    val uncertaintyNote: String?
)
