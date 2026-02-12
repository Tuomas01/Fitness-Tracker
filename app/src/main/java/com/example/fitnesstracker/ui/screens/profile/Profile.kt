package com.example.fitnesstracker.ui.screens.profile

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme

// Profile screen view
@Composable
fun ProfileScreen() {
    // Creates a new viewmodel that is being passed to the UserTextFields composable
    val viewModel = ProfileViewModel()
    UserTextFields(viewModel)
}

// Composable that shows text fields where user can view and change their information
// Uses the viewmodel passed as an argument to handle user information. Logic for this can be found in the ProfileViewModel
@Composable
fun UserTextFields(
    viewModel: ProfileViewModel
) {
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
            .fillMaxSize()
            .wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("User Name") }
        )

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        // A read only text field for the gender picker. Changes the openDialogRadio value to show the dialog on press
        // Onclick function wouldn't work on the modifier so that is why the text field is using pointerInput instead to handle user press
        OutlinedTextField(
            value = viewModel.gender,
            onValueChange = { viewModel.updateGender(it) },
            label = { Text("Gender") },
            readOnly = true,
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        openDialogRadio.value = !openDialogRadio.value
                    }
                }
            }
        )

        OutlinedTextField(
            value = viewModel.age,
            onValueChange = { viewModel.updateAge(it) },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        OutlinedTextField(
            value = viewModel.height,
            onValueChange = { viewModel.updateHeight(it) },
            label = { Text("Height") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            suffix = {
                Text(text = "cm")
            }
        )

        OutlinedTextField(
            value = viewModel.weight,
            onValueChange = { viewModel.updateWeight(it) },
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            suffix = {
                Text(text = "kg")
            }
        )
    }
}

// Radio button for selecting gender
@Composable
fun GenderRadioButtons(modifier: Modifier = Modifier) {
    val radioOptions = listOf("Male", "Female", "Other")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    Column(modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// Dialog composable that is shown when user clicks on the gender text field
// This composable calls the GenderRadioButtons composable, so that the user can select their gender from the dialog instead of typing it
@Composable
fun DialogRadio(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GenderRadioButtons()
                TextButton(
                    onClick = { onConfirmation() }
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    FitnessTrackerTheme {
        ProfileScreen()
    }
}