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
    "test-coverage",
    "maven-publishing",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)