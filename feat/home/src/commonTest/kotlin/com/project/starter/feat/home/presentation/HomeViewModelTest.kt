package com.project.starter.feat.home.presentation

import app.cash.turbine.test
import com.project.starter.core.domain.usecase.GetExamplesUseCase
import com.project.starter.core.model.ExampleModel
import com.project.starter.core.testing.fakes.FakeExampleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
            assertEquals(emptyList<ExampleModel>(), viewModel.uiState.value.data.items)
            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `LoadItems event updates state with ExampleModel list on success`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // Initial state

                viewModel.setEvent(HomeEvent.LoadItems)

                // With UnconfinedTestDispatcher, the coroutine executes eagerly and StateFlow
                // conflates the intermediate loading=true and error=null states,
                // emitting only the final success state.
                val successState = awaitItem()
                assertFalse(successState.isLoading)
                assertEquals(2, successState.data.items.size)
                assertEquals("Test1", successState.data.items[0].name)
                assertEquals("Test2", successState.data.items[1].name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `LoadItems event emits ShowToast effect when repository fails`() =
        runTest {
            repository.shouldReturnError = true
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.setEvent(HomeEvent.LoadItems)

                val effect = awaitItem()
                assertTrue(effect is HomeEffect.ShowToast)
                assertEquals("Mock error", (effect as HomeEffect.ShowToast).message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `OnItemClicked event emits NavigateToDetail effect`() =
        runTest {
            val item = ExampleModel("1", "Test1", "Desc 1")
            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.setEvent(HomeEvent.OnItemClicked(item))

                val effect = awaitItem()
                assertTrue(effect is HomeEffect.NavigateToDetail)
                assertEquals("1", (effect as HomeEffect.NavigateToDetail).id)
                assertEquals("Test1", effect.title)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
