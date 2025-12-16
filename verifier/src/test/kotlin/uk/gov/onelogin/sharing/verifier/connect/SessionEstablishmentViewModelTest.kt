package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner

class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = ConnectWithHolderDeviceRule(createComposeRule())

    private fun createViewModel(
        bluetoothAdapterProvider: FakeBluetoothAdapterProvider = FakeBluetoothAdapterProvider(true),
        bluetoothScanner: BluetoothScanner = FakeAndroidBluetoothScanner()
    ): SessionEstablishmentViewModel =
        SessionEstablishmentViewModel(
            bluetoothAdapterProvider = bluetoothAdapterProvider,
            scanner = bluetoothScanner
        )
}