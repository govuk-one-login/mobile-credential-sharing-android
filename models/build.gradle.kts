plugins {
    listOf(
        "java-library",
        "java-test-fixtures",
        "jacoco",
    ).forEach(::id)
    listOf(
        libs.plugins.kotlin.jvm,
    ).forEach { alias(it) }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencies {
    listOf(
        libs.junit,
    ).forEach(::testImplementation)
}