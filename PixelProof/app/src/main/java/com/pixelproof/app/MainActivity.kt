package com.pixelproof.app

import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity

import io.github.inomnom.filepicker.api.FilePicker
import io.github.inomnom.filepicker.api.PickedFile
import io.github.inomnom.filepicker.api.PickerConfig
import io.github.inomnom.filepicker.api.PickerResult

class MainActivity : AppCompatActivity() {

    private val tag = "PixelProofPart1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * Part #1:
         *
         * Existing GitHub FilePicker
         *
         * Camera
         * Gallery / Photo Picker
         *
         * Single image only.
         */

        openImagePicker()
    }

    private fun openImagePicker() {

        FilePicker.showSheet(
            context = this,

            config = PickerConfig.singleImage(
                compress = true
            ),

            onResult = this::handlePickerResult
        )
    }

    private fun handlePickerResult(
        result: PickerResult
    ) {

        when (result) {

            is PickerResult.Success -> {

                if (result.files.isEmpty()) {
                    Log.w(
                        tag,
                        "No image selected"
                    )
                    return
                }

                val image: PickedFile =
                    result.files.first()

                Log.d(
                    tag,
                    "Image URI: ${image.uri}"
                )

                Log.d(
                    tag,
                    "Image name: ${image.name}"
                )

                Log.d(
                    tag,
                    "Image size: ${image.size}"
                )

                Log.d(
                    tag,
                    "Image MIME: ${image.mimeType}"
                )

                /*
                 * IMPORTANT:
                 *
                 * This is the hand-off point
                 * for Part #2 AI Detection.
                 *
                 * Part #2 will receive this PickedFile.
                 */
            }

            is PickerResult.Error -> {

                Log.e(
                    tag,
                    "Image picker error",
                    result.error
                )
            }

            is PickerResult.Cancelled -> {

                Log.d(
                    tag,
                    "Image picker cancelled"
                )
            }
        }
    }
}
