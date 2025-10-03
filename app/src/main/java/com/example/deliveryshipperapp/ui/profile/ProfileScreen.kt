package com.example.deliveryshipperapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.deliveryshipperapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Thông tin Shipper") }) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val s = profile) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text("❌ ${s.message}")
                is Resource.Success -> {
                    val u = s.data!!
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("👤 ${u.name}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Email: ${u.email}")
                        Text("SĐT: ${u.phone}")
                        Text("Địa chỉ: ${u.address}")
                        Text("Vai trò: ${u.role}")
                        Text("Trạng thái: ${if (u.status == 1) "Hoạt động" else "Khoá"}")
                    }
                }
            }
        }
    }
}