package com.pravejxlab.focuspie.ui.host_table

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pravejxlab.focuspie.FocusPieRoute
import com.pravejxlab.focuspie.ui.common.AcceptConnectionRequestPage
import com.pravejxlab.focuspie.ui.common.StudentInfo
import com.pravejxlab.focuspie.domain.ConnectionState
import com.pravejxlab.focuspie.domain.PayloadState
import com.pravejxlab.focuspie.domain.PayloadType
import com.pravejxlab.focuspie.ui.common.getRequiredPermissions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.pravejxlab.focuspie.R
import com.pravejxlab.focuspie.ui.home.google_sans_font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostTableScreen(
    navController: NavController,
    viewModel: HostTableViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val payloadState by viewModel.payloadState.collectAsStateWithLifecycle()

    LaunchedEffect(payloadState) {
        if (payloadState is PayloadState.Sent) {
            val payloadType = (payloadState as PayloadState.Sent).payloadType

            if (payloadType is PayloadType.StartTimeBroadcast) {
                navController.navigate(FocusPieRoute.HostLiveStudy)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { true }) {
            viewModel.startAdvertising()
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
        HostTablePage(
            innerPadding = innerPadding,
            connectedStudents = viewModel.connectedStudents.toList(),
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
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
    ) {
        // Page Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Host a Table",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    // fontFamily = google_sans_font
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your table is open and visible to nearby students.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (connectedStudents.isEmpty()) {
                BroadcastingEmptyState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(connectedStudents, key = { it.id }) { student ->
                        StudentItemCard(student = student)
                    }
                }
            }
        }

        // Pinned Bottom Button
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentItemCard(
    student: StudentInfo
) {
    Card(
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
            // Avatar Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.person),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Student Name
            Text(
                text = student.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Connected Status Badge with Icon
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check_circle),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "CONNECTED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun BroadcastingEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing or static broadcast icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.wifi_tethring),
                contentDescription = "Broadcasting",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Broadcasting...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Waiting for students to discover and join your table.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}