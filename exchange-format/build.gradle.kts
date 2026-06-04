plugins {
    listOf(
        libs.plugins.templates.kotlin.library,
        libs.plugins.kotlin.serialization
    ).forEach { alias(it) }
}

dependencies {
    listOf(
        projects.credentialFormat
    ).forEach(::api)

    listOf(
        libs.kotlinx.serialization.json
    ).forEach(::implementation)

    listOf(
        libs.junit,
        libs.com.google.test.parameter.injector
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
        name.set(
            "GOV.UK One Login Wallet Sharing: Digital Credential models"
        )
        description.set(
            """
            Provides data structures for digital credentials passed between credential holders and
            credential verifiers.
            """.trimIndent()
        )
    }
}
