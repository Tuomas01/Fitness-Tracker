package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnesstracker.MainActivity
import kotlin.random.Random

@Composable
fun TrainingScreen(
    navigateToPlan: () -> Unit,
) {
    TrainingPlanCarousel(navigateToPlan)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanCarousel(
    navigateToPlan: () -> Unit,
    trainingViewModel: TrainingViewModel = hiltViewModel()
) {
    val carouselItems = remember {
        listOf(
            TrainingPlan(0, "High-Intensity Interval Training", "Cardio"),
            TrainingPlan(1, "After exercise yoga", "Stretch"),
            TrainingPlan(2, "Calisthenics beginner", "Strength"),
            TrainingPlan(3, "Full body weight training", "Strength"),
        )
    }

    HorizontalUncontainedCarousel(
        state = rememberCarouselState { carouselItems.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemWidth = 200.dp,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) { i ->
        val item = carouselItems[i]
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .padding(8.dp)
                .height(160.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(item.name)
                HorizontalDivider(thickness = 2.dp)
                Text("Type: ${item.type}")
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                IconButton(
                    onClick = {
                        trainingViewModel.savePlanInfo(item.id, item.name, item.type)
                        navigateToPlan()
                    }
                ) {
                    Icon(Icons.Default.ArrowForward, "Arrow forward")
                }
            }
        }
    }
}
