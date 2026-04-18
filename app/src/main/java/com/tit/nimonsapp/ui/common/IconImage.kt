package com.tit.nimonsapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tit.nimonsapp.R
import kotlin.math.abs

private val iconBgColors = listOf(
    Color(0xFFFFCDD2), Color(0xFFFFF9C4), Color(0xFFC8E6C9),
    Color(0xFFBBDEFB), Color(0xFFE1BEE7), Color(0xFFFFE0B2),
    Color(0xFFB2EBF2), Color(0xFFF8BBD0),
)

fun iconBgColorFor(url: String): Color =
    iconBgColors[abs(url.hashCode()) % iconBgColors.size]

@Composable
fun iconImage(
    iconUrl: String,
    size: Dp = 56.dp,
) {
    val bgColor = remember(iconUrl) { iconBgColorFor(iconUrl) }
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier.size(size * 0.6f),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable.ic_app),
            error = painterResource(R.drawable.ic_app),
        )
    }
}
