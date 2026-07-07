package uk.gov.onelogin.sharing.plugins.testing

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestedExtension
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidApp
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidLibrary

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "roborazzi",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)

val testImplementation by project.configurations

/*
 *
 */
dependencies {
    listOf(
        "screenshot-testing"
    ).map { versionCatalogId ->
        libs.findBundle(versionCatalogId).get()
    }.forEach { dependency ->
        testImplementation(dependency)
    }
}

if (pluginManager.isAndroidApp()) {
    configure<ApplicationExtension> {
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }
} else if (pluginManager.isAndroidLibrary()) {
    configure<LibraryExtension> {
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }
}

configure<TestedExtension> {
    testFixtures {
        enable = true
    }
}
