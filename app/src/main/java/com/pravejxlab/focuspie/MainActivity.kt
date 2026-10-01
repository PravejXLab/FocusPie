package com.pravejxlab.focuspie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pravejxlab.focuspie.home.HomeScreen
import com.pravejxlab.focuspie.host_table.HostTableScreen
import com.pravejxlab.focuspie.join_table.JoinTableScreen
import com.pravejxlab.focuspie.live_study.HostLiveStudyScreen
import com.pravejxlab.focuspie.live_study.StudentLiveStudyScreen
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
                    composable<FocusPieRoute.HostLiveStudy> { HostLiveStudyScreen() }
                    composable<FocusPieRoute.StudentLiveStudy> { StudentLiveStudyScreen() }
                }
            }
        }
    }
}