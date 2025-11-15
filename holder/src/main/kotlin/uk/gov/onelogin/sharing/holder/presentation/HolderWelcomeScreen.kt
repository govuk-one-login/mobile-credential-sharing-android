package uk.gov.onelogin.sharing.holder.presentation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderWelcomeViewModel = holderWelcomeViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier) {
        RequestPermissions()
        HolderWelcomeText()

        HolderWelcomeScreenContent(
            errorMessage = uiState.lastErrorMessage,
            advertiserState = uiState.advertiserState,
            uuid = uiState.uuid.toString(),
            qrCodeData = uiState.qrData,
            onStartClick = viewModel::onStartAdvertise,
            onStopClick = viewModel::onStopAdvertise,
            onShowError = viewModel::onErrorMessageShown
        )
    }
}

// This will be updated in - https://govukverify.atlassian.net/browse/DCMAW-16531
@Composable
private fun RequestPermissions() {
    val context = LocalContext.current
    val permissionChecker = BluetoothPermissionChecker(context)
    var hasPermission by remember { mutableStateOf(permissionChecker.hasPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    LaunchedEffect(hasPermission) {
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
    }
}

@Suppress("LongMethod")
@Composable
fun HolderWelcomeScreenContent(
    errorMessage: String?,
    advertiserState: AdvertiserState,
    uuid: String,
    qrCodeData: String?,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onShowError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentErrorShown by rememberUpdatedState(onShowError)

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            currentErrorShown()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        qrCodeData?.let {
            QrCodeImage(
                data = it,
                size = QR_SIZE
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bluetooth Advertising",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row {
            Button(onClick = onStartClick) {
                Text("Start")
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Button(
                enabled = advertiserState == AdvertiserState.Started,
                onClick = onStopClick
            ) {
                Text("Stop")
            }
        }

        Text(
            text = "Status: $advertiserState",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (advertiserState == AdvertiserState.Started) {
            Text(
                text = "UUID: $uuid"
            )
        }
    }
}

// This can be removed when DI is added
@Composable
private fun holderWelcomeViewModel(): HolderWelcomeViewModel {
    val context = LocalContext.current

    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(
                    modelClass.isAssignableFrom(
                        HolderWelcomeViewModel::class.java
                    )
                ) {
                    "Unknown ViewModel class $modelClass"
                }

                val adapterProvider = AndroidBluetoothAdapterProvider(context)
                val bleAdvertiser = AndroidBleAdvertiser(
                    bleProvider = AndroidBleProvider(
                        bluetoothAdapter = adapterProvider,
                        bleAdvertiser = AndroidBluetoothAdvertiserProvider(
                            adapterProvider
                        )
                    ),
                    permissionChecker = BluetoothPermissionChecker(context)
                )

                return HolderWelcomeViewModel(
                    sessionSecurity = SessionSecurityImpl(),
                    engagementGenerator = EngagementGenerator(),
                    bleAdvertiser = bleAdvertiser
                ) as T
            }
        }
    }

    return viewModel(factory = factory)
}

@Preview
@Composable
private fun HolderWelcomeScreenPreview() {
    HolderWelcomeScreenContent(
        errorMessage = null,
        advertiserState = AdvertiserState.Started,
        uuid = "11111111-2222-3333-4444-555555555555",
        qrCodeData = "QR Data",
        onStartClick = {},
        onStopClick = {},
        onShowError = {},
        modifier = Modifier
    )
}
