package com.project.starter.core.data.local

class WasmPlatformPathProvider : PlatformPathProvider {
    override fun getDatabasePath(name: String): String = name

    override fun getDataStorePath(name: String): String = name
}
