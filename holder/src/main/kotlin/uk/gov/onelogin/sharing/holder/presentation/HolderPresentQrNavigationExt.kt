package uk.gov.onelogin.sharing.holder.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitle
import uk.gov.onelogin.sharing.holder.HolderNavigationExtensions.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.navigateToHolderConsentScreen
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorNavigationExt.navigateToUnrecoverableHolderError

object HolderPresentQrNavigationExt {
    fun NavController.navigateToHolderPresentQrScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderPresentQrRoute, options)

    internal fun NavGraphBuilder.configureHolderPresentQrScreen(controller: NavController) {
        composable<HolderPresentQrRoute> {
            val context = LocalContext.current
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val scope = rememberCoroutineScope { Dispatchers.Main }

                HolderWelcomeScreen(
                    onAwaitingUserConsent = {
                        controller.navigateToHolderConsentScreen()
                    },
                    onConnectionError = {
                        errorTitle(context, it)
                            .let { title ->
                                scope.launch {
                                    controller.navigateToBluetoothConnectionErrorRoute(title)
                                }
                                title
                            }
                            .also {
                                Log.w(
                                    HolderRoutes::class.java.simpleName,
                                    "Navigated to error screen: $it"
                                )
                            }
                    },
                    onGenericError = {
                        scope.launch {
                            controller.navigateToUnrecoverableHolderError()
                        }
                    }
                )
            }
        }
    }
}
