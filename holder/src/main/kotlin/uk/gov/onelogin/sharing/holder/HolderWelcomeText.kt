package uk.gov.onelogin.sharing.holder

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security

@Composable
fun HolderWelcomeText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "Welcome to GOV.UK Wallet Sharing"
    )

    logDeviceEngagementAsCborBase64()
}

private fun logDeviceEngagementAsCborBase64() {
    val fakeKeyBytes = "FAKE_EDEVICE_KEY".toByteArray()
    val uuid = "11111111-2222-3333-4444-555555555555"
    val securityObject = Security(
        cipherSuiteIdentifier = 1,
        eDeviceKeyBytes = EmbeddedCbor(fakeKeyBytes)
    )

    val deviceEngagement = DeviceEngagement.builder(securityObject)
        .version("1.0")
        .ble(peripheralUuid = uuid)
        .build()

    val bytes = deviceEngagement.encode()
    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

    @SuppressLint("LogConditional")
    Log.d("MainActivity", "$base64")
}
