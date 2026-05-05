package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.HolderRoutes.convertSessionStateToNavigation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Composable
fun MonitorHolderSessionState(
    holderSessionState: StateFlow<HolderSessionState>,
    navController: NavHostController,
    context: Context = LocalContext.current
) {
    val coroutineScope = rememberCoroutineScope { Dispatchers.Main }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            holderSessionState.map { state ->
                convertSessionStateToNavigation(
                    context,
                    navController,
                    state
                )
            }.collect { navigationFunction ->
                navigationFunction()
            }
        }
    }
}
