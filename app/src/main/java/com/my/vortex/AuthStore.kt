package com.my.vortex

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.my.vortex.domain.model.user.User
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "vortex_auth")

internal class AuthStore(context: Context) {
    private val store = context.applicationContext.authDataStore

    val session: Flow<AuthState> = store.data.map { prefs ->
        val token = prefs[TOKEN]
        val id = prefs[USER_ID]
        val username = prefs[USERNAME]
        if (token.isNullOrBlank() || id.isNullOrBlank() || username.isNullOrBlank()) {
            AuthState.SignedOut
        } else {
            AuthState.SignedIn(User(id = id, username = username, avatar = prefs[AVATAR]))
        }
    }.catch { exception ->
        if (exception is IOException) emit(AuthState.ReadError) else throw exception
    }

    suspend fun save(callback: OAuthCallback) {
        store.edit { prefs ->
            prefs[TOKEN] = callback.token
            prefs[USER_ID] = callback.id
            prefs[USERNAME] = callback.username
            if (callback.avatar == null) prefs.remove(AVATAR) else prefs[AVATAR] = callback.avatar
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("access_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val AVATAR = stringPreferencesKey("avatar")
    }
}

internal sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data object ReadError : AuthState
    data class SignedIn(val user: User) : AuthState
}

internal class OAuthCallback(val token: String, val id: String, val username: String, val avatar: String?) {
    companion object {
        fun fromIntent(intent: Intent?): OAuthCallback? {
            if (intent?.action != Intent.ACTION_VIEW) return null
            return fromUri(intent.data ?: return null)
        }

        fun fromUri(uri: Uri): OAuthCallback? {
            if (!uri.isHierarchical || uri.scheme != "vortex" || uri.host != "callback" ||
                !uri.path.isNullOrEmpty() || uri.fragment != null || uri.port != -1 || uri.userInfo != null
            ) return null
            return try {
                fun single(name: String): String? = uri.getQueryParameters(name).singleOrNull()
                val token = single("token")?.takeIf { it.isNotBlank() && it.length <= 4096 && it.none(Char::isWhitespace) } ?: return null
                val id = single("id")?.takeIf { it.length in 1..20 && it.all(Char::isDigit) } ?: return null
                val username = single("username")?.takeIf { it.isNotBlank() && it.length <= 256 && it.none(Char::isISOControl) } ?: return null
                val avatars = uri.getQueryParameters("avatar")
                if (avatars.size > 1) return null
                val avatar = avatars.singleOrNull()?.takeUnless { it.isBlank() || it == "null" }
                if (avatar != null && (avatar.length > 128 || !avatar.matches(Regex("[a-zA-Z0-9_]+")))) return null
                OAuthCallback(token, id, username, avatar)
            } catch (_: UnsupportedOperationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
