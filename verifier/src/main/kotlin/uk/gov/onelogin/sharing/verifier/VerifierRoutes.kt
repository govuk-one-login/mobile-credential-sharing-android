package uk.gov.onelogin.sharing.verifier

import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute

object VerifierRoutes {
    fun NavGraphBuilder.configureVerifierRoutes() {
        configureVerifierScannerRoute()
    }
}
