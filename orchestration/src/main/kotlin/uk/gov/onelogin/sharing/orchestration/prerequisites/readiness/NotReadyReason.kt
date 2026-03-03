package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

/**
 * State table for the different reasons that a device fails to authorize.
 */
sealed class NotReadyReason {
    /**
     * State for when the device doesn't have the necessary hardware for a given
     * [uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite].
     */
    data object BluetoothTurnedOff : NotReadyReason()
}
