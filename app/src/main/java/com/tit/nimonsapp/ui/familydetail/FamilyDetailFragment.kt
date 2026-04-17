package com.tit.nimonsapp.ui.familydetail

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.data.network.MaskedFamilyMemberDto
import com.tit.nimonsapp.ui.common.iconImage

class FamilyDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val familyId = arguments?.getInt("familyId") ?: -1

        return ComposeView(requireContext()).apply {
            setContent {
                FamilyDetailScreen(
                    familyId = familyId,
                    onBack = { findNavController().popBackStack() },
                    onLeaveSuccess = { findNavController().popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailScreen(
    familyId: Int,
    viewModel: FamilyDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onLeaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(familyId) {
        viewModel.loadFamilyDetail(familyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.familyDetail?.name ?: "Loading...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.meta.isLoading && uiState.familyDetail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.familyDetail != null) {
            val family = uiState.familyDetail!!
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA))
            ) {
                // Blue Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1967D2))
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            iconImage(iconUrl = family.iconUrl, size = 64.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = family.name,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${family.members.size} members · Created ${family.createdAt.take(10)}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Family Code Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "FAMILY CODE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1967D2)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = family.familyCode ?: "-----",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1967D2),
                                    letterSpacing = 4.sp
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Family Code", family.familyCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .background(Color(0xFF1967D2), CircleShape)
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Share this code so others can join your family",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "MEMBERS",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                // Members List
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            family.members.forEachIndexed { index, member ->
                                MemberItem(
                                    member = member,
                                    isMe = member.email == uiState.me?.email
                                )
                                if (index < family.members.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 72.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Leave Family Button
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    TextButton(
                        onClick = { viewModel.leaveFamily(onLeaveSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmittingAction
                    ) {
                        Text(
                            text = "Leave Family",
                            color = Color(0xFFD32F2F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun MemberItem(member: MaskedFamilyMemberDto, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(getRandomColor(member.fullName), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.fullName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.fullName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = member.email,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (isMe) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF4D2))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "You",
                    fontSize = 11.sp,
                    color = Color(0xFF856404),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getRandomColor(name: String): Color {
    val colors = listOf(
        Color(0xFF1967D2), Color(0xFFD32F2F), Color(0xFF388E3C), Color(0xFFF57C00),
        Color(0xFF7B1FA2), Color(0xFF00796B), Color(0xFF5D4037)
    )
    return colors[name.length % colors.size]
}
