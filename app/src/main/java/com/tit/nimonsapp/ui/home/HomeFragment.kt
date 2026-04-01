package com.tit.nimonsapp.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.network.GetDiscoverFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): android.view.View =
        ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.loadHome()
                }

                homeScreen(
                    state = uiState,
                    onRefresh = viewModel::refresh,
                    onGoToProfile = {
                        findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    },
                    onGoToCreateFamily = {
                        findNavController().navigate(R.id.action_homeFragment_to_createFamilyFragment)
                    },
                )
            }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun homeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToCreateFamily: () -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(state.meta.errorMessage) {
        state.meta.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = state.isRefreshing,
            onRefresh = onRefresh,
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7FA))
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                homeHeader(
                    onGoToProfile = onGoToProfile,
                    profileInitial = state.me?.fullName?.firstProfileInitial() ?: "?",
                )
            }

            item {
                sectionTitle("MY FAMILIES")
            }

            item {
                if (state.myFamilies.isEmpty() && state.meta.isLoading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.myFamilies.isEmpty()) {
                    emptyCard("You are not in any family yet.")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(state.myFamilies) { family ->
                            myFamilyCard(family = family)
                        }
                    }
                }
            }

            item {
                sectionTitle("DISCOVER FAMILIES")
            }

            item {
                if (state.discoverFamilies.isEmpty() && state.meta.isLoading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.discoverFamilies.isEmpty()) {
                    emptyCard("No families to discover right now.")
                } else {
                    discoverFamiliesCard(
                        families = state.discoverFamilies,
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        PullRefreshIndicator(
            refreshing = state.isRefreshing,
            state = pullRefreshState,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
        )

        FloatingActionButton(
            onClick = onGoToCreateFamily,
            containerColor = Color(0xFFDCEBFF),
            contentColor = Color(0xFF2F80ED),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create family",
            )
        }
    }
}

@Composable
private fun homeHeader(
    onGoToProfile: () -> Unit,
    profileInitial: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Nimons360",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F1F24),
        )

        Surface(
            onClick = onGoToProfile,
            shape = CircleShape,
            color = Color(0xFF4A90E2),
            modifier = Modifier.size(40.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = profileInitial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // TextButton(
        //     onClick = onGoToProfile,
        //     modifier =
        //         Modifier
        //             .clip(CircleShape)
        //             .background(Color(0xFF4A90E2))
        //             .padding(horizontal = 6.dp),
        // ) {
        //     Text(
        //         text = profileInitial,
        //         color = Color.White,
        //         fontWeight = FontWeight.Bold,
        //     )
        // }
    }
}

@Composable
private fun sectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF595965),
    )
}

@Composable
private fun myFamilyCard(family: GetMyFamiliesResponseDto) {
    Card(
        modifier =
            Modifier
                .width(176.dp)
                .aspectRatio(0.82f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            familyIconImage(
                iconUrl = family.iconUrl,
            )

            Text(
                text = family.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF2A2A33),
            )

            Text(
                text = "${family.members.size} members",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6F6F7A),
            )

            memberAvatarRow(
                labels = family.members.map { initialsFromName(it.fullName) },
            )
        }
    }
}

@Composable
private fun discoverFamiliesCard(families: List<GetDiscoverFamiliesResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            families.forEachIndexed { index, family ->
                discoverFamilyRow(family = family)

                if (index != families.lastIndex) {
                    Divider(
                        color = Color(0xFFE9E9EF),
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun discoverFamilyRow(family: GetDiscoverFamiliesResponseDto) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        familyIconImage(
            iconUrl = family.iconUrl,
            size = 52.dp,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = family.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A2A33),
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
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFE4F0FF),
        ) {
            Text(
                text = "Join",
                color = Color(0xFF2F80ED),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun familyIconImage(
    iconUrl: String,
    size: Dp = 56.dp,
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = iconUrl) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    URL(iconUrl).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }.getOrNull()
            }
    }

    Box(
        modifier =
            Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF2F2F7)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Family icon",
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun memberAvatarRow(
    labels: List<String>,
    maxVisible: Int = 4,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                color = Color(0xFFE0E0E0),
                overlapIndex = visible.size,
                textColor = Color(0xFF666666),
            )
        }

        Spacer(modifier = Modifier.width((visible.size * 16).dp))
    }
}

@Composable
private fun smallAvatar(
    label: String,
    color: Color,
    overlapIndex: Int,
    textColor: Color = Color.White,
) {
    Box(
        modifier =
            Modifier
                .offset { IntOffset(x = -overlapIndex * 12, y = 0) }
                .size(28.dp)
                .clip(CircleShape)
                .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun emptyCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = Color(0xFF6F6F7A),
            )
        }
    }
}

private val avatarColors =
    listOf(
        Color(0xFF2F80ED),
        Color(0xFFD83A34),
        Color(0xFF43A047),
        Color(0xFFF57C00),
        Color(0xFF7B1FA2),
        Color(0xFFC2185B),
    )

private fun initialsFromName(name: String): String =
    name
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .take(1)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

private fun String.firstProfileInitial(): String =
    trim()
        .firstOrNull()
        ?.uppercase()
        ?: "?"
