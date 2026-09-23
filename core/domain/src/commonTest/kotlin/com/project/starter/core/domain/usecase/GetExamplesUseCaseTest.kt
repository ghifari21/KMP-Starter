package com.project.starter.core.domain.usecase

import com.project.starter.core.testing.fakes.FakeExampleRepository
import com.project.starter.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetExamplesUseCaseTest {
    private val dispatcherRule = MainDispatcherRule()
    private val repository = FakeExampleRepository()
    private val useCase =
        GetExamplesUseCase(
            repository = repository,
        )

    @Test
    fun `invoke should return success result with items when repository succeeds`() =
        runTest {
            // Act
            val result = useCase().first()

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.size)
            assertEquals("Test1", result.getOrNull()?.get(0)?.title)
        }

    @Test
    fun `invoke should return failure result when repository fails`() =
        runTest {
            // Arrange
            repository.shouldReturnError = true

            // Act
            val result = useCase().first()

            // Assert
            assertTrue(result.isFailure)
            assertEquals("Mock error", result.exceptionOrNull()?.message)
        }
}
