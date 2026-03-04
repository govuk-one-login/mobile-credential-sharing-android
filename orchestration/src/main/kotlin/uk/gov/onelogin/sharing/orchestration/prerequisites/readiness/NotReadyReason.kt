package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

/**
 * State table for the different reasons that a device isn't ready to complete the User journey.
 */
sealed class NotReadyReason {
    /**
     * State for when the device doesn't have the necessary hardware for a given
     * [uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite].
     */
    data object BluetoothTurnedOff : NotReadyReason()
}
