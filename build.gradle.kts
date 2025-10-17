import java.io.ByteArrayOutputStream

// Used within submodules instead of defining the API level in every submodule.
val androidCompileSdk: Int by extra(36)
val androidMinSdk: Int by extra(29)
val androidTargetSdk: Int by extra(androidCompileSdk)

/**
 * The number of commits within the current git branch.
 *
 * Used for declaring the version code of an android app.
 */
val androidVersionCode: Int by extra {
    providers.exec {
        executable(
            layout.projectDirectory.file("scripts/getCurrentVersionCode")
        )
    }.standardOutput.asText.map {
        it.trim().toInt()
    }.getOrElse(1)
}

/**
 * The latest git tag that exists within the repository.
 *
 * Used for declaring the version name of an android app, as well as library versions during
 * publication.
 */
val androidVersionName: String by extra {
    providers.exec {
        executable(
            layout.projectDirectory.file("scripts/getCurrentVersionName")
        )
    }.standardOutput.asText.map(String::trim)
        .getOrElse("0.0.1")
}
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

/**
 * Gradle task for testing the [androidVersionCode] property value
 */
val printVersionCode by tasks.registering {
    logger.warn("Android version code: $androidVersionCode")
}

/**
 * Gradle task for testing the [androidVersionName] property value.
 */
val printVersionName by tasks.registering {
    logger.warn("Android version name: $androidVersionName")
}
