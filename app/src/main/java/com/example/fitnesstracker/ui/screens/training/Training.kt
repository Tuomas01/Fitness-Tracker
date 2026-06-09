package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnesstracker.MainActivity
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonNull
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random

@Composable
fun TrainingScreen(
    navigateToPlan: () -> Unit,
    trainingViewModel: TrainingViewModel
) {
    val trainingPlans = trainingViewModel.listOfPlans.collectAsState()
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
    ) {
        val uniqueTypes = remember { mutableListOf<String>() }
        TrainingPlanCarousel(navigateToPlan)
        for (i in trainingPlans.value) {
            if (!uniqueTypes.contains(i.type)) {
                uniqueTypes.add(i.type)
                TrainingPlanCarousel(
                    navigateToPlan,
                    planType = i.type,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanCarousel(
    navigateToPlan: () -> Unit,
    trainingViewModel: TrainingViewModel = hiltViewModel(),
    planType: String = "All",
) {
    val trainingPlans = trainingViewModel.listOfPlans.collectAsState()
    var plansCount = trainingPlans.value.count()

    ElevatedCard(
        modifier = Modifier
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "$planType workout plans",
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState(initialItem = 0) {
                //plansCount = trainingPlans.value.count()
                /*if (planType != "All") {
                    plansCount = 0
                    for (i in trainingPlans.value) {
                        if (i.type == planType || i.type.contains(planType)) {
                            println(":DDDDD ${i.type}, :D $plansCount")
                            plansCount += 1
                        }
                    }
                }*/
                trainingPlans.value.count()
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 8.dp),
            preferredItemWidth = 200.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { i ->
            val item = trainingPlans.value[i]
            if (planType == "All" || (item.type == planType || item.type.contains(planType))) {
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
                        Text("${item.name} plan")
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
                                println(":DDDDDD $item")
                                trainingViewModel.savePlanInfo(item.id, item.name, item.type)
                                trainingViewModel.addExercisesToPlan(item.id)
                                navigateToPlan()
                            }
                        ) {
                            Icon(Icons.Default.ArrowForward, "Arrow forward")
                        }
                    }
                }
            }
        }
    }
}