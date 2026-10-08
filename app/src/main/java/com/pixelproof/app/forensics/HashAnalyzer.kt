// app/src/main/java/com/pixelproof/app/forensics/HashAnalyzer.kt

package com.pixelproof.app.forensics

import android.net.Uri
import java.security.MessageDigest
import android.content.Context

object HashAnalyzer {

    fun sha256(context: Context, uri: Uri): String {
        val digest = MessageDigest.getInstance("SHA-256")

        context.contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(8192)

            while (true) {
                val count = input.read(buffer)
                if (count <= 0) break
                digest.update(buffer, 0, count)
            }
        }

        return digest.digest()
            .joinToString("") { "%02x".format(it) }
    }
}
