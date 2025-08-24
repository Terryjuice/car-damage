package com.cardamageai.core.common.config

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "car_damage_ai_prefs", 
        Context.MODE_PRIVATE
    )
    
    fun setAnthropicApiKey(apiKey: String) {
        preferences.edit()
            .putString(KEY_ANTHROPIC_API, apiKey)
            .apply()
    }
    
    fun getAnthropicApiKey(): String? {
        return preferences.getString(KEY_ANTHROPIC_API, null)
    }
    
    fun hasAnthropicApiKey(): Boolean {
        return !getAnthropicApiKey().isNullOrEmpty()
    }
    
    fun clearApiKeys() {
        preferences.edit()
            .clear()
            .apply()
    }
    
    companion object {
        private const val KEY_ANTHROPIC_API = "anthropic_api_key"
    }
}