package com.tit.nimonsapp.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.ui.common.iconImage

@Composable
fun myFamilyCard(
    family: GetMyFamiliesResponseDto,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Card(
        onClick = onClick,
        modifier = Modifier.width(176.dp).aspectRatio(0.82f).shadow(elevation = 3.dp, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            iconImage(iconUrl = family.iconUrl)

            Text(
                text = if (family.name.length > 30) family.name.take(30) + "…" else family.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "${family.members.size} members",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            memberAvatarRow(labels = family.members.map { initialsFromName(it.fullName) })
        }
    }
}
