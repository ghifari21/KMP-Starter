package com.project.starter.core.domain.usecase

import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow

class GetExampleUseCase(private val repository: ExampleRepository) {
    operator fun invoke(): Flow<List<ExampleModel>> {
        return repository.getExamples()
    }
}
