/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * DiscordLoginWebView.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_profile.ui.component

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.my.vortex.isPlausibleSessionToken
import com.my.vortex.isDiscordWebUrl
import org.json.JSONTokener

internal const val TOKEN_SCRIPT = """(function(){
    if (location.protocol !== 'https:' || (location.hostname !== 'discord.com' && location.hostname !== 'www.discord.com')) return null;
    function read(storage) {
        try {
            var value = storage.getItem('token');
            if (!value) return null;
            try { var decoded = JSON.parse(value); if (typeof decoded !== 'string') return null; value = decoded; } catch (_) {}
            return typeof value === 'string' && value.length >= 20 && value.length <= 4096 ? value : null;
        } catch (_) { return null; }
    }
    var frame = null;
    try {
        frame = document.createElement('iframe');
        frame.style.display = 'none';
        (document.body || document.documentElement).appendChild(frame);
        var value = read(frame.contentWindow.localStorage);
        if (value) return value;
    } catch (_) {} finally { if (frame) frame.remove(); }
    try { return read(window.localStorage); } catch (_) { return null; }
})()"""
private const val SAMSUNG_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DiscordLoginWebView(
    onLoginCompleted: (String) -> Unit, onProgress: (Int) -> Unit, onError: () -> Unit,
    checkRequest: Int, onCheckEmpty: () -> Unit
) {
    val context = LocalContext.current
    val completed = rememberUpdatedState(onLoginCompleted)
    val progress = rememberUpdatedState(onProgress)
    val error = rememberUpdatedState(onError)
    val requestedCheck = rememberUpdatedState(checkRequest)
    val checkEmpty = rememberUpdatedState(onCheckEmpty)
    val view = remember(context) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            if (Build.MANUFACTURER.equals("motorola", ignoreCase = true)) settings.userAgentString = SAMSUNG_USER_AGENT
        }
    }
    DisposableEffect(view) {
        val handler = Handler(Looper.getMainLooper())
        var finished = false
        var evaluating = false
        var lastCheck = checkRequest
        val poll = object : Runnable {
            override fun run() {
                if (finished) return
                val manual = requestedCheck.value != lastCheck
                if (!evaluating && isDiscordWebUrl(view.url.orEmpty())) {
                    evaluating = true
                    if (manual) lastCheck = requestedCheck.value
                    view.evaluateJavascript(TOKEN_SCRIPT) { result ->
                        evaluating = false
                        val token = try { JSONTokener(result ?: "null").nextValue() as? String } catch (_: Exception) { null }
                        if (!finished && isDiscordWebUrl(view.url.orEmpty()) && token != null && isPlausibleSessionToken(token)) {
                            finished = true
                            handler.removeCallbacks(this)
                            completed.value(token)
                        } else if (!finished && manual) checkEmpty.value()
                    }
                } else if (!evaluating && manual) {
                    lastCheck = requestedCheck.value
                    checkEmpty.value()
                }
                if (!finished) handler.postDelayed(this, 1000)
            }
        }
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame || isDiscordWebUrl(request.url.toString())) return false
                if (request.url.scheme == "https") runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, request.url)) }
                return true
            }
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                if (url != null && !isDiscordWebUrl(url)) view.stopLoading()
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, err: WebResourceError) {
                if (request.isForMainFrame) error.value()
            }
        }
        view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) { progress.value(newProgress) }
        }
        view.loadUrl("https://discord.com/login")
        handler.post(poll)
        onDispose {
            finished = true
            handler.removeCallbacksAndMessages(null)
            view.stopLoading()
            (view.parent as? ViewGroup)?.removeView(view)
            view.destroy()
        }
    }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
}
