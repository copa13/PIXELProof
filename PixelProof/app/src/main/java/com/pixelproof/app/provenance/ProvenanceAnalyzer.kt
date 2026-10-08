package com.pixelproof.app.provenance

import android.content.Context
import android.net.Uri
import org.contentauth.c2pa.C2PAError
import org.contentauth.c2pa.Reader
import org.contentauth.c2pa.FileStream
import java.io.File

object ProvenanceAnalyzer {

    fun analyze(
        context: Context,
        uri: Uri
    ): ProvenanceResult {

        val temporaryFile = File.createTempFile(
            "pixelproof_c2pa_",
            ".jpg",
            context.cacheDir
        )

        try {

            context.contentResolver
                .openInputStream(uri)
                ?.use { input ->
                    temporaryFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                ?: return ProvenanceResult(
                    credentialsFound = false,
                    valid = false,
                    manifestJson = null,
                    status = "Unable to read image",
                    findings = listOf(
                        "The selected image could not be read."
                    )
                )

            val stream = FileStream(
                temporaryFile,
                FileStream.Mode.READ
            )

            try {

                val reader = Reader.fromStream(
                    "image/jpeg",
                    stream
                )

                try {

                    val manifestJson = reader.json()

                    if (manifestJson.isBlank()) {

                        return ProvenanceResult(
                            credentialsFound = false,
                            valid = false,
                            manifestJson = null,
                            status = "No Content Credentials Found",
                            findings = listOf(
                                "No C2PA Content Credentials were found in this image."
                            )
                        )
                    }

                    return ProvenanceResult(
                        credentialsFound = true,
                        valid = true,
                        manifestJson = manifestJson,
                        status = "Content Credentials Found",
                        findings = listOf(
                            "C2PA Content Credentials were found.",
                            "The C2PA manifest was successfully read."
                        )
                    )

                } finally {
                    reader.close()
                }

            } finally {
                stream.close()
            }

        } catch (e: C2PAError) {

            return ProvenanceResult(
                credentialsFound = false,
                valid = false,
                manifestJson = null,
                status = "No Valid Content Credentials",
                findings = listOf(
                    "The image does not contain a readable valid C2PA manifest.",
                    "C2PA verification could not confirm provenance."
                )
            )

        } catch (e: Exception) {

            return ProvenanceResult(
                credentialsFound = false,
                valid = false,
                manifestJson = null,
                status = "Provenance Analysis Failed",
                findings = listOf(
                    "An error occurred while reading Content Credentials."
                )
            )

        } finally {
            temporaryFile.delete()
        }
    }
}
