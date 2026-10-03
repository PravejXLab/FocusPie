package com.pravejxlab.focuspie.join_table

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pravejxlab.focuspie.FocusPieRoute
import com.pravejxlab.focuspie.R
import com.pravejxlab.focuspie.home.google_sans_font
import com.pravejxlab.focuspie.manager.PayloadState
import com.pravejxlab.focuspie.manager.PayloadType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTableScreen(
    navController: NavController,
    viewModel: JoinTableViewModel = hiltViewModel()
) {
    val advertisementState by viewModel.advertisementState.collectAsStateWithLifecycle()
    val payloadState by viewModel.payloadState.collectAsStateWithLifecycle()

    LaunchedEffect(payloadState) {
        if (payloadState is PayloadState.Received) {
            val payloadType = (payloadState as PayloadState.Received).payloadType

            if (payloadType is PayloadType.StartTimeBroadcast) {
                navController.navigate(FocusPieRoute.StudentLiveStudy)
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
        JoinTablePage(
            innerPadding = innerPadding,
            availableTables = viewModel.availableTables,
            onTableClicked = { viewModel.requestConnection(it) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun JoinTablePage(
    innerPadding: PaddingValues = PaddingValues(),
    availableTables: List<TableInfo> = emptyList(),
    onTableClicked: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.extra_light_green)
            )
        ) {
            Text(
                text = "Looking for available tables, ask your host to start a table...",
                fontFamily = google_sans_font,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(32.dp)
            )
        }

        AvailableTablesSegment(
            availableTables = availableTables,
            onTableClicked = { onTableClicked(it) }
        )
    }
}

@Composable
fun AvailableTablesSegment(
    availableTables: List<TableInfo> = emptyList(),
    onTableClicked: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(availableTables) { table ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(100))
                    .background(color = colorResource(R.color.extra_light_green))
                    .clickable { onTableClicked(table.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = table.name,
                        fontFamily = google_sans_font
                    )

                    Text(
                        text = table.status,
                        fontFamily = google_sans_font
                    )
                }
            }
        }
    }
}