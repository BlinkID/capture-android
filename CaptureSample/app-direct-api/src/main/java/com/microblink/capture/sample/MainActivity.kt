package com.microblink.capture.sample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.microblink.capture.analysis.FrameAnalysisResult
import com.microblink.capture.directapi.AnalysisError
import com.microblink.capture.directapi.AnalyzerRunner
import com.microblink.capture.directapi.FrameAnalysisResultListener
import com.microblink.capture.image.ImageRotation
import com.microblink.capture.image.InputImage
import com.microblink.capture.result.AnalyzerResult
import com.microblink.capture.sample.camera.result.contract.CustomCapture
import com.microblink.capture.sample.common.result.ResultScreen
import com.microblink.capture.sample.common.result.ResultsHolder
import com.microblink.capture.sample.common.result.toResultData
import com.microblink.capture.sample.ui.theme.CaptureSampleTheme
import com.microblink.capture.settings.AnalyzerSettings
import com.microblink.capture.settings.CaptureStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaptureSampleTheme {
                MainNavHost(startDestination = getString(R.string.nav_route_main))
            }
        }
    }
}

private const val TAG = "MainActivity"

@Composable
fun MainNavHost(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val ctx = LocalContext.current
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(ctx.getString(R.string.nav_route_main)) {
            MainScreen(
                onAnalyserResultAvailable = { analyserResult: AnalyzerResult ->
                    // We are using ResultHolder singleton for the simplicity of this sample.
                    // It is not the best way to pass the data to the result screen.
                    ResultsHolder.clear()
                    ResultsHolder.resultData = analyserResult.toResultData()
                    navController.navigate(ctx.getString(R.string.nav_route_results))
                }
            )
        }
        composable(ctx.getString(R.string.nav_route_results)) {
            ResultScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    onAnalyserResultAvailable: (AnalyzerResult) -> Unit
) {
    val ctx = LocalContext.current
    val customCaptureLauncher =
        rememberLauncherForActivityResult(contract = CustomCapture(), onResult = { analyzerResult ->
            ResultsHolder.clear()
            analyzerResult?.let {
                onAnalyserResultAvailable(it)
            }
        })
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        customCaptureLauncher.launch(
                            AnalyzerSettings(
                                // here you can customise analyzer settings
                                // captureSingleSide = true
                            )
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.btn_launch_camera))
                }
                val corScope = rememberCoroutineScope()
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        corScope.launch(context = Dispatchers.Default) {
                            // launch image analysis on the background thread (not on the Main thread)
                            val analyzerResult = analyzeStaticImagesFromAssets(ctx)
                            withContext(Dispatchers.Main) {
                                if (analyzerResult.completenessStatus == AnalyzerResult.CompletenessStatus.Complete) {
                                    onAnalyserResultAvailable(analyzerResult)
                                } else {
                                    Toast.makeText(
                                        ctx,
                                        "Failed to capture static images",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.btn_launch_static_images))
                }
            }
        }
    }
}

private fun analyzeStaticImagesFromAssets(ctx: Context): AnalyzerResult {
    // reset analyzer to ensure empty state
    AnalyzerRunner.reset()
    AnalyzerRunner.settings = AnalyzerSettings(
        // here we have to use single frame strategy, because we have one frame per document side
        captureStrategy = CaptureStrategy.SingleFrame
    )

    // in this sample, we don't care about results for each frame, we check the final result later
    val frameAnalysisResultListener = object : FrameAnalysisResultListener {
        override fun onAnalysisDone(result: FrameAnalysisResult) {
            // here you can check analysis result from the single frame if you need it
            Log.d(TAG, "Static frame successfully captured: ${result.frameCaptured}")
        }

        override fun onError(error: AnalysisError, exception: Exception) {
            // called when frame analysis error happens
        }

    }
    loadInputImageFromAssets(ctx, "cro_id_front.jpeg")?.let {
        AnalyzerRunner.analyzeImage(
            image = it,
            resultListener = frameAnalysisResultListener
        )
    }
    loadInputImageFromAssets(ctx, "cro_id_back.jpeg")?.let {
        AnalyzerRunner.analyzeImage(
            image = it,
            resultListener = frameAnalysisResultListener
        )
    }
    // detach result returns analyzer to initial state and prevents further result modifications
    val result =  AnalyzerRunner.detachResult()
    // terminate analyzer to free allocated resources
    AnalyzerRunner.terminate()
    return result
}

private fun loadInputImageFromAssets(ctx: Context, fileName: String): InputImage? {
    var bmpImage: Bitmap?
    try {
        ctx.assets.open(fileName).use { inputStream ->
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bmpImage = BitmapFactory.decodeStream(inputStream, null, options)
        }
    } catch (e: IOException) {
        // handle exception
        Log.e(TAG, "Failed to load image from assets!")
        Toast.makeText(ctx, "Failed to load image from assets!", Toast.LENGTH_LONG).show()
        return null
    }
    return bmpImage?.let {
        InputImage.createFromBitmap(
            input = it,
            // in this case, images from assets are not rotated
            rotation = ImageRotation.ROTATION_0,
            // we analyze entire image
            cropRect = Rect(0, 0, it.width, it.height)
        )
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen {
        // do nothing here
    }
}
