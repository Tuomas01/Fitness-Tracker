package com.example.fitnesstracker.ui.screens.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(
    viewModel: AuthViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val passwordlessAuth by viewModel.passwordlessAuth.collectAsState()

    val openTokenDialog = remember { mutableStateOf(false) }
    when {
        openTokenDialog.value -> {
            TokenDialog(
                onDismissRequest = { openTokenDialog.value = false },
                onConfirmation = {
                    openTokenDialog.value = false
                }
            )
        }
    }
    Column(
        modifier = Modifier
            .padding(20.dp)
            .wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                viewModel.updateEmail(it)
            },
            label = { Text("Email") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!passwordlessAuth) {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    viewModel.updatePassword(it)
                },
                label = { Text("Password") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            onClick = {
                localSoftwareKeyboardController?.hide()
                if (passwordlessAuth) {
                    openTokenDialog.value = !openTokenDialog.value
                } else {
                    coroutineScope.launch {
                        viewModel.authenticateUser()
                    }
                }
            }) {
            Text("Sign in")
        }
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                viewModel.changeAuthMethod()
            }
        ) {
            Text("Alternative authentication method")
        }
    }
}

@Composable
fun TokenDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val otpToken by viewModel.otpToken.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Please input the code from email here",
                    modifier = Modifier
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                )
                OutlinedTextField(
                    value = otpToken,
                    onValueChange = {
                        viewModel.updateToken(it)
                    },
                    label = { Text("OTP") },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                Button(
                    onClick = {
                        onConfirmation()
                        coroutineScope.launch {
                            viewModel.authenticateUser()
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}