package com.pravejxlab.focuspie

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

val google_sans_font = FontFamily(Font(R.font.google_sans_font))

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { HomeTopBar() }
    ) { innerPadding ->
        HomePage(
            innerPadding = innerPadding,
            onHostTableClicked = {},
            onJoinGroupClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePage(
    innerPadding: PaddingValues = PaddingValues(),
    onHostTableClicked: () -> Unit = {},
    onJoinGroupClicked: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        item { TotalStudyHero("04:34:19") }
        item { Spacer(Modifier.height(32.dp)) }

        item {
            LibraryActionCard(
                title = "Host a table",
                description = "Create a local focus group as host.",
                iconId = R.drawable.host,
                containerColor = Color(0xFF6BCBD7),
                onClick = onHostTableClicked
            )
        }
        item { Spacer(Modifier.height(16.dp)) }

        item {
            LibraryActionCard(
                title = "Join a table",
                description = "Scan the room for active study groups.",
                iconId = R.drawable.table,
                containerColor = Color(0xFF60C765),
                onClick = {}
            )
        }
    }
}

@Composable
fun TotalStudyHero(time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Total Focus Today",
                fontFamily = google_sans_font,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // The massive timer display
        Text(
            text = time,
            fontFamily = google_sans_font,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 2.sp
        )
    }
}


@Composable
fun LibraryActionCard(
    title: String,
    description: String,
    @DrawableRes iconId: Int,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Section: Icon and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A slightly translucent circle behind the icon makes it pop
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconId),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    fontFamily = google_sans_font,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Bottom Section: Translucent Description Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Black.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = description,
                    fontFamily = google_sans_font,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = dateString(),
                fontFamily = google_sans_font,
                style = MaterialTheme.typography.titleMedium
            )
        },

        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "Profile"
                )
            }
        },

        actions = {
            TimeLeftString()
        }
    )
}

fun dateString(): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

    return LocalDate.now().format(formatter)
}

@Composable
fun TimeLeftString() {
    var timeLefts by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val startOfDay = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0,0,0))
            val now = LocalDateTime.now()

            val timeLeft = Duration.between(now, startOfDay)
            val seconds = timeLeft.seconds % 60
            val hours = timeLeft.toHours()
            val minutes = timeLeft.toMinutes() % 60

            timeLefts = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            delay(1.seconds)
        }
    }

    Text(
        text = timeLefts,
        fontFamily = google_sans_font,
        modifier = Modifier.padding(end = 16.dp),
        style = MaterialTheme.typography.titleMedium
    )
}