package com.project.starter.feat.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.starter.common.utils.collectMvi

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToDetail: (id: String, title: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.collectMvi { effect ->
        when (effect) {
            is HomeEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            is HomeEffect.NavigateToDetail -> navigateToDetail(effect.id, effect.title)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setEvent(HomeEvent.LoadItems)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(state.data.items) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text(item.description) },
                        modifier =
                            Modifier.clickable {
                                viewModel.setEvent(HomeEvent.OnItemClicked(item))
                            },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
