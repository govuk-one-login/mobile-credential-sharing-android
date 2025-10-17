package uk.gov.onelogin.sharing.plugins.testing

import com.android.build.api.dsl.TestedExtension
import com.android.build.gradle.BaseExtension

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

/**
 * Configure the android plugin based on configuring
 * [Robolectric](https://robolectric.org/getting-started/#building-with-gradle-kotlin).
 */
configure<BaseExtension> {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

configure<TestedExtension> {
    testFixtures {
        enable = true
    }
}
