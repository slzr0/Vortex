package com.my.vortex

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthStoreTest {
    private val store = AuthStore(ApplicationProvider.getApplicationContext())

    @After
    fun clearSession() = runBlocking { store.clear() }

    @Test
    fun callbackPersistsAndLogoutClearsSession() = runBlocking {
        val uri = Uri.parse("vortex://callback?token=access123&id=123456789&username=hello%20world&avatar=a_abc123")
        val callback = OAuthCallback.fromIntent(Intent(Intent.ACTION_VIEW, uri))
        assertNotNull(callback)
        store.save(callback!!)
        val signedIn = store.session.first() as AuthState.SignedIn
        assertEquals("123456789", signedIn.user.id)
        assertEquals("hello world", signedIn.user.username)
        assertEquals("a_abc123", signedIn.user.avatar)
        store.clear()
        assertTrue(store.session.first() is AuthState.SignedOut)
    }

    @Test
    fun absentAvatarIsAllowed() {
        val callback = OAuthCallback.fromUri(Uri.parse("vortex://callback?token=x&id=123&username=user&avatar=null"))
        assertNotNull(callback)
        assertNull(callback!!.avatar)
    }

    @Test
    fun rejectsMalformedOrAmbiguousCallbacks() {
        val invalid = listOf(
            "https://callback?token=x&id=123&username=user",
            "vortex://callback/extra?token=x&id=123&username=user",
            "vortex://callback?token=x&token=y&id=123&username=user",
            "vortex://callback?token=x&id=not-a-snowflake&username=user",
            "vortex://callback?token=x&id=123&username=user&avatar=../x",
            "vortex://callback?token=x&id=123"
        )
        invalid.forEach { assertNull(OAuthCallback.fromUri(Uri.parse(it))) }
        assertNull(OAuthCallback.fromIntent(Intent(Intent.ACTION_MAIN, Uri.parse("vortex://callback?token=x&id=123&username=user"))))
    }
}
