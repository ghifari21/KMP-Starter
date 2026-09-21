package com.project.starter.core.data.local

interface PlatformPathProvider {
    fun getDatabasePath(name: String): String

    fun getDataStorePath(name: String): String
}
