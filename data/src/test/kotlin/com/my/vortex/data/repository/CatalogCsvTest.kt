package com.my.vortex.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogCsvTest {
    @Test fun parsesQuotedCommasQuotesAndNewlines() {
        val rows = parseCsv("id,title\r\na,\"Game, One\"\r\nb,\"A \"\"quoted\"\"\nname\"\r\n")
        assertEquals(listOf("id", "title"), rows[0])
        assertEquals(listOf("a", "Game, One"), rows[1])
        assertEquals(listOf("b", "A \"quoted\"\nname"), rows[2])
    }
}
