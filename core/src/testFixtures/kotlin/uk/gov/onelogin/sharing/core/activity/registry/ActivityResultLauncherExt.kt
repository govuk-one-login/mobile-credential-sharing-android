package uk.gov.onelogin.sharing.core.activity.registry

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

object ActivityResultLauncherExt {
    @Composable
    fun ProvideActivityResultRegistry(
        activityResultRegistry: ActivityResultRegistry,
        content: @Composable () -> Unit
    ) {
        val activityResultRegistryOwner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = activityResultRegistry
        }
        CompositionLocalProvider(
            LocalActivityResultRegistryOwner provides activityResultRegistryOwner
        ) { content() }
    }
}
