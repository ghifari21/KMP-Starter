package com.project.starter.core.domain.repository

interface SessionManager {
    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    suspend fun clearSession()
}
