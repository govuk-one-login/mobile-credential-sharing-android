package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.android.ui.theme.spacingDouble

@Composable
fun ConnectWithHolderDeviceScreen(mdocUri: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        Text("Successfully scanned QR code:")
        Text(mdocUri)
    }
}
