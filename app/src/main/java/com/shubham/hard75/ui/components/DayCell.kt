package com.shubham.hard75.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.data.db.entities.DayStatus

@Composable
fun DayCell(day: ChallengeDay) {
    val backgroundColor = when (day.status) {
        DayStatus.LOCKED -> Color.LightGray
        DayStatus.FAILED -> Color(0xFFD32F2F) // Red
        DayStatus.IN_PROGRESS -> Color(0xFFFFC107) // Yellow
        DayStatus.COMPLETED -> Color(0xFF4CAF50) // Green
    }

    val textColor = when (day.status) {
        DayStatus.LOCKED -> Color.DarkGray
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f), // Make it a square
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = day.dayNumber.toString(),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
