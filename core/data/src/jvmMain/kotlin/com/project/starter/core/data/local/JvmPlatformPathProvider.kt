package com.project.starter.core.data.local

import java.io.File

class JvmPlatformPathProvider : PlatformPathProvider {
    private val appDataDir: String
        get() {
            val userHome = System.getProperty("user.home")
            val appDir = File(userHome, ".kmpstarter")
            if (!appDir.exists()) appDir.mkdirs()
            return appDir.absolutePath
        }

    override fun getDatabasePath(name: String): String {
        return File(appDataDir, name).absolutePath
    }

    override fun getDataStorePath(name: String): String {
        return File(appDataDir, "$name.preferences_pb").absolutePath
    }
}
