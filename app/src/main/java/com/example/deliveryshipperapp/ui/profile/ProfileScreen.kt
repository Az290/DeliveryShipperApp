package com.example.deliveryshipperapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.deliveryshipperapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        "Thông tin Shipper",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
                    )
                )
                .padding(padding)
        ) {
            when (val state = profile) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is Resource.Error -> Text(
                    text = "❌ ${state.message}",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                is Resource.Success -> {
                    val user = state.data!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Name
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "SHIPPER",
                            color = Color(0xFFDCE1FF),
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        // Card: Info
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth()
                            ) {
                                InfoLine(label = "📧  Email", value = user.email)
                                InfoLine(label = "📞  SĐT", value = user.phone)
                                InfoLine(label = "🏠  Địa chỉ", value = user.address)
                                InfoLine(label = "⭐  Vai trò", value = user.role ?: "shipper")

                                val statusText = when (user.status) {
                                    1 -> "Hoạt động"
                                    2 -> "Đã khoá"
                                    0 -> "Chưa kích hoạt"
                                    else -> "Không rõ"
                                }
                                val statusColor = when (user.status) {
                                    1 -> Color(0xFF4CAF50)
                                    2 -> Color(0xFFF44336)
                                    else -> Color(0xFFFFC107)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "🔒  Trạng thái",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        statusText,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))

                        Text(
                            text = "Luôn nhiệt tình – An toàn – Đúng giờ!",
                            color = Color.White,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun InfoLine(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF555555), fontWeight = FontWeight.SemiBold)
        Text(text = value ?: "-", color = Color.Black, fontWeight = FontWeight.Medium)
    }
}