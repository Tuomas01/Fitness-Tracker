package com.example.fitnesstracker.ui.screens.authentication

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    val email by viewModel.email.collectAsState()
    //val password by viewModel.password.collectAsState()
    //val passwordlessAuth by viewModel.passwordlessAuth.collectAsState()

    val openTokenDialog = remember { mutableStateOf(false) }
    when {
        openTokenDialog.value -> {
            TokenDialog(
                onDismissRequest = { openTokenDialog.value = false },
                onConfirmation = {
                    openTokenDialog.value = false
                },
                context = context
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
        /*if (!passwordlessAuth) {
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
        }*/
        val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            onClick = {
                localSoftwareKeyboardController?.hide()
                openTokenDialog.value = !openTokenDialog.value
                viewModel.authenticateWithOtp()
                /*if (passwordlessAuth) {
                    openTokenDialog.value = !openTokenDialog.value
                } else {
                    coroutineScope.launch {
                        viewModel.authenticateUser()
                    }
                }*/
            }) {
            Text("Sign in")
        }
        /*Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                viewModel.changeAuthMethod()
            }
        ) {
            Text("Alternative authentication method")
        }*/
    }
}

@Composable
fun TokenDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    context: Context,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val otpToken by viewModel.otpToken.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

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
                    text = "An OTP code has been sent to your email, please input the code here.",
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
                        coroutineScope.launch {
                            viewModel.verifyOtp()
                        }
                        viewModel.getSession()
                        if (isLoggedIn) {
                            onConfirmation()
                        } else {
                            Toast.makeText(
                                context,
                                "OTP was incorrect, please enter the value again",
                                Toast.LENGTH_SHORT
                            ).show()
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