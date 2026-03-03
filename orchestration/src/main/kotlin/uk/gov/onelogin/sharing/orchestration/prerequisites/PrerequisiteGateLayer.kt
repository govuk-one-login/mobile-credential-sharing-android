package uk.gov.onelogin.sharing.orchestration.prerequisites

/**
 * Sealed interface that contains abstractions designed to verify the device state during the
 * 'Pre-flight' stage of the User journey.
 *
 * @see uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
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
         * [PrerequisiteResponse.Unauthorized].
         */
        fun checkAuthorization(prerequisite: Prerequisite): PrerequisiteResponse.Unauthorized?
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
         * [PrerequisiteResponse.Incapable].
         */
        fun checkCapability(prerequisite: Prerequisite): PrerequisiteResponse.Incapable?
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
         * [PrerequisiteResponse.NotReady].
         */
        fun checkReadiness(prerequisite: Prerequisite): PrerequisiteResponse.NotReady?
    }
}
