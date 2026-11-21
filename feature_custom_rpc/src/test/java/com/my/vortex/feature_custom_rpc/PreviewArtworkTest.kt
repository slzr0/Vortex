package com.my.vortex.feature_custom_rpc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreviewArtworkTest {
    @Test fun acceptsOnlyDirectHttpImages() {
        assertEquals("https://example.com/image.png", previewArtworkUrl(" https://example.com/image.png "))
        assertEquals("http://example.com/image.png", previewArtworkUrl("http://example.com/image.png"))
        assertNull(previewArtworkUrl("mp:external/asset"))
        assertNull(previewArtworkUrl("file:///data/private"))
        assertNull(previewArtworkUrl("https://name:password@example.com/image.png"))
        assertNull(previewArtworkUrl("https://"))
        assertNull(previewArtworkUrl(""))
    }
}
