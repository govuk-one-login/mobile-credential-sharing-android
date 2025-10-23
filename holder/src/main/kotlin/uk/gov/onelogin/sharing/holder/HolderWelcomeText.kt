package uk.gov.onelogin.sharing.holder

import android.util.Base64
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
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
    val deviceEngagement = DeviceEngagement(
        "1.0",
        listOf(
            BleDeviceRetrievalMethod()
        )
    )
    val bytes = DeviceEngagementCbor.encode(deviceEngagement)
    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

    Log.d("MainActivity", "$base64")
}
