package com.example.fitnesstracker.ui.screens.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme

@Composable
fun HomeScreen() {
    GreetingHome("Testing Home page")
}

@Composable
fun GreetingHome(pageName: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $pageName!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FitnessTrackerTheme {
        HomeScreen()
    }
}