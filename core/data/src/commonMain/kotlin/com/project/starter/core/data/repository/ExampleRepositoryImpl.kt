package com.project.starter.core.data.repository

import com.project.starter.common.base.BaseRepository
import com.project.starter.common.utils.DispatcherProvider
import com.project.starter.core.data.network.ExampleApiService
import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.DataMapper.toDomain
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow

class ExampleRepositoryImpl(
    private val apiService: ExampleApiService,
    private val dispatcherProvider: DispatcherProvider,
) : BaseRepository(),
    ExampleRepository {
    override fun getExamples(): Flow<Result<List<ExampleModel>>> =
        safeCall(
            dispatcher = dispatcherProvider.io,
        ) {
            apiService.fetchExamples().map { it.toDomain() }
        }
}
