package uk.gov.onelogin.sharing.verifier.finish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

@Composable
internal fun FinishedVerifierJourneyScreen(
    response: DeviceResponse,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(spacingDouble),
    onExitJourney: () -> Unit = {}
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("FinishedVerifierJourneyScreen")
    }

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        Text(
            "You have successfully verified your documents",
            modifier = Modifier.semantics {
                heading()
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacingSingle)
        ) {
            items(response.documents!!, key = { it.docType }) { document ->
                Text(document.toString())
            }
        }
        Button(onClick = onExitJourney) {
            Text("Exit journey")
        }
    }
}
