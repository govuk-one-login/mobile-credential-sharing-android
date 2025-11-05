package uk.gov.onelogin.sharing.holder

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.le.AdvertisingSetParameters
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiserImpl
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.SessionSecurityImpl

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HolderWelcomeText()

        val eDeviceKey = SessionSecurityImpl()
        eDeviceKey.generateEcPublicKey(EC_ALGORITHM, EC_PARAMETER_SPEC)

        val engagement = EngagementGenerator()
        val engagementData = "mdoc:${engagement.qrCodeEngagement()}"
        QrCodeImage(
            modifier = Modifier,
            data = engagementData,
            size = QR_SIZE
        )

        val bleAdvertiser = BleAdvertiserImpl(LocalContext.current)

        LaunchedEffect(Unit) {
            bleAdvertiser.startAdvertise(
                Payload(engagementData.toByteArray()),
                AdvertisingSetParameters.Builder().build()
            )
        }
    }
}

class Payload(private val engagementData: ByteArray) : BleAdvertiser.Payload {
    override fun asBytes(): ByteArray {
        return engagementData
    }

}