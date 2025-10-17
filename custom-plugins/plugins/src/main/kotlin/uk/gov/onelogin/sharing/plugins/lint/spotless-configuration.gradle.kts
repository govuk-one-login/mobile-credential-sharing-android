package uk.gov.onelogin.sharing.plugins.lint

import com.diffplug.gradle.spotless.BaseKotlinExtension
import com.diffplug.gradle.spotless.SpotlessExtension

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "spotless",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)

configure<SpotlessExtension> {
    kotlin {
        target(
            "src/*/kotlin/**/*.kt",
            "src/*/java/**/*.kt",
        )
        verifierKtLintConfig()
    }
    kotlinGradle {
        target(
            "*.gradle.kts"
        )
        verifierKtLintConfig()
    }
}

fun BaseKotlinExtension.verifierKtLintConfig() {
    ktlint(libs.findVersion("ktlint").get().requiredVersion)
        .customRuleSets(
            listOf(
                libs.findLibrary("compose-rules-ktlint").get().get().toString(),
            )
        )
        .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")

        // duplicates the `.editorconfig` properties
        .editorConfigOverride(
            mapOf(
                "ktlint_code_style" to "android_studio",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            )
        )
}
