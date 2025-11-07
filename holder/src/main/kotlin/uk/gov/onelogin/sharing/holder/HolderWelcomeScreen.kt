package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.advertiser.AdvertiserStartResult
import uk.gov.onelogin.sharing.bluetooth.advertiser.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.advertiser.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.SessionSecurityImpl

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

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

        val bleAdvertiser = remember {
            AndroidBleAdvertiser(
                bleProvider = AndroidBleProvider(context)
            )
        }

        val bleAdvertiserState by bleAdvertiser.state.collectAsState()

        LaunchedEffect(Unit) {
            val result = bleAdvertiser.startAdvertise(
                BleAdvertiseData(
                    payload = object : BleAdvertiser.Payload {
                        override fun asBytes(): ByteArray = engagementData.toByteArray()
                    },
                    serviceUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
                )
            )
            if (result is AdvertiserStartResult.Error) {
                println("start advertise error: ${result.error}")
            }
        }

        when (bleAdvertiserState) {
            is AdvertiserState.Failed -> println("Failed")
            AdvertiserState.Idle -> println("Idle")
            AdvertiserState.Started -> println("Started")
            AdvertiserState.Starting -> println("Starting")
            AdvertiserState.Stopped -> println("Stopped")
            AdvertiserState.Stopping -> println("Stopping")
        }
    }
}
