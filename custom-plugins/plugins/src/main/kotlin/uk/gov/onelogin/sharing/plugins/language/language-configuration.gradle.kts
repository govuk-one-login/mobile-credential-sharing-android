package uk.gov.onelogin.sharing.plugins.language

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import uk.gov.onelogin.sharing.plugins.language.LanguageVersionExtension.Companion.languageVersions

val languageVersions: LanguageVersionExtension = languageVersions()

if (pluginManager.hasPlugin("com.android.application")) {
    configure<ApplicationExtension> {
        configureCompileOptions()
    }
    configure<KotlinAndroidExtension> {
        jvmToolchain(
            languageVersions.javaVersionNumber.get()
        )
        compilerOptions {
            configureKotlinCompiler()
        }
    }
} else if (pluginManager.hasPlugin("com.android.library")) {
    configure<LibraryExtension> {
        configureCompileOptions()
    }
    configure<KotlinAndroidExtension> {
        jvmToolchain(
            languageVersions.javaVersionNumber.get()
        )
        compilerOptions {
            configureKotlinCompiler()
        }
    }
} else if (pluginManager.hasPlugin("java-library")) {
    configure<KotlinJvmProjectExtension> {
        jvmToolchain(
            languageVersions.javaVersionNumber.get()
        )
        compilerOptions {
            configureKotlinCompiler()
        }
    }
}

fun KotlinJvmCompilerOptions.configureKotlinCompiler() {
    apiVersion.set(languageVersions.kotlinVersion)
    allWarningsAsErrors.assign(
        true
    )
}

fun CommonExtension<*, *, *, *, *, *>.configureCompileOptions() {
    compileOptions {
        sourceCompatibility =
            languageVersions.javaVersion.get()
        targetCompatibility =
            languageVersions.javaVersion.get()
    }
}
