package uk.gov.onelogin.sharing.plugins

import org.gradle.api.plugins.PluginManager

/**
 * Wrapper object for containing convenience functions relating to the [PluginManager]
 */
object PluginManagerExtensions {
    fun PluginManager.isAndroidApp(): Boolean = hasPlugin("com.android.application")
    fun PluginManager.isAndroidLibrary(): Boolean = hasPlugin("com.android.library")
    fun PluginManager.isJavaLibrary(): Boolean = hasPlugin("java-library")
}