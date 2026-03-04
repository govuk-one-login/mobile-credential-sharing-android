package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

/**
 * State table for the different reasons that a device fails capability checks.
 */
sealed class IncapableReason {
    /**
     * State for when the device doesn't have the necessary hardware for a given
     * [uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite].
     */
    data object MissingHardware : IncapableReason()
}
