package com.project.starter.core.testing.fakes

import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeExampleRepository : ExampleRepository {
    var shouldReturnError = false
    var mockData =
        listOf(
            ExampleModel("1", "Test1", "Description 1"),
            ExampleModel("2", "Test2", "Description 2"),
        )

    override fun getExamples(): Flow<Result<List<ExampleModel>>> =
        if (shouldReturnError) {
            flowOf(Result.failure(Exception("Mock error")))
        } else {
            flowOf(Result.success(mockData))
        }
}
