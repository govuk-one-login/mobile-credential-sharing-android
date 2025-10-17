package uk.gov.onelogin.sharing.plugins.lint

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import uk.gov.onelogin.sharing.plugins.language.LanguageVersionExtension

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val languageVersions = extensions.getByType<LanguageVersionExtension>()

listOf(
    "detekt",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)

val detektPlugins by project.configurations

dependencies {
    detektPlugins(
        libs.findLibrary("compose-rules-detekt").get()
    )
}

configure<DetektExtension> {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("${rootProject.projectDir}/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        md.required.set(true) // simple Markdown format
    }
    jvmTarget = languageVersions.javaMajorVersion.get()
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = languageVersions.javaMajorVersion.get()
}