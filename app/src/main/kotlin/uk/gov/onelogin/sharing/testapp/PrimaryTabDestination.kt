package uk.gov.onelogin.sharing.testapp

import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeRoute

@Serializable
sealed class PrimaryTabDestination(val label: String) {
    abstract fun getStartRoute(): Any

    @Serializable
    data object Holder : PrimaryTabDestination(
        "Holder"
    ) {
        override fun getStartRoute(): Any = HolderWelcomeRoute
    }

    // DCMAW-16273: Update start route destination
    @Serializable
    data object Verifier : PrimaryTabDestination(
        "Verifier"
    ) {
        override fun getStartRoute(): Any = Unit
    }

    companion object {
        @JvmStatic
        fun entries(): List<PrimaryTabDestination> = listOf(
            Holder,
            Verifier
        )
    }
}
