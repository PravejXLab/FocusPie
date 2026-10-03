package com.pravejxlab.focuspie.live_study

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StudentLiveStudyScreen(
    viewModel: StudentLiveStudyViewModel = hiltViewModel()
) {
    val timeLeft by viewModel.timeLeft.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        LiveStudyPage(
            innerPadding = innerPadding,
            timeLeft = timeLeft,
            studyingStudents = viewModel.connectedStudents
        )
    }
}