package com.tit.nimonsapp.ui.splash.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tit.nimonsapp.R

@Composable
fun SplashMapPin(
    dropOffsetY: Dp,
    showRipple: Boolean,
    modifier: Modifier = Modifier,
) {
    val pinColor = colorResource(R.color.splash_pin)
    val pinDotColor = colorResource(R.color.splash_pin_dot)
    val transition = updateTransition(targetState = showRipple, label = "pinLandingTransition")

    val squashScaleY by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Efek mendarat
                keyframes {
                    durationMillis = 800
                    1.1f at 0 with LinearOutSlowInEasing
                    0.7f at 350 with FastOutSlowInEasing
                    1.15f at 500 with FastOutSlowInEasing
                    0.95f at 650 with FastOutSlowInEasing
                    1.0f at 800
                }
            } else {
                spring(stiffness = Spring.StiffnessLow)
            }
        },
        label = "scaleY"
    ) { state -> if (state) 1f else 1.1f }

    val squashScaleX by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                keyframes {
                    durationMillis = 800
                    0.9f at 0
                    1.3f at 200
                    0.9f at 500
                    1.05f at 650
                    1.0f at 800
                }
            } else {
                spring(stiffness = Spring.StiffnessLow)
            }
        },
        label = "scaleX"
    ) { state -> if (state) 1f else 0.9f }

    Box(
        modifier = modifier
            .size(160.dp)
            .offset(y = dropOffsetY)
            .graphicsLayer {
                scaleY = squashScaleY
                scaleX = squashScaleX
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showRipple) {
            RadarPulse()
        }

        Canvas(modifier = Modifier.size(65.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                cubicTo(0f, size.height * 0.45f, 0f, 0f, size.width / 2f, 0f)
                cubicTo(size.width, 0f, size.width, size.height * 0.45f, size.width / 2f, size.height)
                close()
            }
            drawPath(path = path, color = pinColor)
            drawCircle(
                color = pinDotColor,
                radius = size.width / 6f,
                center = Offset(size.width / 2f, size.height * 0.35f),
            )
        }
    }
}

@Composable
private fun RadarPulse() {
    val rippleColor = colorResource(R.color.splash_ripple)
    val infinite = rememberInfiniteTransition(label = "radar")

    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 4.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearOutSlowInEasing), 
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale",
    )
    val alpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearOutSlowInEasing), 
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = rippleColor,
            radius = 22.dp.toPx() * scale,
            alpha = alpha,
            style = Stroke(2.dp.toPx()),
        )
    }
}