import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`

    // list of 3rd party plugins required by the custom plugins
    listOf(
        libs.plugins.kotlin.android,
        libs.plugins.roborazzi,
    ).forEach { dependency ->
        alias(dependency) apply false
    }

    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
}

dependencies {
    listOf(
        libs.detekt.gradle,
        libs.junit,
        libs.org.robolectric,
        libs.sonarqube.gradle,
        libs.spotless.gradle,
    ).forEach(::api)

    listOf(
        libs.com.android.tools.build.gradle,
        libs.com.google.devtools.ksp.gradle,
        libs.org.jetbrains.kotlin.gradle,
        libs.roborazzi.gradle,
        libs.uk.gov.pipelines.plugins,
    ).forEach(::implementation)
}

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
