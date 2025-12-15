@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.verifier.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.core.UUIDExtensions.toByteArray
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID

@Inject
@ViewModelKey(SessionEstablishmentViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SessionEstablishmentViewModel(
    private val bluetoothAdapterProvider: BluetoothAdapterProvider,
    private val scanner: BluetoothScanner
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectWithHolderDeviceState())
    val uiState: StateFlow<ConnectWithHolderDeviceState> = _uiState
    private var scannerJob: Job? = null

    init {
        _uiState.update {
            it.copy(
                isBluetoothEnabled = bluetoothAdapterProvider.isEnabled()
            )
        }
    }

    fun scanForDevice(uuid: ByteArray) {
        scannerJob = viewModelScope.launch(Dispatchers.IO) {

            val uuidConvert = uuid.toUUID()
            val uuidBigEndian = uuidConvert.toByteArray()

            try {
                withTimeout(30000L) {
                    val scanFlow = scanner.scan(
                        scanningPeriodMilliseconds = 30_000L,
                        peripheralServerModeUuids = listOf(uuidBigEndian)
                    )

                    val result = scanFlow.first()

                    Log.d(
                        SessionEstablishmentViewModel::class.simpleName,
                        result.toString()
                    )
                }
            } catch (exception: TimeoutCancellationException) {
                Log.w(
                    SessionEstablishmentViewModel::class.java.simpleName,
                    "Timeout occurred when scanning for UUIDs.",
                    exception
                )
            }
        }
    }

    fun stopScanning() {
        Log.d(SessionEstablishmentViewModel::class.simpleName, "Stopping scanner")
        scannerJob?.cancel()
    }

    override fun onCleared() {
        Log.d(SessionEstablishmentViewModel::class.simpleName, "VM cleared, stopping scanner")
        scannerJob?.cancel()
        super.onCleared()
    }
}
