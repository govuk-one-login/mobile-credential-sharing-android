plugins {
    listOf(
        libs.plugins.templates.kotlin.library
    ).forEach { alias(it) }
}

dependencies {
    listOf(
        libs.org.hamcrest
    ).forEach(::testFixturesApi)

    listOf(
        libs.junit,
        libs.com.google.test.parameter.injector,
        libs.io.github.classgraph,
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
            "GOV.UK One Login Wallet Sharing: Verification formats"
        )
        description.set(
            """
            Provides data structures for the Credential verification (`:credential-verification`)
            module.
            """.trimIndent()
        )
    }
}
