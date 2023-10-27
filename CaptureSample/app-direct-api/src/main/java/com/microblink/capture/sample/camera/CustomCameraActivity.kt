package com.microblink.capture.sample.camera

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.core.widget.TextViewCompat
import com.microblink.capture.analysis.FrameAnalysisResult
import com.microblink.capture.analysis.FrameAnalysisStatus
import com.microblink.capture.directapi.AnalyzerRunner
import com.microblink.capture.sample.R
import com.microblink.capture.sample.camera.analysis.CustomCaptureStreamAnalyzer
import com.microblink.capture.sample.camera.result.contract.CustomCapture
import com.microblink.capture.sample.camera.ux.CameraResolution
import com.microblink.capture.sample.camera.ux.UiMessage
import com.microblink.capture.settings.AnalyzerSettings
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CustomCameraActivity : AppCompatActivity() {

    private val handler: Handler = Handler(Looper.getMainLooper())

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraPreviewView: PreviewView
    private lateinit var instructionsTextSwitcher: TextSwitcher

    private var currentUiMessage = UiMessage.SENSING_FRONT_SIDE
    private var nextUiMessage = UiMessage.SENSING_FRONT_SIDE
    private var uiMessageUpdateScheduled = false
    private var lastUiMessageUpdateTimestamp = 0L


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(
                this,
                "Capture is not available without camera permission",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupViews()
        // reset analyzer state to empty
        AnalyzerRunner.reset()
        // set analyzer settings loaded from intent
        AnalyzerRunner.settings = intent.extras?.let {
            BundleCompat.getParcelable(it, INTENT_EXTRAS_ANALYZER_SETTINGS, AnalyzerSettings::class.java)
        } ?: throw java.lang.IllegalStateException("Intent does not contain expected CaptureSettings!")
        if (!isCameraPermissionGranted()) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun setupViews() {
        cameraPreviewView = findViewById(R.id.cameraPreviewView)
        instructionsTextSwitcher = findViewById<TextSwitcher>(R.id.instructionsView).apply {
            setFactory {
                TextView(this@CustomCameraActivity).apply {
                    gravity = Gravity.CENTER
                    TextViewCompat.setTextAppearance(this, R.style.custom_capture_instructions_text)
                }
            }
        }
        updateUiMessage(currentUiMessage.stringResourceId)
    }

    private fun updateUiMessage(@StringRes messageResId: Int) {
        val message = getString(messageResId)
        if (message.isBlank()) {
            instructionsTextSwitcher.visibility = View.INVISIBLE
        } else {
            instructionsTextSwitcher.visibility = View.VISIBLE
        }
        instructionsTextSwitcher.setText(message)
    }

    private fun isCameraPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener ({
            val cameraProvider = cameraProviderFuture.get()
            cameraPreviewView.post {
                // Using cameraPreviewView.post to run this code after the cameraPreview view has
                // been successfully initialized in order to get the viewPort (otherwise
                // viewPort is null) and correct camera preview dimensions.
                // One known device on which this happens is Asus Zenfone 9.

                try {
                    val useCaseGroup = UseCaseGroup.Builder().apply {
                        cameraPreviewView.viewPort?.let {
                            setViewPort(it)
                        } ?: Log.e(TAG, "ViewPort is not available!")

                        val isPreviewInPortrait =
                            cameraPreviewView.height >= cameraPreviewView.width

                        // add preview use case
                        addUseCase(
                            Preview.Builder().apply {
                                // use resolution with the same aspect ratio as for the image analysis
                                // to ensure that crop rect is appropriate
                                setTargetResolution(
                                    CameraResolution.RESOLUTION_1080p.toTargetResolution(
                                        isPreviewInPortrait
                                    )
                                )
                            }.build().apply {
                                setSurfaceProvider(cameraPreviewView.surfaceProvider)
                            }
                        )
                        // add image analysis use case
                        addUseCase(
                            ImageAnalysis.Builder().apply {
                                setTargetResolution(
                                    CameraResolution.RESOLUTION_2160p.toTargetResolution(
                                        isPreviewInPortrait
                                    )
                                )
                            }.build().apply {
                                setAnalyzer(cameraExecutor, captureStreamAnalyzer)
                            }
                        )
                    }.build()

                    // unbind all previous use cases
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        useCaseGroup
                    )
                } catch (exc: Exception) {
                    Log.e(TAG, "Camera use case binding failed", exc)
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleFrameAnalysisResult(frameAnalysisResult: FrameAnalysisResult) {
        when (frameAnalysisResult.captureState) {
            FrameAnalysisResult.CaptureState.FirstSideCaptureInProgress,
            FrameAnalysisResult.CaptureState.SecondSideCaptureInProgress -> run {
                val processingFailure = getProcessingFailureTypeFromAnalysisResult(frameAnalysisResult.frameAnalysisStatus)
                nextUiMessage = when (processingFailure) {
                    ProcessingFailureType.DOCUMENT_FRAMING_NO_DOCUMENT -> {
                        if (frameAnalysisResult.captureState == FrameAnalysisResult.CaptureState.FirstSideCaptureInProgress) {
                            UiMessage.SENSING_FRONT_SIDE
                        } else {
                            UiMessage.SENSING_BACK_SIDE
                        }
                    }
                    ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_ORIENTATION_UNSUITABLE ->
                        UiMessage.ROTATE_DOCUMENT
                    ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_TOO_FAR -> UiMessage.MOVE_CLOSER
                    ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_TOO_CLOSE -> UiMessage.MOVE_FARTHER
                    ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_ANGLE_TOO_STEEP -> UiMessage.ALIGN_DOCUMENT
                    ProcessingFailureType.DOCUMENT_TOO_CLOSE_TO_FRAME_EDGE -> UiMessage.MOVE_FARTHER
                    ProcessingFailureType.LIGHTING_TOO_DARK -> UiMessage.INCREASE_LIGHTING
                    ProcessingFailureType.LIGHTING_TOO_BRIGHT -> UiMessage.DECREASE_LIGHTING
                    ProcessingFailureType.BLUR_DETECTED -> UiMessage.ELIMINATE_BLUR
                    ProcessingFailureType.GLARE_DETECTED -> UiMessage.ELIMINATE_GLARE
                    ProcessingFailureType.OCCLUDED_BY_HAND -> UiMessage.KEEP_FULLY_VISIBLE
                    ProcessingFailureType.WRONG_SIDE -> UiMessage.WRONG_SIDE
                    null -> UiMessage.PROCESSING
                }
            }
            FrameAnalysisResult.CaptureState.SideCaptured -> {
                nextUiMessage = UiMessage.FLIP_DOCUMENT_SIDE
            }
            FrameAnalysisResult.CaptureState.DocumentCaptured -> {
                // pause scanning early here to avoid further analysis while finishing activity
                captureStreamAnalyzer.pauseAnalysis()

                // you can use final result

                // detach result to avoid further result changes and clear the state of the AnalyzerRunner
                CustomCapture.analyzerResultHolder = AnalyzerRunner.detachResult()
                // terminate analyzer to free allocated resources
                AnalyzerRunner.terminate()
                // finish activity with RESULT_OK status
                setResult(Activity.RESULT_OK)
                finish()
                return
            }
        }

        synchronized(currentUiMessage) {
            if (!uiMessageUpdateScheduled) {
                val timeFromLastUpdateMs = System.currentTimeMillis() - lastUiMessageUpdateTimestamp
                handler.postDelayed(
                    updateUiMessageRunnable,
                    (currentUiMessage.minimumDurationMs - timeFromLastUpdateMs).coerceAtLeast(0)
                )
                uiMessageUpdateScheduled = true
            }
        }
        if (nextUiMessage.pauseScanning) {
            // if next message pauses scanning, pause scanning early here to avoid further messages
            // that could replace the nextUiMessage
            captureStreamAnalyzer.pauseAnalysis()
        }
    }

    private val captureStreamAnalyzer = CustomCaptureStreamAnalyzer(
        onFrameAnalysisDone = ::handleFrameAnalysisResult,
        onFrameAnalysisError = { unrecoverableError ->
            // terminate analyzer to free allocated resources
            AnalyzerRunner.terminate()
            runOnUiThread {
                Toast.makeText(this@CustomCameraActivity, unrecoverableError.name, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    )

    /**
     * Returns failure type for the given [FrameAnalysisStatus] or null if the failure is not
     * detected.
     */
    private fun getProcessingFailureTypeFromAnalysisResult(frameAnalysisStatus: FrameAnalysisStatus): ProcessingFailureType? {
        return when (frameAnalysisStatus.sideAnalysisStatus) {
            FrameAnalysisStatus.DocumentSideAnalysisStatus.SideAlreadyCaptured ->
                ProcessingFailureType.WRONG_SIDE
            else -> null
        } ?: when (frameAnalysisStatus.framingStatus) {
            FrameAnalysisStatus.DocumentFramingStatus.NoDocument ->
                ProcessingFailureType.DOCUMENT_FRAMING_NO_DOCUMENT
            FrameAnalysisStatus.DocumentFramingStatus.CameraTooFar ->
                ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_TOO_FAR
            FrameAnalysisStatus.DocumentFramingStatus.CameraTooClose ->
                ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_TOO_CLOSE
            FrameAnalysisStatus.DocumentFramingStatus.CameraOrientationUnsuitable ->
                ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_ORIENTATION_UNSUITABLE
            FrameAnalysisStatus.DocumentFramingStatus.CameraAngleTooSteep ->
                ProcessingFailureType.DOCUMENT_FRAMING_CAMERA_ANGLE_TOO_STEEP
            FrameAnalysisStatus.DocumentFramingStatus.DocumentTooCloseToFrameEdge ->
                ProcessingFailureType.DOCUMENT_TOO_CLOSE_TO_FRAME_EDGE
            else -> null
        } ?: when (frameAnalysisStatus.lightingStatus) {
            FrameAnalysisStatus.DocumentLightingStatus.TooBright -> ProcessingFailureType.LIGHTING_TOO_BRIGHT
            FrameAnalysisStatus.DocumentLightingStatus.TooDark -> ProcessingFailureType.LIGHTING_TOO_DARK
            else -> null
        } ?: when (frameAnalysisStatus.blurStatus) {
            FrameAnalysisStatus.DocumentBlurStatus.BlurDetected -> ProcessingFailureType.BLUR_DETECTED
            else -> null
        } ?: when (frameAnalysisStatus.glareStatus) {
            FrameAnalysisStatus.DocumentGlareStatus.GlareDetected -> ProcessingFailureType.GLARE_DETECTED
            else -> null
        } ?: when (frameAnalysisStatus.occlusionStatus) {
            FrameAnalysisStatus.DocumentOcclusionStatus.Occluded -> ProcessingFailureType.OCCLUDED_BY_HAND
            else -> null
        }
    }

    private val updateUiMessageRunnable = Runnable {
        synchronized(currentUiMessage) {
            lastUiMessageUpdateTimestamp = System.currentTimeMillis()
            currentUiMessage = nextUiMessage
            uiMessageUpdateScheduled = false
        }

        // display current ui message
        Log.d(TAG, "UiMessage: ${currentUiMessage.name}")
        updateUiMessage(currentUiMessage.stringResourceId)

        if (currentUiMessage.pauseScanning) {
            // resume analysis, scanning has been paused in handleFrameAnalysisResult()
            handler.postDelayed({
                captureStreamAnalyzer.resumeAnalysis()
            }, currentUiMessage.minimumDurationMs)
        }
    }

    companion object {
        private const val TAG = "CustomCameraActivity"
        const val INTENT_EXTRAS_ANALYZER_SETTINGS = "Extra_Analyzer_Settings"
    }
}

private enum class ProcessingFailureType {
    DOCUMENT_FRAMING_NO_DOCUMENT,
    DOCUMENT_FRAMING_CAMERA_TOO_FAR,
    DOCUMENT_FRAMING_CAMERA_TOO_CLOSE,
    DOCUMENT_FRAMING_CAMERA_ANGLE_TOO_STEEP,
    DOCUMENT_FRAMING_CAMERA_ORIENTATION_UNSUITABLE,
    DOCUMENT_TOO_CLOSE_TO_FRAME_EDGE,
    LIGHTING_TOO_DARK,
    LIGHTING_TOO_BRIGHT,
    BLUR_DETECTED,
    GLARE_DETECTED,
    OCCLUDED_BY_HAND,
    WRONG_SIDE
}
