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
import android.graphics.Matrix
import com.microblink.capture.image.ImageRotation
import kotlin.math.max

fun Bitmap.correctRotation(rotation: ImageRotation): Bitmap {
    if (rotation == ImageRotation.ROTATION_0) {
        return this
    } else {
        // matrix for transforming the image
        val matrix = Matrix()
        val rotationDegrees = when (rotation) {
            ImageRotation.ROTATION_90 -> 90f
            ImageRotation.ROTATION_180 -> 180f
            ImageRotation.ROTATION_270 -> 270f
            else -> {throw IllegalStateException("Unexpected image rotation!")}
        }
        matrix.postRotate(rotationDegrees, width / 2f, height / 2f)

        // if image is too large, scale it down so it can be displayed in image view
        val maxDimension = max(width, height)
        val maxAllowedDimension = 1920
        if (maxDimension > maxAllowedDimension) {
            val scale = maxAllowedDimension.toFloat() / maxDimension
            matrix.postScale(scale, scale)
        }
        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            false
        )
    }
}