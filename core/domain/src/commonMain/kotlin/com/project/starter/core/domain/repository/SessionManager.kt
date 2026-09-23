package com.project.starter.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionManager {
    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    suspend fun clearSession()

    /** Emits `true` when a valid token exists, `false` otherwise. */
    fun isLoggedIn(): Flow<Boolean>
}
