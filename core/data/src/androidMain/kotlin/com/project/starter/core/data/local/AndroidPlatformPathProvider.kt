package com.project.starter.core.data.local

import android.content.Context
import java.io.File

class AndroidPlatformPathProvider(
    private val context: Context,
) : PlatformPathProvider {
    override fun getDatabasePath(name: String): String = context.getDatabasePath(name).absolutePath

    override fun getDataStorePath(name: String): String = File(context.filesDir, "datastore/$name.preferences_pb").absolutePath
}
