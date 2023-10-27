package com.microblink.capture.sample.camera.ux

import android.util.Size

enum class CameraResolution(val width: Int, val height: Int) {
    RESOLUTION_1080p(1920, 1080),
    RESOLUTION_2160p(3840, 2160);

    fun toTargetResolution(isPortrait: Boolean): Size =
        if (isPortrait) {
            Size(height, width)
        } else {
            Size(width, height)
        }
}