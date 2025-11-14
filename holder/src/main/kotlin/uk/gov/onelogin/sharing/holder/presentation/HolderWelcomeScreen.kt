package uk.gov.onelogin.sharing.holder.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserStartResult
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HolderWelcomeText()

        val eDeviceKey = SessionSecurityImpl()
        val ecPublicKey = eDeviceKey.generateEcPublicKey(EC_ALGORITHM, EC_PARAMETER_SPEC)
        val uuid = UUID.randomUUID()
        val engagement = EngagementGenerator()

        ecPublicKey?.let {
            val key = CoseKey.generateCoseKey(it)
            println("Successfully created CoseKey: $key")

            QrCodeImage(
                modifier = Modifier,
                data = "mdoc:${engagement.qrCodeEngagement(key, uuid)}",
                size = QR_SIZE
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BluetoothScreen(uuid = uuid)
    }
}

@Suppress("LongMethod")
@Composable
fun BluetoothScreen(uuid: UUID, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val stopAdvertisingEnable = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Bluetooth Advertising",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        val bluetoothAdapterProvider = AndroidBluetoothAdapterProvider(context)
        val bleAdvertiser = remember {
            AndroidBleAdvertiser(
                bleProvider = AndroidBleProvider(
                    bluetoothAdapter = bluetoothAdapterProvider,
                    bleAdvertiser = AndroidBluetoothAdvertiserProvider(bluetoothAdapterProvider)
                ),
                permissionChecker = BluetoothPermissionChecker(context)
            )
        }

        val bleAdvertiserState by bleAdvertiser.state.collectAsState()

        val advertiseData = BleAdvertiseData(
            serviceUuid = uuid
        )

        Row {
            Button(onClick = {
                scope.launch {
                    val result = bleAdvertiser.startAdvertise(advertiseData)
                    if (result is AdvertiserStartResult.Error) {
                        Toast.makeText(
                            context,
                            result.error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }) {
                Text("Start")
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Button(
                enabled = stopAdvertisingEnable.value,
                onClick = {
                    scope.launch {
                        bleAdvertiser.stopAdvertise()
                    }
                }
            ) {
                Text("Stop")
            }
        }

        when (bleAdvertiserState) {
            AdvertiserState.Started -> {
                stopAdvertisingEnable.value = true
            }

            else -> {
                stopAdvertisingEnable.value = false
            }
        }

        Text(
            text = "State: $bleAdvertiserState",
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
