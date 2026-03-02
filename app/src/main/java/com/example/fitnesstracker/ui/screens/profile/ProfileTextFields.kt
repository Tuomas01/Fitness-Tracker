package com.example.fitnesstracker.ui.screens.profile

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Composable that shows text fields where user can view and change their information
// Uses the viewmodel passed as an argument to handle user information. Logic for this can be found in the ProfileViewModel
@Composable
fun ProfileTextFields(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    // Handles showing the gender picker dialog
    val openDialogRadio = remember { mutableStateOf(false) }
    when {
        openDialogRadio.value -> {
            DialogRadio(
                onDismissRequest = { openDialogRadio.value = false },
                onConfirmation = {
                    openDialogRadio.value = false
                }
            )
        }
    }
    Column(
        modifier = Modifier
            .wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = userState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Name") },
            maxLines = 1,
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        viewModel.clearName()
                    }
                }
            }
        )

        OutlinedTextField(
            value = userState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            maxLines = 1,
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        viewModel.clearEmail()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
        )

        // A read only text field for the gender picker. Changes the openDialogRadio value to show the dialog on press
        // Onclick function wouldn't work on the modifier so that is why the text field is using pointerInput instead to handle user press
        OutlinedTextField(
            value = userState.gender,
            onValueChange = { viewModel.updateGender(it) },
            label = { Text("Gender") },
            maxLines = 1,
            readOnly = true,
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        viewModel.clearGender()
                        openDialogRadio.value = !openDialogRadio.value
                    }
                }
            }
        )

        OutlinedTextField(
            value = userState.age,
            onValueChange = { viewModel.updateAge(it) },
            label = { Text("Age") },
            maxLines = 1,
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        viewModel.clearAge()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )

        OutlinedTextField(
            value = userState.height,
            onValueChange = { viewModel.updateHeight(it) },
            label = { Text("Height") },
            maxLines = 1,
            suffix = {
                Text(text = "cm")
            },
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        viewModel.clearHeight()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )

        OutlinedTextField(
            value = userState.weight,
            onValueChange = { viewModel.updateWeight(it) },
            label = { Text("Weight") },
            maxLines = 1,
            suffix = {
                Text(text = "kg")
            },
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        viewModel.clearWeight()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )
    }
}