package com.project.starter.core.data.repository

import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ExampleRepositoryImpl : ExampleRepository {
    override fun getExamples(): Flow<List<ExampleModel>> {
        return flowOf(
            listOf(
                ExampleModel("1", "KMP", "Kotlin Multiplatform"),
                ExampleModel("2", "CMP", "Compose Multiplatform")
            )
        )
    }
}
