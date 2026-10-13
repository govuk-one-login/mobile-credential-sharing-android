package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder

@Composable
internal fun TurnOnBluetoothScreen(modifier: Modifier = Modifier) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("TurnOnBluetoothScreen")
    }
    TurnOnBluetoothContent(modifier)
}

@Composable
internal fun TurnOnBluetoothContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Turn on Bluetooth")
    }
}

@Composable
@Preview(showBackground = true)
internal fun TurnOnBluetoothScreenPreview() {
    GdsTheme {
        TurnOnBluetoothContent(modifier = Modifier.fillMaxSize())
    }
}