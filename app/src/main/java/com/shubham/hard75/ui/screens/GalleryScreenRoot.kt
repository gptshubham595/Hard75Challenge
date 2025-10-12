package com.shubham.hard75.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.shubham.hard75.data.db.entities.ChallengeDay
import com.shubham.hard75.data.db.entities.DayStatus
import com.shubham.hard75.ui.viewmodel.GalleryViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun GalleryScreenRoot(
    onNavigateBack: () -> Unit,
    viewModel: GalleryViewModel = koinViewModel()
) {
    val photosByAttempt by viewModel.photosByAttempt.collectAsState()
    GalleryScreen(photosByAttempt = photosByAttempt, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    photosByAttempt: Map<Int, List<ChallengeDay>>,
    onNavigateBack: () -> Unit
) {
    var selectedDay by remember { mutableStateOf<ChallengeDay?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("75 Day Gallery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (photosByAttempt.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't taken any photos yet!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                photosByAttempt.toSortedMap(compareByDescending { it }).forEach { (attemptNumber, days) ->
                    item {
                        AttemptSection(
                            attemptNumber = attemptNumber,
                            days = days,
                            onPhotoClick = { day -> selectedDay = day }
                        )
                    }
                }
            }
        }
    }

    // Full-screen photo detail view
    if (selectedDay != null) {
        PhotoDetailDialog(
            day = selectedDay!!,
            onDismiss = { selectedDay = null }
        )
    }
}

@Preview
@Composable
fun GalleryScreenPreview() {
    val sampleDays = listOf(
        ChallengeDay(1, 1, DayStatus.COMPLETED, 10, 5, selfieImageUrl = "https://example.com/image1.jpg", selfieNote = "Feeling great!", timestamp = System.currentTimeMillis()),
        ChallengeDay(1, 2, DayStatus.COMPLETED, 10, 5, selfieImageUrl = "https://example.com/image2.jpg", selfieNote = "Pushing through.", timestamp = System.currentTimeMillis() - 86400000),
        ChallengeDay(1, 3, DayStatus.IN_PROGRESS, 5, 5, selfieImageUrl = "https://example.com/image3.jpg", selfieNote = "Almost there.", timestamp = System.currentTimeMillis() - 172800000)
    )
    val photosByAttempt = mapOf(1 to sampleDays)
    GalleryScreen(photosByAttempt = photosByAttempt, onNavigateBack = {})
}

@Preview
@Composable
fun GalleryScreenEmptyPreview() {
    GalleryScreen(photosByAttempt = emptyMap(), onNavigateBack = {})
}

@Composable
private fun AttemptSection(
    attemptNumber: Int,
    days: List<ChallengeDay>,
    onPhotoClick: (ChallengeDay) -> Unit
) {
    val totalScore = days.sumOf { it.score }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attempt $attemptNumber",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "$totalScore pts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(((days.size + 1) / 2 * 220).dp)
    ) {
        items(days, key = { it.dayNumber }) { day ->
            PostalCardItem(day, onClick = { onPhotoClick(day) })
        }
    }
}

@Composable
fun PostalCardItem(day: ChallengeDay, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            AsyncImage(
                model = day.selfieImageUrl,
                contentDescription = "Selfie for Day ${day.dayNumber}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "Day ${day.dayNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                day.timestamp?.let {
                    Text(
                        text = formatDate(Date(it), "dd MMM yyyy"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                day.selfieNote?.let { note ->
                    Text(
                        text = "\"$note\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoDetailDialog(day: ChallengeDay, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            onClick = onDismiss
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = day.selfieImageUrl,
                    contentDescription = "Full screen selfie for Day ${day.dayNumber}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Day ${day.dayNumber}",
                    style = MaterialTheme.typography.headlineMedium
                )
                day.timestamp?.let {
                    Text(
                        text = formatDate(Date(it), "dd MMM yyyy, hh:mm a"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                day.selfieNote?.let { note ->
                    Text(
                        text = "\"$note\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PhotoDetailDialogPreview() {
    val day = ChallengeDay(
        attemptNumber = 1,
        dayNumber = 42,
        status = DayStatus.COMPLETED,
        totalTasks = 5,
        selfieImageUrl = "https://example.com/image.jpg",
        selfieNote = "This was a tough but rewarding day. The progress is visible and I'm feeling proud of my consistency.",
        timestamp = 1678886400000L // March 15, 2023
    )
    PhotoDetailDialog(day = day, onDismiss = {})
}

private fun formatDate(date: Date, format: String): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(date)
}