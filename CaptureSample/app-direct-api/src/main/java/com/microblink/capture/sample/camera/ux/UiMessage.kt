package com.microblink.capture.sample.camera.ux

import androidx.annotation.StringRes
import com.microblink.capture.sample.R

enum class UiMessage(
    @StringRes val stringResourceId: Int,
    val minimumDurationMs: Long = 1000L,
    val pauseScanning: Boolean = false
) {
    SENSING_FRONT_SIDE(R.string.capture_status_sensing_front),
    SENSING_BACK_SIDE(R.string.capture_status_sensing_back),
    PROCESSING(R.string.capture_status_processing),
    FLIP_DOCUMENT_SIDE(
        stringResourceId = R.string.capture_status_flip_document,
        pauseScanning = true,
        minimumDurationMs = 2500
    ),
    ROTATE_DOCUMENT(
        stringResourceId = R.string.capture_status_rotate_document,
        minimumDurationMs = 2000
    ),
    MOVE_FARTHER(R.string.capture_status_move_farther),
    MOVE_CLOSER(R.string.capture_status_move_closer),
    KEEP_FULLY_VISIBLE(R.string.capture_status_keep_fully_visible),
    ALIGN_DOCUMENT(R.string.capture_status_align_document),
    INCREASE_LIGHTING(R.string.capture_status_increase_lighting_intensity),
    DECREASE_LIGHTING(R.string.capture_status_decrease_lighting_intensity),
    ELIMINATE_GLARE(R.string.capture_status_eliminate_glare),
    ELIMINATE_BLUR(R.string.capture_status_eliminate_blur),
    WRONG_SIDE(R.string.capture_status_flip_document);
}