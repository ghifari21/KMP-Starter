package com.project.starter.core.domain.repository

import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow

interface ExampleRepository {
    fun getExamples(): Flow<Result<List<ExampleModel>>>
}
