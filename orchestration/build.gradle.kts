plugins {
    listOf(
        libs.plugins.templates.kotlin.library
    ).forEach { alias(it) }
}

dependencies {
    listOf(
        libs.kotlinx.coroutines.core,
    ).forEach(::api)

    listOf(
        libs.com.google.test.parameter.injector,
        libs.junit,
    ).forEach(::testFixturesApi)

    listOf(
        libs.app.cash.turbine,
        libs.com.google.test.parameter.injector,
        libs.junit,
        libs.kotlinx.coroutines.test,
    ).forEach(::testImplementation)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        html.required = true
        xml.required = true
    }
}
mavenPublishingConfig {
    mavenConfigBlock {
        name.set("GOV.UK One Login Wallet Sharing: Digital Credential Orchestrator")
        description.set(
            """
            Provides the Orchestration layer.
            """.trimIndent()
        )
    }
}
