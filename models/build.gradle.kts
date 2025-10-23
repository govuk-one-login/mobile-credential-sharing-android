plugins {
    listOf(
        libs.plugins.templates.kotlin.library
    ).forEach { alias(it) }
}

dependencies {
    listOf(
        libs.jackson.databind,
        libs.jackson.cbor,
        libs.jackson.kotlin
    ).forEach(::implementation)
}

dependencies {
    listOf(
        libs.junit
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
