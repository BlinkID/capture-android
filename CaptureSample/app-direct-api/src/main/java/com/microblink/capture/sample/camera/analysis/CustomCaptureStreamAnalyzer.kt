package com.microblink.capture.sample.camera.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.microblink.capture.analysis.FrameAnalysisResult
import com.microblink.capture.directapi.AnalysisError
import com.microblink.capture.directapi.AnalyzerRunner
import com.microblink.capture.directapi.FrameAnalysisResultListener
import com.microblink.capture.image.ImageRotation

class CustomCaptureStreamAnalyzer(
    private val onAnalysisStart: (() -> Unit)? = null,
    private val onFrameAnalysisDone: (FrameAnalysisResult) -> Unit,
    private val onFrameAnalysisError: (error: AnalysisError) -> Unit,
) : ImageAnalysis.Analyzer {

    private var analysisPaused = false
    private var analysisStarted = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (!analysisStarted) {
            onAnalysisStart?.invoke()
            analysisStarted = true
        }
        if (!analysisPaused) {

            image.image?.let { mediaImage ->
                // use helper method to create Capture input image from Android media image
                val inputImage =
                    com.microblink.capture.image.InputImage.createFromAndroidMediaImage(
                        mediaImage,
                        ImageRotation.fromDegreesInt(image.imageInfo.rotationDegrees),
                        image.cropRect
                    )

                // use AnalyzerRunner to analyze image
                // we are using analyzeStreamImage method for scanning multiple images from
                // stream (it could be camera stream or video stream)
                AnalyzerRunner.analyzeStreamImage(inputImage, frameAnalysisResultListener)
                // dispose input image after usage
                inputImage.dispose()
            }
        }
        // image has to be closed
        image.close()
    }

    private val frameAnalysisResultListener = object: FrameAnalysisResultListener {
        override fun onAnalysisDone(result: FrameAnalysisResult) {
            onFrameAnalysisDone(result)
        }

        override fun onError(error: AnalysisError, exception: Exception) {
            analysisPaused = true
            onFrameAnalysisError(error)
        }

    }

    fun pauseAnalysis() {
        analysisPaused = true
    }

    fun resumeAnalysis() {
        analysisPaused = false
    }

}