package com.ninezero.presentation.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ninezero.presentation.R

@Composable
fun OnboardingLottieAnim(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.anim_onboarding)
    )

    val state = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = Int.MAX_VALUE,
        speed = 1.0f
    )

    LottieAnimation(
        composition = composition,
        progress = { state.progress },
        modifier = modifier.fillMaxSize()
    )
}