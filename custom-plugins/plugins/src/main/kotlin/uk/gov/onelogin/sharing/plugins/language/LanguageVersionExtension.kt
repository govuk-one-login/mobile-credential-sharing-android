package uk.gov.onelogin.sharing.plugins.language

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * Gradle configuration blob for storing version information for the programming languages used
 * within the project.
 *
 * Whilst the primary use exists within `language-configuration.gradle.kts`, other custom plugins
 * also request this extension for defining values such as `jvmTarget`.
 *
 * Configuration example:
 * ```kotlin
 * languageVersions {
 *     // Convention values as per the `languageVersions()` companion function:
 *     javaVersion.set(JavaVersion.VERSION_21)
 *     kotlinVersion.set(KotlinVersion.KOTLIN_2_2)
 * }
 * ```
 */
interface LanguageVersionExtension {
    /**
     * The version of Java to use.
     */
    val javaVersion: Property<JavaVersion>

    /**
     * The [String] representation of the [javaVersion] property.
     */
    val javaMajorVersion: Provider<String> get() = javaVersion.map(JavaVersion::getMajorVersion)

    /**
     * The [Int] representation of the [javaVersion] property.
     */
    val javaVersionNumber: Provider<Int> get() = javaVersion.map { it.majorVersion.toInt() }

    /**
     * The version of Kotlin to use.
     */
    val kotlinVersion: Property<KotlinVersion>

    companion object {
        /**
         * A [Project] extension function that binds a [LanguageVersionExtension] instance to the
         * project for use within the `language-configuration-root.gradle.kts` plugin.
         */
        internal fun Project.languageVersions(): LanguageVersionExtension =
            extensions.create(
                "languageVersions",
                LanguageVersionExtension::class.java
            ).apply {
                this.javaVersion.convention(JavaVersion.VERSION_21)
                this.kotlinVersion.convention(KotlinVersion.KOTLIN_2_2)
            }
    }
}