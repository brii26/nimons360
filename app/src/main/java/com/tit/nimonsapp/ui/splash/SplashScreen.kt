package com.tit.nimonsapp.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tit.nimonsapp.R
import com.tit.nimonsapp.ui.splash.components.SplashGridBackground
import com.tit.nimonsapp.ui.splash.components.SplashLoadingDots
import com.tit.nimonsapp.ui.splash.components.SplashMapPin
import com.tit.nimonsapp.ui.splash.components.SplashTitle
import kotlinx.coroutines.launch

@Composable
fun splashScreen(onSplashFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val mapOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "mapOffset",
    )

    var pinDropped by remember { mutableStateOf(false) }
    val pinOffsetY by animateDpAsState(
        targetValue = if (pinDropped) 0.dp else (-400).dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "pinDrop",
    )

    val titleOffsetX = remember { Animatable(-600f) }
    val titleAlpha = remember { Animatable(0f) }
    val underlineWidth = remember { Animatable(0f) }
    val dot1Alpha = remember { Animatable(0f) }
    val dot2Alpha = remember { Animatable(0f) }
    val dot3Alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Pin drop
        kotlinx.coroutines.delay(200)
        pinDropped = true

        // Title slides
        launch { titleOffsetX.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        launch { titleAlpha.animateTo(1f, tween(500)) }

        kotlinx.coroutines.delay(600)

        // Underline animatoin
        underlineWidth.animateTo(1f, tween(500, easing = FastOutSlowInEasing))

        // Loading dots
        dot1Alpha.animateTo(1f, tween(550))
        dot2Alpha.animateTo(1f, tween(550))
        dot3Alpha.animateTo(1f, tween(550))

        kotlinx.coroutines.delay(1500)
        onSplashFinished()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.splash_background)),
        contentAlignment = Alignment.Center,
    ) {
        SplashGridBackground(
            offset = mapOffset,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-30).dp),
        ) {
            SplashMapPin(
                dropOffsetY = pinOffsetY,
                showRipple = pinDropped,
            )

            Modifier.offset(y = (-20).dp)

            SplashTitle(
                titleOffsetX = titleOffsetX.value,
                titleAlpha = titleAlpha.value,
                underlineWidth = underlineWidth.value,
            )
        }

        SplashLoadingDots(
            dot1Alpha = dot1Alpha.value,
            dot2Alpha = dot2Alpha.value,
            dot3Alpha = dot3Alpha.value,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
        )
    }
}
