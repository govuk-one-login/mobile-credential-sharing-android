
// Used within submodules instead of defining the API level in every submodule.
val androidCompileSdk: Int by extra(36)
val androidMinSdk: Int by extra(29)
val androidTargetSdk: Int by extra(androidCompileSdk)
val javaVersion: JavaVersion by extra(JavaVersion.VERSION_21)

/**
 * Prefix used within the namespaces of gradle submodules.
 */
val namespacePrefix: String by extra("uk.gov.onelogin.sharing")

plugins {
    listOf(
        libs.plugins.android.application,
        libs.plugins.android.library,
        libs.plugins.kotlin.jvm,
        libs.plugins.kotlin.android,
        libs.plugins.kotlin.compose,
    ).forEach { plugin ->
        alias(plugin) apply false
    }
}