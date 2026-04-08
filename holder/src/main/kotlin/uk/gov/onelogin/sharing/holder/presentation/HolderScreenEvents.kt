package uk.gov.onelogin.sharing.holder.presentation

import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError

sealed interface HolderScreenEvents {
    data class NavigateToError(val error: BluetoothSessionError) : HolderScreenEvents
}
