package com.dorachat.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

internal object TokenStore {

    private const val PREFERENCE_NAME = "secure_token"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFERENCE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun save(access: String, refresh: String? = null) {
        prefs.edit {
            putString(KEY_ACCESS, access)
            refresh?.let { putString(KEY_REFRESH, it) }
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
