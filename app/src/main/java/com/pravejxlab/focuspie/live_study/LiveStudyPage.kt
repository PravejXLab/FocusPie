package com.pravejxlab.focuspie.live_study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pravejxlab.focuspie.R
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pravejxlab.focuspie.home.google_sans_font
import com.pravejxlab.focuspie.join_table.StudentInfo

@Preview(showBackground = true)
@Composable
fun LiveStudyPage(
    innerPadding: PaddingValues = PaddingValues(),
    timeLeft: Long = 36_00_000L,
    studyingStudents: List<StudentInfo> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        TimeLeftSegment(timeLeft)
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            items(studyingStudents) { student ->
                StudyingStudentSegment(student)
            }
        }
    }
}

@Composable
fun TimeLeftSegment(timeLeft: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.extra_light_blue)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeLeft.toMMSSTime(),
            fontFamily = google_sans_font,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(48.dp)
        )
    }
}

@Composable
fun StudyingStudentSegment(studentInfo: StudentInfo) {
    Column(
        modifier = Modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(
                    color = colorResource(R.color.light_green),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "00:00:00",
                color = Color.White
            )
        }
        Spacer(Modifier.height(4.dp))

        Text(
            text = studentInfo.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}