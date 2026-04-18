package com.tit.nimonsapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

private const val SHARED_PREFS_NAME = "encrypted_session"

class SessionRepository(
    private val context: Context,
) {
    companion object {
        private const val TOKEN_KEY = "token"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            SHARED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _tokenFlow = MutableStateFlow<String?>(null)
    val tokenFlow: Flow<String?> = _tokenFlow.asStateFlow()

    init {
        // Initialize the flow with current token value
        _tokenFlow.value = getToken()
    }

    fun saveToken(token: String) {
        encryptedSharedPreferences.edit().putString(TOKEN_KEY, token).apply()
        _tokenFlow.value = token
    }

    fun getToken(): String? = encryptedSharedPreferences.getString(TOKEN_KEY, null)

    fun clearToken() {
        encryptedSharedPreferences.edit().remove(TOKEN_KEY).apply()
        _tokenFlow.value = null
    }

    fun hasToken(): Boolean = getToken() != null

    fun getBearerToken(): String? {
        val token = getToken() ?: return null
        return "Bearer $token"
    }
}
