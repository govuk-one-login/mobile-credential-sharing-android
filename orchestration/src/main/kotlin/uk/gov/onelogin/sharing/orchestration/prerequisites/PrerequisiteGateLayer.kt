package uk.gov.onelogin.sharing.orchestration.prerequisites

/**
 * Sealed interface that contains abstractions designed to verify the device state during the
 * proceeding list of session states:
 *
 * - `NotStarted`: Perform the checks when beginning the Orchestrator journey.
 * - `Preflight`: Perform the checks after initially failing to meet prerequisites.
 *
 * @see uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.NotStarted
 * @see uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
 * @see uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.NotStarted
 * @see uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Preflight
 */
sealed interface PrerequisiteGateLayer {
    /**
     * Abstraction for authorizing observable capabilities. This ensures that the app is able to
     * interact with a given [Prerequisite], which usually defers to permission checking.
     */
    fun interface Authorization : PrerequisiteGateLayer {
        /**
         * Validate the [prerequisite] capability.
         *
         * @return `null` when validations pass. Otherwise, an instance of
         * [MissingPrerequisiteReason.Unauthorized].
         */
        fun checkAuthorization(prerequisite: Prerequisite): MissingPrerequisiteReason.Unauthorized?
    }

    /**
     * Abstraction for validating the capability of an Android-powered device. This ensures that the
     * device has the necessary hardware, based on the provided [Prerequisite].
     */
    fun interface Capability : PrerequisiteGateLayer {
        /**
         * Validate the [prerequisite] capability.
         *
         * @return `null` when validations pass. Otherwise, an instance of
         * [MissingPrerequisiteReason.Incapable].
         */
        fun checkCapability(prerequisite: Prerequisite): MissingPrerequisiteReason.Incapable?
    }

    /**
     * Abstraction for validating the readiness to use a [Prerequisite] on an Android-powered
     * device. This ensures that the device state is in the necessary state to perform actions based
     * on the [Prerequisite], such as Bluetooth being turned on.
     */
    fun interface Readiness : PrerequisiteGateLayer {
        /**
         * Validate the [prerequisite] capability.
         *
         * @return `null` when validations pass. Otherwise, an instance of
         * [MissingPrerequisiteReason.NotReady].
         */
        fun checkReadiness(prerequisite: Prerequisite): MissingPrerequisiteReason.NotReady?
    }
}
