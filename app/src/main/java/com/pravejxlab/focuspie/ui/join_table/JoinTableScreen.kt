package com.pravejxlab.focuspie.ui.join_table

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pravejxlab.focuspie.FocusPieRoute
import com.pravejxlab.focuspie.domain.ConnectionState
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.PayloadType
import com.pravejxlab.focuspie.ui.common.AcceptConnectionRequestPage
import com.pravejxlab.focuspie.ui.common.TableInfo
import com.pravejxlab.focuspie.ui.common.getRequiredPermissions
import com.pravejxlab.focuspie.ui.common.isLocationEnabled
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pravejxlab.focuspie.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTableScreen(
    navController: NavController,
    viewModel: JoinTableViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val payloadState by viewModel.payloadState.collectAsStateWithLifecycle()

    LaunchedEffect(payloadState) {
        if (payloadState is PayloadState.Received) {
            val payloadType = (payloadState as PayloadState.Received).payloadType

            val canNavigate = payloadType is PayloadType.StartTimeBroadcast || payloadType is PayloadType.TriggerStartStudy
            if (canNavigate) {
                navController.navigate(FocusPieRoute.StudentLiveStudy)
                viewModel.stopDiscovery()
            }
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (isLocationEnabled(context)) {
            viewModel.startDiscovery()
        } else {
            navController.popBackStack()
            Toast.makeText(context, "Please enable location to continue", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { true }) {
            if (Build.VERSION.SDK_INT <= 30) {
                if (isLocationEnabled(context)) {
                    viewModel.startDiscovery()
                } else {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    locationLauncher.launch(intent)
                }
            } else {
                viewModel.startDiscovery()
            }
        } else {
            navController.popBackStack()
            Toast.makeText(context, "Please allow all required permissions to continue", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(getRequiredPermissions())
    }

    if (connectionState is ConnectionState.Initiated) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { viewModel.denyConnection((connectionState as ConnectionState.Initiated).id) }
        ) {
            AcceptConnectionRequestPage(
                tableName = (connectionState as ConnectionState.Initiated).name,
                authDigits = (connectionState as ConnectionState.Initiated).authDigits,
                denyConnection = { viewModel.denyConnection((connectionState as ConnectionState.Initiated).id) },
                acceptConnection = { viewModel.acceptConnection((connectionState as ConnectionState.Initiated).id) }
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
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Page Header
        Text(
            text = "Join a Table",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                // fontFamily = google_sans_font // Add your font here
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Find an open table to join, or ask your host to start one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle between Empty State and Table List
        if (availableTables.isEmpty()) {
            EmptyTableState()
        } else {
            AvailableTablesSegment(
                availableTables = availableTables,
                onTableClicked = onTableClicked
            )
        }
    }
}

@Composable
fun AvailableTablesSegment(
    availableTables: List<TableInfo>,
    onTableClicked: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Adds clean spacing between items
    ) {
        items(availableTables) { table ->
            TableItemCard(
                table = table,
                onClick = { onTableClicked(table.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableItemCard(
    table: TableInfo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Icon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.person),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Table Name and Subtext
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to join",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Clean Status Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = table.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun EmptyTableState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.info),
            contentDescription = "No tables",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tables available",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Wait for a host to start a table so you can join.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}