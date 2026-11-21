package com.my.vortex.feature_custom_rpc

import java.net.URI

internal fun previewArtworkUrl(value: String): String? {
    val candidate = value.trim()
    return runCatching {
        val uri = URI(candidate)
        candidate.takeIf {
            (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) &&
                !uri.host.isNullOrBlank() && uri.userInfo == null
        }
    }.getOrNull()
}
