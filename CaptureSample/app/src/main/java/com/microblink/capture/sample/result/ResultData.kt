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

package com.microblink.capture.sample.result

import android.graphics.Bitmap
import com.microblink.capture.image.ImageRotation

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