package com.project.starter.core.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosPlatformPathProvider : PlatformPathProvider {
    @OptIn(ExperimentalForeignApi::class)
    private val documentDirectory: String
        get() {
            val documentDirectory =
                NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
            return requireNotNull(documentDirectory?.path)
        }

    override fun getDatabasePath(name: String): String = "$documentDirectory/$name"

    override fun getDataStorePath(name: String): String = "$documentDirectory/$name.preferences_pb"
}
