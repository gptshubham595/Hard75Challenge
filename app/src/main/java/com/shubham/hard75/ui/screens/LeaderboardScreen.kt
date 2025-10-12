package com.shubham.hard75.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shubham.hard75.model.LeaderboardEntry
import com.shubham.hard75.model.LeaderboardState
import com.shubham.hard75.ui.viewmodel.LeaderboardViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreenRoot(
    onNavigateBack: () -> Unit,
    viewModel: LeaderboardViewModel = koinViewModel()
) {
    val leaderboardState by viewModel.leaderboardState.collectAsState()
    LeaderboardScreen(
        onNavigateBack = onNavigateBack,
        leaderboardState = leaderboardState
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    leaderboardState: LeaderboardState
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                leaderboardState.isLoading -> {
                    CircularProgressIndicator()
                }

                leaderboardState.error != null -> {
                    Text(
                        text = "Error: ${leaderboardState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                leaderboardState.entries.isEmpty() -> {
                    Text(text = "No one has completed the challenge yet!")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(leaderboardState.entries) { entry ->
                            LeaderboardItem(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = entry.userName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Completed: ${entry.completedDate?.let { formatDate(it) } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = "${entry.totalScore} pts", style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(date)
}


@Preview(showBackground = true)
@Composable
fun LeaderboardScreenPreview() {
    val sampleEntries = listOf(
        LeaderboardEntry("user1", "User One", 7500),
        LeaderboardEntry("user2", "User Two", 7400),
        LeaderboardEntry("user3", "User Three", 7300),
    )
    LeaderboardScreen(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(entries = sampleEntries, isLoading = false)
    )
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenEmptyPreview() {
    LeaderboardScreen(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(entries = emptyList(), isLoading = false)
    )
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenLoadingPreview() {
    LeaderboardScreen(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(isLoading = true)
    )
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenErrorPreview() {
    LeaderboardScreen(
        onNavigateBack = {},
        leaderboardState = LeaderboardState(error = "Failed to load data")
    )
}
