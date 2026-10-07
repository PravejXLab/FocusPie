package com.pravejxlab.focuspie.ui.appreciation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun AppreciationScreen(
    navController: NavController,
    timeLeft: Long,
    viewModel: AppreciationViewModel = hiltViewModel()
) {
    BackHandler(true) {
        viewModel.destroyConnection()
        navController.popBackStack()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (timeLeft == 0L) "🥺" else "🎉",
                fontSize = 60.sp
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = if (timeLeft == 0L) {
                    "Sorry, discipline broke this time. Please don't regret, just try again."
                } else {
                    "Congratulations! your team has shown great discipline this time, keep it up."
                },
                textAlign = TextAlign.Center
            )
        }
    }
}