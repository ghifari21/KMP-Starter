package com.project.starter.feat.home.presentation

import app.cash.turbine.test
import com.project.starter.core.domain.usecase.GetExamplesUseCase
import com.project.starter.core.testing.fakes.FakeExampleRepository
import com.project.starter.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeViewModelTest {
    private val dispatcherRule = MainDispatcherRule()
    private val repository = FakeExampleRepository()
    private val useCase =
        GetExamplesUseCase(
            repository = repository,
        )

    private fun createViewModel() = HomeViewModel(useCase)

    @Test
    fun `initial state is empty and not loading`() =
        runTest {
            val viewModel = createViewModel()
            assertEquals(emptyList<String>(), viewModel.uiState.value.data.items)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun `LoadItems event updates state correctly on success`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                // Initial state
                awaitItem()

                viewModel.setEvent(HomeEvent.LoadItems)

                // Loading state
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                // Success state
                val successState = awaitItem()
                assertEquals(false, successState.isLoading)
                assertEquals(listOf("Test1", "Test2"), successState.data.items)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
