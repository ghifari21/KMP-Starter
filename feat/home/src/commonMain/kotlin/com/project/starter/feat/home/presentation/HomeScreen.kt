package com.project.starter.feat.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column {
        Button(onClick = { viewModel.setEvent(HomeEvent.LoadItems) }) {
            Text("Load Items")
        }
        if (state.isLoading) {
            Text("Loading...")
        } else {
            state.items.forEach { item ->
                Text(item)
            }
        }
    }
}
