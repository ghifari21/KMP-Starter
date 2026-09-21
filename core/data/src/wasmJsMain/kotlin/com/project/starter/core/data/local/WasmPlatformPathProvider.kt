package com.project.starter.core.data.local

class WasmPlatformPathProvider : PlatformPathProvider {
    override fun getDatabasePath(name: String): String {
        return name
    }

    override fun getDataStorePath(name: String): String {
        return name
    }
}
