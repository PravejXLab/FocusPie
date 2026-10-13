package com.pravejxlab.focuspie.ui.live_study

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pravejxlab.focuspie.FocusPieRoute
import com.pravejxlab.focuspie.ui.common.LiveStudyPage

@Composable
fun HostLiveStudyScreen(
    navController: NavController,
    viewModel: HostLiveStudyViewModel = hiltViewModel()
) {
    val timeLeft by viewModel.timeLeft.collectAsStateWithLifecycle()

    LaunchedEffect(timeLeft) {
        if (timeLeft == 1L) {
            navController.navigate(FocusPieRoute.Appreciation(1L)) {
                popUpTo(FocusPieRoute.Home)
            }
        }

        if (timeLeft == 0L) {
            navController.navigate(FocusPieRoute.Appreciation(0L)) {
                popUpTo(FocusPieRoute.Home)
            }
        }
    }

    BackHandler(true) {}

    Scaffold { innerPadding ->
        LiveStudyPage(
            innerPadding = innerPadding,
            timeLeft = timeLeft,
            studyingStudents = viewModel.connectedStudents.toList()
        )
    }
}