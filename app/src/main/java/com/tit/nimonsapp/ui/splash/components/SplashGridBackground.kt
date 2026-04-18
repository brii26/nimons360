package com.tit.nimonsapp.ui.splash.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.tit.nimonsapp.R

@Composable
fun splashGridBackground(
    offset: Float,
    modifier: Modifier = Modifier,
) {
    val gridColor = colorResource(R.color.splash_grid)
    val alpha = 0.08f
    val strokeWidth = 1.dp

    Canvas(modifier = modifier) {
        val spacing = 80.dp.toPx()
        val shifted = offset % spacing

        for (i in -2..(size.width / spacing).toInt() + 2) {
            val x = i * spacing + shifted
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth.toPx(),
                alpha = alpha,
            )
        }
        for (i in -2..(size.height / spacing).toInt() + 2) {
            val y = i * spacing + shifted
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth.toPx(),
                alpha = alpha,
            )
        }
    }
}
