package uk.gov.onelogin.sharing.plugins

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "java-library",
    "java-test-fixtures",
    "jacoco",
).forEach(pluginManager::apply)

listOf(
    "kotlin-jvm",
    "custom-language.config",
    "spotless-config",
    "detekt-config",
    "sonar-module-config",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)

listOf(
    "uk.gov.publishing.config"
).forEach(pluginManager::apply)