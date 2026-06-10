plugins {
    listOf(
        libs.plugins.templates.kotlin.library,
        libs.plugins.kotlin.serialization
    ).forEach { alias(it) }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    listOf(
        projects.credentialFormat,
        libs.jackson.cbor
    ).forEach(::api)

    listOf(
        libs.jackson.core,
        libs.jackson.kotlin,
        libs.kotlinx.serialization.json
    ).forEach(::implementation)

    listOf(
        libs.junit,
        libs.kotlin.test,
        libs.org.hamcrest,
        libs.com.google.test.parameter.injector
    ).forEach(::testImplementation)

    listOf(
        libs.junit,
        libs.jackson.cbor,
        libs.com.google.test.parameter.injector
    ).forEach(::testFixturesImplementation)
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
