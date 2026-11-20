package uk.gov.onelogin.sharing.holder.qrcode

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.android.ui.theme.spacingDouble

object QrCodeNavigationExt {
    fun NavController.navigateToQrCodeScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(QrCodeRoute, options)

    internal fun NavGraphBuilder.configureQrCodeScreen() {
        composable<QrCodeRoute> {
            QrCodeScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
