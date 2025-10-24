package uk.gov.onelogin.sharing.holder

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementCbor

@Composable
fun HolderWelcomeText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "Welcome to GOV.UK Wallet Sharing"
    )

    test()
}

private fun test() {
    val fakeKeyBytes = "FAKE_EDEVICE_KEY".toByteArray()
    val uuid = "11111111-2222-3333-4444-555555555555"

    val deviceEngagement = DeviceEngagement.builder()
        .version("1.0")
        .security(fakeKeyBytes, 1)
        .ble(peripheralUuid = uuid)
        .build()

    val bytes = DeviceEngagementCbor.encode(deviceEngagement)
    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

    @SuppressLint("LogConditional")
    Log.d("MainActivity", "$base64")
}
