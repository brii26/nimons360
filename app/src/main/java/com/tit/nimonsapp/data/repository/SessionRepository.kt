package com.tit.nimonsapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SESSION_DATASTORE_NAME = "session"

private val Context.sessionDataStore by preferencesDataStore(
    name = SESSION_DATASTORE_NAME,
)

class SessionRepository(
    private val context: Context,
) {
    companion object {
        private val TOKEN_KEY: Preferences.Key<String> =
            stringPreferencesKey("token")
    }

    val tokenFlow: Flow<String?> =
        context.sessionDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[TOKEN_KEY]
            }

    suspend fun saveToken(token: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? = tokenFlow.first()

    suspend fun clearToken() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun hasToken(): Boolean = getToken() != null

    suspend fun getBearerToken(): String? {
        val token = getToken() ?: return null
        return "Bearer $token"
    }
}
