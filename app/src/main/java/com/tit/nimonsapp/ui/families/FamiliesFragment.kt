package com.tit.nimonsapp.ui.families

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.ui.common.iconImage

class FamiliesFragment : Fragment() {
    private val viewModel: FamiliesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FamiliesScreen(
                    viewModel = viewModel,
                    onCreateFamily = {
                        findNavController().navigate(R.id.action_familiesFragment_to_createFamilyFragment)
                    },
                    onNavigateToDetail = { familyId ->
                        val bundle = bundleOf("familyId" to familyId)
                        findNavController().navigate(R.id.action_familiesFragment_to_familyDetailFragment, bundle)
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadFamilies()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliesScreen(
    viewModel: FamiliesViewModel,
    onCreateFamily: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFamily,
                containerColor = Color(0xFFE8F0FE),
                contentColor = Color(0xFF1967D2),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Family")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Families",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search Bar
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        placeholder = { Text("Search families...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F0F7),
                            unfocusedContainerColor = Color(0xFFF3F0F7),
                            disabledContainerColor = Color(0xFFF3F0F7),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Filter Chips
                    Row {
                        FilterChip(
                            selected = uiState.selectedFilter == FamiliesFilter.ALL,
                            onClick = { viewModel.updateFilter(FamiliesFilter.ALL) },
                            label = { Text("All") },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1967D2),
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedFilter == FamiliesFilter.ALL,
                                borderColor = Color.LightGray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = uiState.selectedFilter == FamiliesFilter.MY_FAMILIES,
                            onClick = { viewModel.updateFilter(FamiliesFilter.MY_FAMILIES) },
                            label = { Text("My Families") },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1967D2),
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedFilter == FamiliesFilter.MY_FAMILIES,
                                borderColor = Color.LightGray
                            )
                        )
                    }
                }
            }

            // Pinned Section
            if (uiState.pinnedFamilies.isNotEmpty()) {
                item {
                    Text(
                        text = "PINNED",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                items(uiState.pinnedFamilies, key = { "pinned_${it.id}" }) { family ->
                    FamilyItemCompose(
                        family = family,
                        isPinned = true,
                        onPinClick = { viewModel.togglePinned(family.id) },
                        onClick = { onNavigateToDetail(family.id) }
                    )
                }
            }

            // All Families Section
            item {
                Text(
                    text = "ALL FAMILIES",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
            
            items(uiState.filteredAllFamilies, key = { "all_${it.id}" }) { family ->
                FamilyItemCompose(
                    family = family,
                    isPinned = family.id in uiState.pinnedFamilyIds,
                    onPinClick = { viewModel.togglePinned(family.id) },
                    onClick = { onNavigateToDetail(family.id) }
                )
            }
        }
    }
}

@Composable
fun FamilyItemCompose(
    family: GetFamiliesResponseDto,
    isPinned: Boolean,
    onPinClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0F7))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with white background
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                iconImage(iconUrl = family.iconUrl, size = 48.dp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = family.name,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            IconButton(onClick = onPinClick) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Pin",
                    tint = if (isPinned) Color(0xFF1967D2) else Color.LightGray
                )
            }
        }
    }
}
