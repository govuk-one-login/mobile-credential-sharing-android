package uk.gov.onelogin.sharing.verifier.finish

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

@Composable
internal fun FinishedVerifierJourneyScreen(
    response: DeviceResponse,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    viewModel: FinishedVerifierJourneyViewModel = metroViewModel(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(spacingDouble),
    onExitJourney: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("FinishedVerifierJourneyScreen")
    }

    BackHandler(enabled = true) {
        scope.launch {
            viewModel.reset()
            onExitJourney()
        }
    }

    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = verticalArrangement
    ) {
        Text(
            "You have successfully verified your documents",
            modifier = Modifier.semantics {
                heading()
            }
        )

        response.documents!!.forEach { document ->
            Text(document.toString())
        }

        Button(
            onClick = {
                scope.launch {
                    viewModel.reset()
                    onExitJourney()
                }
            }
        ) {
            Text("Exit journey")
        }
    }
}
