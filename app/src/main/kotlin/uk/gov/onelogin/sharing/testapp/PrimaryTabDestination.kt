package uk.gov.onelogin.sharing.testapp

import android.os.Parcelable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.collections.immutable.toPersistentList
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute

@Serializable
@Parcelize
sealed class PrimaryTabDestination(val label: String) : Parcelable {
    abstract fun getStartRoute(): Any

    @Serializable
    data object Holder : PrimaryTabDestination(
        "Holder"
    ) {
        override fun getStartRoute(): Any = HolderHomeRoute
    }

    @Serializable
    data object Verifier : PrimaryTabDestination(
        "Verifier"
    ) {
        override fun getStartRoute(): Any = VerifierScanRoute
    }

    companion object {
        @JvmStatic
        fun entries(): List<PrimaryTabDestination> = listOf(
            Holder,
            Verifier
        )

        fun NavGraphBuilder.configureTestAppRoutes(
            onNavigate: (Any, NavOptionsBuilder.() -> Unit) -> Unit = { _, _ -> }
        ) {
            composable<Holder> {
                TestAppEntries(
                    entries = listOf(
                        "Welcome screen" to HolderHomeRoute
                    ).sortedBy { navPair -> navPair.first }
                        .toPersistentList(),
                    onNavigate = onNavigate
                )
            }
            composable<Verifier> {
                TestAppEntries(
                    entries = listOf(
                        "QR Scanner" to VerifierScanRoute
                    ).sortedBy { navPair -> navPair.first }
                        .toPersistentList(),
                    onNavigate = onNavigate
                )
            }
        }
    }
}
