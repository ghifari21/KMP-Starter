package com.project.starter.core.data.repository

import com.project.starter.core.data.network.ExampleApiService
import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

class ExampleRepositoryImpl(
    private val apiService: ExampleApiService,
) : ExampleRepository {
    private val store =
        StoreBuilder
            .from(
                fetcher =
                    Fetcher.of { key: String ->
                        // In a real app, this would use the apiService to fetch data
                        // e.g., apiService.getExampleData(key)
                        listOf(
                            ExampleModel("1", "KMP", "Kotlin Multiplatform"),
                            ExampleModel("2", "CMP", "Compose Multiplatform"),
                            ExampleModel("3", "Store5", "Mobile Native Foundation"),
                        )
                    },
            ).build()

    override fun getExamples(): Flow<List<ExampleModel>> =
        store
            .stream(StoreReadRequest.cached("examples", refresh = true))
            .map { response ->
                when (response) {
                    is StoreReadResponse.Data -> response.value
                    else -> emptyList() // Handle loading/error states in real app
                }
            }
}
