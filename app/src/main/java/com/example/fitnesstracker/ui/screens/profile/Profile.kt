package com.example.fitnesstracker.ui.screens.profile

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnesstracker.ui.screens.authentication.AuthViewModel
import kotlinx.coroutines.launch

// Profile screen view
@Composable
fun ProfileScreen(
    onNavigate: () -> Unit,
    clearBackStack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    val user by viewModel.userState.collectAsState()

    val openLinkEmailDialog = remember { mutableStateOf(false) }
    when {
        openLinkEmailDialog.value -> {
            LinkEmailDialog(
                onDismissRequest = { openLinkEmailDialog.value = false },
                clearBackStack = clearBackStack
            )
        }
    }
    Column(
        modifier = Modifier
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ProfileIcon(clearBackStack)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isAnonymous) {
                Text("Logged in as a guest")
                Button(
                    onClick = {
                        openLinkEmailDialog.value = !openLinkEmailDialog.value
                    }
                ) {
                    Text("Link email")
                }
            } else {
                Text("Logged in as ${user.email}")
                Button(
                    onClick = {
                        onNavigate()
                    }
                ) {
                    Text("Update user")
                }
            }
        }
    }
}

@Composable
fun ProfileIcon(
    clearBackStack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Account Icon",
                modifier = Modifier
                    .size(150.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .matchParentSize()
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
        ) {
            IconButton(
                onClick = {
                    viewModel.signOut()
                    clearBackStack()
                },
                modifier = Modifier
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = "Sign out button"
                )
            }
        }
    }
}

@Composable
fun LinkEmailDialog(
    onDismissRequest: () -> Unit,
    clearBackStack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val userState by viewModel.userState.collectAsState()
    val context = LocalContext.current

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
                    text = "Please enter an email you want to link this account to. After linking email to the account, you will be signed out. Press close if you want to keep browsing as a guest.",
                    modifier = Modifier
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                )
                OutlinedTextField(
                    value = userState.email,
                    onValueChange = {
                        viewModel.updateEmail(it)
                    },
                    label = { Text("Email") },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.updateUserEmail()
                            if (success) {
                                authViewModel.signOut()
                                clearBackStack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Something went wrong with linking email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 16.dp, 6.dp)
                        .fillMaxWidth()
                ) {
                    Text("Confirm email")
                }
                Button(
                    onClick = {
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .fillMaxWidth(),
                ) {
                    Text("Close")
                }
            }
        }
    }
}