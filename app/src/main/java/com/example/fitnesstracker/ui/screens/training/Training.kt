package com.example.fitnesstracker.ui.screens.training

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme

@Composable
fun TrainingScreen() {
    GreetingTraining("Testing Training page")
}

@Composable
fun GreetingTraining(pageName: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $pageName!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    FitnessTrackerTheme {
        GreetingTraining("Training")
    }
}