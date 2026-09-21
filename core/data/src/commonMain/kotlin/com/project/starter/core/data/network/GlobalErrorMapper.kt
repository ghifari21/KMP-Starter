package com.project.starter.core.data.network

import com.project.starter.common.exception.AppException
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator

fun HttpClientConfig<*>.installGlobalErrorMapper() {
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            // Map ktor exceptions to AppException
            throw AppException.NetworkException(exception.message ?: "Network error occurred")
        }
        validateResponse { response ->
            if (response.status.value !in 200..299) {
                throw AppException.ServerException("Server returned ${response.status.value}")
            }
        }
    }
}
