package uk.gov.onelogin.sharing.holder.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitle
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.navigateToHolderConsentScreen

object HolderPresentQrNavigationExt {
    fun NavController.navigateToHolderPresentQrScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderPresentQrRoute, options)

    internal fun NavGraphBuilder.configureHolderPresentQrScreen(
        controller: NavController,
        onError: (String) -> Unit
    ) {
        composable<HolderPresentQrRoute> {
            val context = LocalContext.current
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HolderWelcomeScreen(
                    onAwaitingUserConsent = {
                        controller.navigateToHolderConsentScreen()
                    },
                    onConnectionError = {
                        errorTitle(context, it)
                            .let(onError::invoke)
                            .also {
                                Log.w(
                                    HolderRoutes::class.java.simpleName,
                                    "Navigated to error screen: $it"
                                )
                            }
                    }
                )
            }
        }
    }
}
