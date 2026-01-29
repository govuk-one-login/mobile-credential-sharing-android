plugins {
    listOf(
        libs.plugins.templates.kotlin.library
    ).forEach { alias(it) }
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Core"
        )
        description.set(
            """
            A module for holding behaviour that's common throughout the wallet sharing code base.
            """.trimIndent()
        )
    }
}
