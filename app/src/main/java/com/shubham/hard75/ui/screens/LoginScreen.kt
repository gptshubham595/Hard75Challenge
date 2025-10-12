package com.shubham.hard75.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shubham.hard75.model.AuthUiState
import com.shubham.hard75.ui.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreenRoot(
    authViewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState: AuthUiState by authViewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onLoginSuccess = onLoginSuccess,
        onSignInClick = {
            authViewModel.signInWithGoogle(it)
        }
    )
}

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLoginSuccess: () -> Unit,
    onSignInClick: (Context) -> Unit
) {
    val context = LocalContext.current

    // Handle successful login
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the", style = MaterialTheme.typography.headlineSmall)
            Text("75 Hard Challenge", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    onSignInClick(context)
                }) {
                    Text("Sign in with Google")
                }
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        uiState = AuthUiState(
            isLoading = false,
            isSuccess = false,
            errorMessage = null
        ),
        onLoginSuccess = {},
        onSignInClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    LoginScreen(
        uiState = AuthUiState(
            isLoading = true,
            isSuccess = false,
            errorMessage = null
        ),
        onLoginSuccess = {},
        onSignInClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorPreview() {
    LoginScreen(
        uiState = AuthUiState(
            isLoading = false,
            isSuccess = false,
            errorMessage = "An unknown error occurred."
        ),
        onLoginSuccess = {},
        onSignInClick = {}
    )
}