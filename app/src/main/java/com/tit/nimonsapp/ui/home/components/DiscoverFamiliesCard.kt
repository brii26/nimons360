package com.tit.nimonsapp.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.tit.nimonsapp.data.network.GetDiscoverFamiliesResponseDto
import com.tit.nimonsapp.ui.common.iconImage

@Composable
fun discoverFamiliesCard(
    families: List<GetDiscoverFamiliesResponseDto>,
    onFamilyClick: (Int) -> Unit,
    onJoinClick: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    Card(
        modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            families.forEachIndexed { index, family ->
                discoverFamilyRow(family = family, onFamilyClick = onFamilyClick, onJoinClick = onJoinClick)
                if (index != families.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun discoverFamilyRow(
    family: GetDiscoverFamiliesResponseDto,
    onFamilyClick: (Int) -> Unit,
    onJoinClick: (Int) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onFamilyClick(family.id) }
                .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconImage(iconUrl = family.iconUrl, size = 52.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (family.name.length > 30) family.name.take(30) + "…" else family.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        memberAvatarRow(
            labels = family.members.map { initialsFromName(it.fullName) },
            maxVisible = 3,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            onClick = { onJoinClick(family.id) },
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = "Join",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}
