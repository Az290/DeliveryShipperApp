package com.example.deliveryshipperapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deliveryshipperapp.utils.Resource
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🚚 Shipper Login", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.login(email, password) }
            ) { Text("Login") }

            when (val s = state) {
                is Resource.Loading -> {
                    CircularProgressIndicator(Modifier.padding(top = 16.dp))
                }
                is Resource.Error -> {
                    LaunchedEffect(s) {
                        scope.launch {
                            snackbarHostState.showSnackbar("❌ ${s.message}")
                        }
                    }
                }
                is Resource.Success -> {
                    LaunchedEffect(s) {
                        scope.launch {
                            snackbarHostState.showSnackbar("✅ Welcome shipper!")
                        }
                        // ➡️ Điều hướng vào main
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}