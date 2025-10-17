package uk.gov.onelogin.sharing.plugins

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "kotlin-jvm",
    "custom-language.config",
    "spotless-config",
    "detekt-config",
    "test-coverage",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)