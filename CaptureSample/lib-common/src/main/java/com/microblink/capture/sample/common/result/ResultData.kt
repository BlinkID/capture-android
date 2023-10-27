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
import com.microblink.capture.image.ImageRotation
import com.microblink.capture.result.AnalyzerResult

data class ResultData(
    val documentGroup: String?,
    val firstSide: SideData?,
    val secondSide: SideData?
)

data class SideData(
    val side: String,
    val originalImage: Bitmap?,
    val transformedImage: Bitmap?
)

object ResultsHolder {
    var resultData: ResultData? = null

    fun isEmpty(): Boolean = resultData == null

    fun clear() {
        resultData = null
    }
}

fun AnalyzerResult.toResultData(): ResultData =
    ResultData(
        documentGroup = documentGroup.name,
        firstSide = firstCapture?.let { it ->
            val originalImageResult = it.imageResult
            SideData(
                side = it.side.name,
                originalImage = originalImageResult.image.convertToBitmap()
                    ?.correctRotation(originalImageResult.imageRotation),
                transformedImage = it.transformedImageResult?.image?.convertToBitmap()
            )
        },
        secondSide = secondCapture?.let { it ->
            val originalImageResult = it.imageResult
            SideData(
                side = it.side.name,
                originalImage = originalImageResult.image.convertToBitmap()
                    ?.correctRotation(originalImageResult.imageRotation),
                transformedImage = it.transformedImageResult?.image?.convertToBitmap()
            )
        }
    )