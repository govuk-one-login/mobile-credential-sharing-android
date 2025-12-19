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

/* GOV.UK pipelines publishing configuration */
val githubRepositoryName: String by extra("mobile-credential-sharing-android")
val mavenGroupId: String by extra(namespacePrefix)
val buildLogicDir: String by extra("mobile-android-pipelines/buildLogic")
val configDir: String by extra( "${rootProject.rootDir}/config")

plugins {
    listOf(
        libs.plugins.android.application,
        libs.plugins.android.library,
        libs.plugins.kotlin.jvm,
        libs.plugins.kotlin.android,
        libs.plugins.kotlin.compose,
        libs.plugins.kotlin.ksp,
        libs.plugins.hilt.plugin,
        libs.plugins.kotlin.parcelize,
        libs.plugins.kotlin.serialization,
    ).forEach { plugin ->
        alias(plugin) apply false
    }

    listOf(
        libs.plugins.sonarqube
    ).forEach { plugin ->
        alias(plugin)
    }
}

subprojects {
    group = "uk.gov.onelogin.sharing"
    version = androidVersionName
}

/**
 * Gradle task for testing the [androidVersionCode] property value
 */
val printVersionCode by tasks.registering {
    group = "validation"
    description = "Outputs the generated android version code from the extra property"
    logger.lifecycle("Android version code: $androidVersionCode")
}

/**
 * Gradle task for testing the [androidVersionName] property value.
 */
val printVersionName by tasks.registering {
    group = "validation"
    description = "Outputs the generated android version name from the extra property"
    logger.lifecycle("Android version name: $androidVersionName")
}

sonarqube {
    this.setAndroidVariant("debug")

    val sonarRootProperties =
        mapOf(
            "sonar.projectKey" to "govuk-one-login_mobile-credential-sharing-android",
            "sonar.projectName" to "mobile-credential-sharing-android",
            "sonar.token" to System.getProperty("SONAR_TOKEN"),
            "sonar.projectVersion" to "v$androidVersionName-$androidVersionCode",
            "sonar.organization" to "govuk-one-login",
            "sonar.sourceEncoding" to "UTF-8",
            "sonar.sources" to "",
        )

    properties {
        sonarRootProperties.forEach { (key, value) ->
            property(key, value)
        }
    }
}