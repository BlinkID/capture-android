/*
 * Copyright (c) 2023 Microblink Ltd. All rights reserved.
 *
 * ANY UNAUTHORIZED USE OR SALE, DUPLICATION, OR DISTRIBUTION
 * OF THIS PROGRAM OR ANY OF ITS PARTS, IN SOURCE OR BINARY FORMS,
 * WITH OR WITHOUT MODIFICATION, WITH THE PURPOSE OF ACQUIRING
 * UNLAWFUL MATERIAL OR ANY OTHER BENEFIT IS PROHIBITED!
 * THIS PROGRAM IS PROTECTED BY COPYRIGHT LAWS AND YOU MAY NOT
 * REVERSE ENGINEER, DECOMPILE, OR DISASSEMBLE IT.
 */

package com.microblink.capture.sample.common.result

import android.graphics.Bitmap
import android.util.Size
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.microblink.capture.sample.common.R

@Composable
fun ResultScreen() {
    val context = LocalContext.current
    // A surface container using the 'background' color from the theme
    Scaffold (
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .windowInsetsPadding(
                    WindowInsets.displayCutout.union(WindowInsets.systemBars)
                )
                .padding(
                    horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                    vertical = dimensionResource(R.dimen.activity_vertical_margin)
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ResultsHolder.resultData?.let { resultData ->
                TextEntry(text = context.getString(R.string.result_document_group, resultData.documentGroup))
                resultData.firstSide?.let { sideData ->
                    TextEntry(text = context.getString(R.string.result_first_side, sideData.side))
                    sideData.originalImage?.let {
                        ImageEntry(
                            image = sideData.originalImage,
                            imageName = R.string.result_first_side_original_image,
                            Size(it.width, it.height)
                        )
                    }
                    sideData.transformedImage?.let {
                        ImageEntry(
                            image = it,
                            imageName = R.string.result_first_side_trasnformed_image,
                            Size(it.width, it.height)
                        )
                    }
                }
                resultData.secondSide?.let { sideData ->
                    TextEntry(text = context.getString(R.string.result_second_side, sideData.side))
                    sideData.originalImage?.let {
                        ImageEntry(
                            image = sideData.originalImage,
                            imageName = R.string.result_second_side_original_image,
                            Size(it.width, it.height)
                        )
                    }
                    sideData.transformedImage?.let {
                        ImageEntry(
                            image = it,
                            imageName = R.string.result_second_side_trasnformed_image,
                            Size(it.width, it.height)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageEntry(image: Bitmap, @StringRes imageName: Int, resolution: Size) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = context.getString(imageName),
            textAlign = TextAlign.Left
        )
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = context.getString(imageName)
        )
    }
}

@Composable
fun TextEntry(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        textAlign = TextAlign.Left
    )
}