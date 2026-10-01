package com.pravejxlab.focuspie.host_table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pravejxlab.focuspie.FocusPieRoute
import com.pravejxlab.focuspie.R
import com.pravejxlab.focuspie.home.google_sans_font
import com.pravejxlab.focuspie.join_table.AcceptConnectionRequestPage
import com.pravejxlab.focuspie.join_table.AdvertisementState
import com.pravejxlab.focuspie.join_table.StudentInfo
import com.pravejxlab.focuspie.manager.PayloadState
import com.pravejxlab.focuspie.manager.PayloadType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostTableScreen(
    navController: NavController,
    viewModel: HostTableViewModel = hiltViewModel()
) {
    val advertisementState by viewModel.advertisementState.collectAsStateWithLifecycle()
    val payloadState by viewModel.payloadState.collectAsStateWithLifecycle()

    LaunchedEffect(payloadState) {
        if (payloadState is PayloadState.Sent) {
            val payloadType = (payloadState as PayloadState.Sent).payloadType

            if (payloadType is PayloadType.StartTimeBroadcast) {
                navController.navigate(FocusPieRoute.HostLiveStudy)
            }
        }
    }

    if (advertisementState is AdvertisementState.Initiated) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { viewModel.denyConnection((advertisementState as AdvertisementState.Initiated).id) }
        ) {
            AcceptConnectionRequestPage(
                tableName = (advertisementState as AdvertisementState.Initiated).name,
                authDigits = (advertisementState as AdvertisementState.Initiated).authDigits,
                denyConnection = { viewModel.denyConnection((advertisementState as AdvertisementState.Initiated).id) },
                acceptConnection = { viewModel.acceptConnection((advertisementState as AdvertisementState.Initiated).id) }
            )
        }
    }

    Scaffold { innerPadding ->
        HostTablePage(
            innerPadding = innerPadding,
            connectedStudents = if (advertisementState is AdvertisementState.Connected) {
                (advertisementState as AdvertisementState.Connected).connectedStudents
            } else emptyList(),
            onStartStudyButtonClicked = { viewModel.startStudy() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HostTablePage(
    innerPadding: PaddingValues = PaddingValues(),
    connectedStudents: List<StudentInfo> = emptyList(),
    onStartStudyButtonClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.extra_light_blue)
                    )
                ) {
                    Text(
                        text = "Broadcasting has started, looking for students to join...",
                        fontFamily = google_sans_font,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }

            items(connectedStudents, key = { it.id }) { student ->
                AvailableStudentsSegment(student)
            }
        }

        if (connectedStudents.isNotEmpty()) {
            Button(
                onClick = onStartStudyButtonClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.light_blue),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start study",
                    fontFamily = google_sans_font,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun AvailableStudentsSegment(
    student: StudentInfo
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(100))
            .background(color = colorResource(R.color.extra_light_blue))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = student.name,
                fontFamily = google_sans_font
            )

            Text(
                text = "Connected",
                fontFamily = google_sans_font
            )
        }
    }
}