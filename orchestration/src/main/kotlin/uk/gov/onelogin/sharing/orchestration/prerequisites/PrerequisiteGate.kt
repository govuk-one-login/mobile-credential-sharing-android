package uk.gov.onelogin.sharing.orchestration.prerequisites

import uk.gov.onelogin.sharing.core.permission.PermissionCheckerResult

/**
 * Sealed interface that contains abstractions designed to verify the device state during the
 * 'Pre-flight' stage of the User journey.
 *
 * @see uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
 * @see uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Preflight
 */
sealed interface PrerequisiteGate {
    /**
     * Abstraction for validating all of the necessary permissions for the User journey.
     *
     * @param State The User journey data type. Used for injection purposes.
     */
    fun interface Permissions<in State : Any> : PrerequisiteGate {
        /**
         * Validate the permission state of the device.
         */
        fun checkPermissions(): PermissionCheckerResult
    }
}
