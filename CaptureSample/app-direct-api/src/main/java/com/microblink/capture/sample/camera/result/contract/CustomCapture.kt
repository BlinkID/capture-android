package com.microblink.capture.sample.camera.result.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.microblink.capture.result.AnalyzerResult
import com.microblink.capture.sample.camera.CustomCameraActivity
import com.microblink.capture.settings.AnalyzerSettings

/**
 * Android activity result contract for launching the Custom Capture and obtaining capture
 * results.
 *
 */
class CustomCapture: ActivityResultContract<AnalyzerSettings, AnalyzerResult?>() {
    override fun createIntent(context: Context, input: AnalyzerSettings): Intent {
        return Intent(context, CustomCameraActivity::class.java).apply {
            putExtra(CustomCameraActivity.INTENT_EXTRAS_ANALYZER_SETTINGS, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AnalyzerResult? {
        return if (resultCode == Activity.RESULT_OK) {
            return analyzerResultHolder
                ?: throw java.lang.IllegalStateException("Expected AnalyzerResult does not exist!")
        } else {
            return null
        }
    }

    internal companion object {
        var analyzerResultHolder: AnalyzerResult? = null
    }
}