package com.pravejxlab.focuspie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pravejxlab.focuspie.ui.appreciation.AppreciationScreen
import com.pravejxlab.focuspie.ui.home.HomeScreen
import com.pravejxlab.focuspie.ui.host_table.HostTableScreen
import com.pravejxlab.focuspie.ui.join_table.JoinTableScreen
import com.pravejxlab.focuspie.ui.live_study.HostLiveStudyScreen
import com.pravejxlab.focuspie.ui.live_study.StudentLiveStudyScreen
import com.pravejxlab.focuspie.ui.theme.FocusPieTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusPieTheme {
                val navController = rememberNavController()

                NavHost(navController, FocusPieRoute.Home) {
                    composable<FocusPieRoute.Home> { HomeScreen(navController) }
                    composable<FocusPieRoute.HostTable> { HostTableScreen(navController) }
                    composable<FocusPieRoute.JoinTable> { JoinTableScreen(navController) }
                    composable<FocusPieRoute.HostLiveStudy> { HostLiveStudyScreen(navController) }
                    composable<FocusPieRoute.StudentLiveStudy> { StudentLiveStudyScreen(navController) }

                    composable<FocusPieRoute.Appreciation> { backStackEntry ->
                        val routeInfo = backStackEntry.toRoute<FocusPieRoute.Appreciation>()

                        AppreciationScreen(navController, routeInfo.timeLeft)
                    }
                }
            }
        }
    }
}