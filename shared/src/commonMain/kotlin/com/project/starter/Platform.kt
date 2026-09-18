package com.project.starter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform