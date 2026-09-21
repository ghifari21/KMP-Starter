package com.project.starter.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.project.starter.core.domain.repository.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class EncryptedSessionManager(
    private val dataStore: DataStore<Preferences>,
) : SessionManager {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    override suspend fun getToken(): String? =
        dataStore.data
            .map { preferences ->
                preferences[TOKEN_KEY]
            }.first()

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}
