package com.microblink.capture.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.microblink.capture.overlay.resources.CaptureOverlayStrings
import com.microblink.capture.result.AnalyzerResult
import com.microblink.capture.result.CaptureResult
import com.microblink.capture.result.contract.MbCapture
import com.microblink.capture.sample.common.result.ResultData
import com.microblink.capture.sample.common.result.ResultScreen
import com.microblink.capture.sample.common.result.ResultsHolder
import com.microblink.capture.sample.common.result.SideData
import com.microblink.capture.sample.common.result.correctRotation
import com.microblink.capture.sample.common.result.toResultData
import com.microblink.capture.sample.ui.theme.CaptureSampleTheme
import com.microblink.capture.settings.*

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
    val captureLauncher = rememberLauncherForActivityResult(contract = MbCapture(), onResult = { captureResult ->
        if (captureResult.status == CaptureResult.Status.DOCUMENT_CAPTURED) {
            // do something with the result if document has been successfully captured
            captureResult.analyzerResult?.let(onAnalyserResultAvailable)
        }
    })
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =  MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(stringResource(R.string.app_name))
                }
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Button(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = {
                            // instantiate capture settings and customize them to your needs
                            val captureSettings = CaptureSettings(
                                analyzerSettings = AnalyzerSettings(
                                    // there are other options available
                                    captureStrategy = CaptureStrategy.Default // this is default
                                ),
                                uxSettings = UxSettings(
                                    // there are other ux options available
                                    keepScreenOn = true // this is default
                                ),
                                strings = CaptureOverlayStrings(
                                    // here you can define your string resources or use default ones
                                ),
                                // define your style to customise the look or use default style
                                style = null
                            )
                            captureLauncher.launch(captureSettings)
                        }) {
                        Text(text = stringResource(R.string.btn_launch_capture))
                    }
                }
            }
        }
    )
}