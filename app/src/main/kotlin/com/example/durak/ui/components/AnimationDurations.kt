package com.example.durak.ui.components

import com.example.durak.data.AnimationSpeed

object AnimationDurations {
    const val CardPlayMs = 380
    const val DefendMs = 420
    const val TakeMs = 720
    const val DiscardMs = 660
    const val AfterAiMovePauseMs = 620

    fun scale(durationMs: Int, speed: AnimationSpeed): Int =
        when (speed) {
            AnimationSpeed.OFF -> 0
            AnimationSpeed.FAST -> (durationMs * 0.62f).toInt()
            AnimationSpeed.NORMAL -> durationMs
        }
}
