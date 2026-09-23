package com.project.starter.core.domain.usecase

import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow

class GetExamplesUseCase(
    private val repository: ExampleRepository,
) {
    operator fun invoke(): Flow<Result<List<ExampleModel>>> = repository.getExamples()
}
