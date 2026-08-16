package com.timebasedfitness.app.ui.theme

import androidx.compose.animation.core.tween

object Motion {
    const val CardChangeDuration = 500
    const val FadeDuration = 300
    const val StepCheckDuration = 200
    val CardChange = tween<Float>(CardChangeDuration)
    val Fade = tween<Float>(FadeDuration)
}
