package com.project.starter.core.testing.fakes

import com.project.starter.core.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionManager : SessionManager {
    private val tokenFlow = MutableStateFlow<String?>(null)

    override suspend fun saveToken(token: String) {
        tokenFlow.value = token
    }

    override suspend fun getToken(): String? = tokenFlow.value

    override suspend fun clearSession() {
        tokenFlow.value = null
    }

    override fun isLoggedIn(): Flow<Boolean> = tokenFlow.map { it != null }
}
