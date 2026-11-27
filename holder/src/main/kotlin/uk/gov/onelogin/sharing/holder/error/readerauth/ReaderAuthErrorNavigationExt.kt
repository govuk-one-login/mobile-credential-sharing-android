package uk.gov.onelogin.sharing.holder.error.readerauth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object ReaderAuthErrorNavigationExt {
    fun NavController.navigateToReaderAuthError(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(ReaderAuthErrorRoute, options)

    internal fun NavGraphBuilder.configureReaderAuthError() {
        composable<ReaderAuthErrorRoute> {
            ReaderAuthErrorScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
