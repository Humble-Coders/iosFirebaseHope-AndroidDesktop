package org.example.iosfirebasehope

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform