package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi
import uk.gov.onelogin.sharing.ui.impl.di.CredentialSharingUiGraph

class CredentialSharingUiImpl : CredentialSharingUi {
    @Composable
    override fun Render(
        sdk: CredentialSharingSdk,
        startDestination: CredentialSharingDestination,
        modifier: Modifier
    ) {
        val uiGraph = remember(sdk.appGraph) {
            createGraphFactory<CredentialSharingUiGraph.Factory>()
                .create(sdk.appGraph)
        }

        CompositionLocalProvider(
            LocalMetroViewModelFactory provides uiGraph.metroViewModelFactory
        ) {
            CredentialSharingNavHost(
                startDestination = startDestination,
                modifier = modifier
            )
        }
    }
}

@Composable
fun CredentialSharingNavHost(
    startDestination: CredentialSharingDestination,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<CredentialSharingDestination.HolderRoute> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hello World!")
            }
        }
    }
}
