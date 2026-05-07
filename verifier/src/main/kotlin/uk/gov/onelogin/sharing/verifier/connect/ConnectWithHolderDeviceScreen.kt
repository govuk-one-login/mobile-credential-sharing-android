package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder

@Composable
fun ConnectWithHolderDeviceScreen(modifier: Modifier = Modifier) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("ConnectWithHolderDeviceScreen")
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Connect with holder device screen")
    }
}
