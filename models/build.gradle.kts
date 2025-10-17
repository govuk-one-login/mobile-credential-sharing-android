plugins {
    listOf(
        libs.plugins.templates.kotlin.library
    ).forEach { alias(it) }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencies {
    listOf(
        libs.junit
    ).forEach(::testImplementation)
}
