package com.project.starter.core.data.network

import com.project.starter.core.model.response.ExampleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ExampleApiService(
    private val httpClient: HttpClient,
) {
    suspend fun fetchExamples(): List<ExampleResponse> {
        // Now using actual HTTP call
        return httpClient.get("examples").body()
    }
}
