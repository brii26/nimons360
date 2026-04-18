package com.tit.nimonsapp.ui.familydetail

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
                familyDetailScreen(
                    familyId = familyId,
                    onBack = { findNavController().popBackStack() },
                    onLeaveSuccess = { findNavController().popBackStack() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun familyDetailScreen(
    familyId: Int,
    viewModel: FamilyDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onLeaveSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(familyId) {
        viewModel.loadFamilyDetail(familyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.familyDetail?.name ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.meta.isLoading && uiState.familyDetail == null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.familyDetail != null) {
            val family = uiState.familyDetail!!
            val isMember = family.isMember

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF8F9FA)),
            ) {
                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF4CAF50))
                                .padding(24.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            iconImage(iconUrl = family.iconUrl, size = 64.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = family.name,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${family.members.size} members · Created ${family.createdAt.take(10)}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                )

                                if (!isMember) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp),
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Not a member",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isMember) {
                    item {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "FAMILY CODE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = family.familyCode ?: "-----",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4CAF50),
                                        letterSpacing = 4.sp,
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard =
                                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip =
                                                android.content.ClipData.newPlainText(
                                                    "Family Code",
                                                    family.familyCode,
                                                )
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier =
                                            Modifier
                                                .background(Color(0xFF4CAF50), CircleShape)
                                                .size(40.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Share this code so others can join your family",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "MEMBERS",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                    )
                }

                item {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column {
                            family.members.forEachIndexed { index, member ->
                                memberItem(
                                    member = member,
                                    isMe = member.email == uiState.me?.email,
                                    isBlurred = !isMember,
                                )
                                if (index < family.members.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 72.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isMember) {
                    item {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFF57C00),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Join this family to see member details and interact with them on the map.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5D4037),
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(
                                onClick = { showJoinDialog = true },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            ) {
                                Text("Join Family", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You'll need the family code to join",
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                }

                if (isMember) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        TextButton(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmittingAction,
                        ) {
                            Text(
                                text = "Leave Family",
                                color = Color(0xFFD32F2F),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    if (showJoinDialog) {
        joinFamilyDialog(
            errorMessage = uiState.meta.errorMessage,
            isLoading = uiState.isSubmittingAction,
            onJoin = { code ->
                viewModel.joinFamily(
                    code,
                    onJoinSuccess = {
                        showJoinDialog = false
                    },
                )
            },
            onDismiss = {
                showJoinDialog = false
                viewModel.clearError()
            },
        )
    }

    if (showLeaveDialog) {
        leaveFamilyDialog(
            familyName = uiState.familyDetail?.name ?: "",
            isLoading = uiState.isSubmittingAction,
            onLeave = {
                viewModel.leaveFamily(onLeaveSuccess)
                showLeaveDialog = false
            },
            onDismiss = { showLeaveDialog = false },
        )
    }
}

@Composable
fun joinFamilyDialog(
    errorMessage: String?,
    isLoading: Boolean,
    onJoin: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val isError = errorMessage != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Join Family",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter the family code shared by a member of the family to join.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        if (input.length <= 6) {
                            code = input.uppercase()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Family Code") },
                    placeholder = { Text("e.g. TEGGER") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(text = errorMessage ?: "Wrong Family Code!", color = Color.Red)
                        } else {
                            Text(
                                text = "Ask a member of the family for the code",
                                color = Color.Gray,
                            )
                        }
                    },
                    trailingIcon = {
                        Text(
                            text = "${code.length}/6",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp),
                            color = if (code.length == 6) Color(0xFF4CAF50) else Color.Gray,
                        )
                    },
                    textStyle =
                        TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                        ),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color.LightGray,
                            errorBorderColor = Color.Red,
                            focusedLabelColor = Color(0xFF4CAF50),
                            errorLabelColor = Color.Red,
                        ),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onJoin(code) },
                        enabled = code.length == 6 && !isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Join", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun leaveFamilyDialog(
    familyName: String,
    isLoading: Boolean,
    onLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .background(Color(0xFFFFEBEE), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Leave Family",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text =
                        buildString {
                            append("Are you sure you want to leave ")
                            append(familyName)
                            append("?")
                        },
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onLeave,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Leave", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun memberItem(
    member: MaskedFamilyMemberDto,
    isMe: Boolean,
    isBlurred: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .then(if (isBlurred) Modifier.blur(8.dp) else Modifier)
                    .background(getRandomColor(member.fullName), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (!isBlurred) {
                Text(
                    text = member.fullName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isBlurred) {
                Box(
                    modifier =
                        Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray.copy(alpha = 0.5f)),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier =
                        Modifier
                            .width(180.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                )
            } else {
                Text(
                    text = member.fullName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = member.email,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }

        if (isMe && !isBlurred) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF4D2))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "You",
                    fontSize = 11.sp,
                    color = Color(0xFF856404),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun getRandomColor(name: String): Color {
    val colors =
        listOf(
            Color(0xFF4CAF50),
            Color(0xFFD32F2F),
            Color(0xFFE65100),
            Color(0xFFF57C00),
            Color(0xFF0097A7),
            Color(0xFF00796B),
            Color(0xFF5D4037),
        )
    return colors[name.length % colors.size]
}
