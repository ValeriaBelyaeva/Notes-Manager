package com.example.notes_manager.core.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

data class TokenPair(val access: String, val refresh: String)

class TokenStorage(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _tokens = MutableStateFlow(load())

    val tokens: StateFlow<TokenPair?> = _tokens
    val isAuthorized = tokens.map { it?.access?.isNotBlank() == true }

    private fun load(): TokenPair? {
        val a = prefs.getString("access", null)
        val r = prefs.getString("refresh", null)
        return if (!a.isNullOrBlank() && !r.isNullOrBlank()) TokenPair(a, r) else null
    }

    fun currentAccess(): String? = _tokens.value?.access
    fun currentRefresh(): String? = _tokens.value?.refresh

    fun setTokens(access: String, refresh: String) {
        prefs.edit().putString("access", access).putString("refresh", refresh).apply()
        _tokens.value = TokenPair(access, refresh)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _tokens.value = null
    }
}
