package com.tit.nimonsapp.ui.splash.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tit.nimonsapp.R

@Composable
fun SplashTitle(
    titleOffsetX: Float,
    titleAlpha: Float,
    underlineWidth: Float,
    modifier: Modifier = Modifier,
) {
    val primaryColor = colorResource(R.color.splash_text_primary)
    val accentColor = colorResource(R.color.splash_text_accent)
    val underlineColor = colorResource(R.color.splash_pin)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(titleOffsetX.toInt(), 0) }
                .alpha(titleAlpha),
        ) {
            Text(
                text = stringResource(R.string.splash_app_name_prefix),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = primaryColor,
                letterSpacing = (-1.5).sp,
            )
            Text(
                text = stringResource(R.string.splash_app_name_suffix),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = (-1.5).sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f * underlineWidth)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(underlineColor),
        )
    }
}
