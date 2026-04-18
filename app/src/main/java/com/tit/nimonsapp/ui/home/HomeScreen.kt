package com.tit.nimonsapp.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tit.nimonsapp.ui.common.sectionTitle
import com.tit.nimonsapp.ui.home.components.discoverFamiliesCard
import com.tit.nimonsapp.ui.home.components.emptyCard
import com.tit.nimonsapp.ui.home.components.homeHeader
import com.tit.nimonsapp.ui.home.components.myFamilyCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun homeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToCreateFamily: () -> Unit,
    onGoToFamilyDetail: (Int) -> Unit,
    onJoinFamily: (Int) -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(state.meta.errorMessage) {
        state.meta.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = state.isRefreshing,
            onRefresh = onRefresh,
        )

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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

            item { sectionTitle("MY FAMILIES") }

            item {
                if (state.myFamilies.isEmpty() && state.meta.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                } else if (state.myFamilies.isEmpty()) {
                    emptyCard("You are not in any family yet.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(state.myFamilies) { myFamilyCard(family = it, onClick = { onGoToFamilyDetail(it.id) }) }
                    }
                }
            }

            item { sectionTitle("DISCOVER FAMILIES") }

            item {
                if (state.discoverFamilies.isEmpty() && state.meta.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                } else if (state.discoverFamilies.isEmpty()) {
                    emptyCard("No families to discover right now.")
                } else {
                    discoverFamiliesCard(
                        families = state.discoverFamilies,
                        onFamilyClick = onGoToFamilyDetail,
                        onJoinClick = onJoinFamily,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(96.dp)) }
        }

        PullRefreshIndicator(
            refreshing = state.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
        )

        FloatingActionButton(
            onClick = onGoToCreateFamily,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp),
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create family")
        }
    }
}

private fun String.firstProfileInitial(): String = trim().firstOrNull()?.uppercase() ?: "?"
