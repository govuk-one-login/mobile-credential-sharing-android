package uk.gov.onelogin.sharing.holder.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.holder.di.HolderGraph

@Serializable
object HolderHomeRoute {

    fun NavGraphBuilder.configureHolderWelcomeScreen(context: Context) {
        val graph = createGraphFactory<HolderGraph.Factory>()
            .create(context)

        composable<HolderHomeRoute> {
            CompositionLocalProvider(
                LocalMetroViewModelFactory provides graph.metroViewModelFactory
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HolderWelcomeScreen()
                }
            }
        }
    }
}
