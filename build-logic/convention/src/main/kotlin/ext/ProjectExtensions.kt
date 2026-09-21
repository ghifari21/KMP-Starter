package ext

import org.gradle.api.plugins.PluginManager

fun PluginManager.id(id: String) {
    apply(id)
}
