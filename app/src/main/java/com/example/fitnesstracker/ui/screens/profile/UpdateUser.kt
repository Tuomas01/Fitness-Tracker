package com.example.fitnesstracker.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UpdateUserScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProfileTextFields()
        UpdateUserButton()
    }
}

@Composable
fun UpdateUserButton(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Button(
        onClick = {
            viewModel.updateUser()
        },
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text("Update User")
    }
}