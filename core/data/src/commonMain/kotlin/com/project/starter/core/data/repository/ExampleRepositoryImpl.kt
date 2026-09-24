package com.project.starter.core.data.repository

import com.project.starter.common.utils.DispatcherProvider
import com.project.starter.core.data.network.ExampleApiService
import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.DataMapper.toDomain
import com.project.starter.core.model.DataMapper.toEntity
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

private const val EXAMPLES_KEY = "examples"

class ExampleRepositoryImpl(
    private val apiService: ExampleApiService,
    private val dispatcherProvider: DispatcherProvider,
) : ExampleRepository {
    // In-memory cache backing Store5's SourceOfTruth
    private val memoryCache = mutableListOf<ExampleModel>()

    private val store =
        StoreBuilder
            .from(
                fetcher =
                    Fetcher.of { _: String ->
                        apiService.fetchExamples().map { it.toEntity().toDomain() }
                    },
                sourceOfTruth =
                    SourceOfTruth.of<String, List<ExampleModel>, List<ExampleModel>>(
                        reader = { _ ->
                            flow {
                                if (memoryCache.isNotEmpty()) emit(memoryCache.toList())
                            }
                        },
                        writer = { _, items ->
                            memoryCache.clear()
                            memoryCache.addAll(items)
                        },
                        delete = { _ -> memoryCache.clear() },
                        deleteAll = { memoryCache.clear() },
                    ),
            ).build()

    override fun getExamples(): Flow<Result<List<ExampleModel>>> =
        flow {
            store
                .stream(StoreReadRequest.cached(EXAMPLES_KEY, refresh = true))
                .collect { response ->
                    when (response) {
                        is StoreReadResponse.Data -> emit(Result.success(response.value))
                        is StoreReadResponse.Error.Exception ->
                            emit(Result.failure(response.error))
                        is StoreReadResponse.Error.Message ->
                            emit(Result.failure(Exception(response.message)))
                        else -> Unit // Loading, NoNewData — ignore
                    }
                }
        }.flowOn(dispatcherProvider.io)
}
