package com.pixelproof.app.provenance

data class ProvenanceResult(
    val credentialsFound: Boolean,
    val valid: Boolean,
    val manifestJson: String?,
    val status: String,
    val findings: List<String>
)
