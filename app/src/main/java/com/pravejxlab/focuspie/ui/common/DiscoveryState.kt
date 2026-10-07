package com.pravejxlab.focuspie.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.pravejxlab.focuspie.R

import com.pravejxlab.focuspie.ui.home.google_sans_font
import com.pravejxlab.focuspie.domain.EndpointInfo
import com.pravejxlab.focuspie.domain.Status
import kotlinx.serialization.Serializable

@Serializable
data class StudentInfo(
    val id: String,
    val name: String
)

fun Collection<EndpointInfo>.toTablesInfo() = map {
    TableInfo(
        id = it.endpointId,
        name = it.endpointName,
        status = if (it.status == Status.None || it.status == Status.Awaiting) "" else it.status.name
    )
}
fun Collection<EndpointInfo>.toStudentsInfo() = map { StudentInfo(it.endpointId, it.endpointName) }

data class TableInfo(
    val id: String,
    val name: String,
    val status: String = Status.Awaiting.name
)


@Preview(showBackground = true)
@Composable
fun AcceptConnectionRequestPage(
    tableName: String = "",
    authDigits: String = "",
    denyConnection: () -> Unit = {},
    acceptConnection: () -> Unit = {}
) {
    if (tableName.isEmpty() || authDigits.isBlank()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.enhanced_encryption),
                contentDescription = "Security Authentication",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Secure Connection",
            fontFamily = google_sans_font,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Connecting with \"$tableName\"",
            fontFamily = google_sans_font,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = authDigits,
                    fontFamily = google_sans_font,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Verify this code exactly matches the one shown on the other device before accepting.",
                fontFamily = google_sans_font,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Cleaner than Spacer
        ) {
            OutlinedButton(
                onClick = denyConnection,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Deny",
                    fontFamily = google_sans_font,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = acceptConnection,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.extra_light_blue).copy(0.75f)
                )
            ) {
                Text(
                    text = "Accept",
                    fontFamily = google_sans_font,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}