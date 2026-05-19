plugins {
    listOf(
        libs.plugins.templates.kotlin.library
    ).forEach { alias(it) }
}

dependencies {
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
            "GOV.UK One Login Wallet Sharing: Verification module"
        )
        description.set(
            """
            Provides data structures and business logic for handling document verifications.
            """.trimIndent()
        )
    }
}
