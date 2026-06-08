package uk.gov.onelogin.sharing.bluetooth.api.core

enum class BluetoothStatus {
    ON,
    OFF,
    TURNING_ON,
    TURNING_OFF,
    UNKNOWN;

    fun isOff(): Boolean = this in offStates

    companion object {
        private val offStates = listOf(
            OFF,
            TURNING_OFF
        )
    }
}
