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
import com.my.vortex.isDiscordAppUrl
import com.my.vortex.isDiscordWebUrl
import org.json.JSONTokener

private const val TOKEN_SCRIPT = """(function(){try{var f=document.createElement('iframe');document.body.appendChild(f);var t=f.contentWindow.localStorage.getItem('token');f.remove();return t ? JSON.parse(t) : null;}catch(e){return null;}})()"""
private const val SAMSUNG_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DiscordLoginWebView(onLoginCompleted: (String) -> Unit, onProgress: (Int) -> Unit, onError: () -> Unit) {
    val context = LocalContext.current
    val completed = rememberUpdatedState(onLoginCompleted)
    val progress = rememberUpdatedState(onProgress)
    val error = rememberUpdatedState(onError)
    val view = remember(context) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            if (Build.MANUFACTURER.equals("motorola", ignoreCase = true)) settings.userAgentString = SAMSUNG_USER_AGENT
        }
    }
    DisposableEffect(view) {
        val handler = Handler(Looper.getMainLooper())
        var finished = false
        var evaluating = false
        val poll = object : Runnable {
            override fun run() {
                if (finished) return
                if (!evaluating && isDiscordAppUrl(view.url.orEmpty())) {
                    evaluating = true
                    view.evaluateJavascript(TOKEN_SCRIPT) { result ->
                        evaluating = false
                        val token = try { JSONTokener(result ?: "null").nextValue() as? String } catch (_: Exception) { null }
                        if (!finished && isDiscordAppUrl(view.url.orEmpty()) && !token.isNullOrBlank()) {
                            finished = true
                            handler.removeCallbacks(this)
                            completed.value(token)
                        }
                    }
                }
                if (!finished) handler.postDelayed(this, 1500)
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
