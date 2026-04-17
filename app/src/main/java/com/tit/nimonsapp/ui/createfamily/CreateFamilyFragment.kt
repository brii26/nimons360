package com.tit.nimonsapp.ui.createfamily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R
import com.tit.nimonsapp.ui.common.iconImage

class CreateFamilyFragment : Fragment() {
    private val viewModel: CreateFamilyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CreateFamilyScreen(
                    viewModel = viewModel,
                    onBack = { findNavController().popBackStack() },
                    onSuccess = { 
                        findNavController().navigate(R.id.action_createFamilyFragment_to_familiesFragment)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateFamilyScreen(
    viewModel: CreateFamilyViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdFamily) {
        if (uiState.createdFamily != null) {
            viewModel.consumeCreatedFamily()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Family", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.createFamily() },
                        enabled = uiState.name.isNotBlank() && !uiState.meta.isLoading
                    ) {
                        if (uiState.meta.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Create", fontWeight = FontWeight.Bold, color = Color(0xFF1967D2))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Main Preview Icon
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color(0xFF1967D2) // Blue background for preview
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        iconImage(iconUrl = uiState.iconUrl, size = 64.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Family Name Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FAMILY NAME",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { if (it.length <= 40) viewModel.onNameChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Tedjumama") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text("${uiState.name.length}/40")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1967D2),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1967D2), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "After creating the family, an auto-generated family code will be produced. Members will need to enter this code to join.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Icon Picker
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CHOOSE AN ICON",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    familyIcons.forEach { url ->
                        val isSelected = uiState.iconUrl == url
                        Surface(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable { viewModel.onIconUrlChanged(url) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color.White else Color(0xFFF5F5F5),
                            border = if (isSelected) BorderStroke(2.dp, Color(0xFF1967D2)) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                iconImage(iconUrl = url, size = 40.dp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
