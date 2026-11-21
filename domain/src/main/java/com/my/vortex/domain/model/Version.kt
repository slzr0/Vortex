
package com.my.vortex.domain.model

import com.my.vortex.domain.model.release.Release

class Version(numbers: List<String>) {

    private var major: Int = 0
    private var minor: Int = 0

    init {
        major = numbers.getOrNull(0)?.toIntOrNull() ?: 0
        minor = numbers.getOrNull(1)?.toIntOrNull() ?: 0
    }

    constructor() : this(listOf())
    constructor(string: String?) : this(string?.trim()?.removePrefix("v")?.removePrefix("V")?.split(".") ?: listOf())

    override fun toString() = "$major.$minor"

    operator fun compareTo(other: Version): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    fun whetherNeedUpdate(current: Version): Boolean = this > current
}

fun String?.toVersion(): Version = Version(this)

fun Release.toVersion(): Version = Version(tagName)