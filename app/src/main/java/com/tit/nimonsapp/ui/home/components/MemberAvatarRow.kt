package com.tit.nimonsapp.ui.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tit.nimonsapp.ui.common.smallAvatar

@Composable
fun memberAvatarRow(
    labels: List<String>,
    maxVisible: Int = 4,
) {
    val overflowBg = MaterialTheme.colorScheme.surfaceVariant
    val overflowText = MaterialTheme.colorScheme.onSurfaceVariant

    Row(verticalAlignment = Alignment.CenterVertically) {
        val visible = labels.take(maxVisible)
        visible.forEachIndexed { index, label ->
            smallAvatar(
                label = label,
                color = avatarColors[index % avatarColors.size],
                overlapIndex = index,
            )
        }

        val remaining = labels.size - visible.size
        if (remaining > 0) {
            smallAvatar(
                label = "+$remaining",
                color = overflowBg,
                overlapIndex = visible.size,
                textColor = overflowText,
            )
        }

        Spacer(modifier = Modifier.width((visible.size * 16).dp))
    }
}

internal val avatarColors = listOf(
    Color(0xFF0097A7), // avatar_1
    Color(0xFFD83A34),
    Color(0xFF43A047),
    Color(0xFFF57C00),
    Color(0xFFE65100),
    Color(0xFFC2185B),
)

internal fun initialsFromName(name: String): String =
    name.trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .take(1)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
