package uk.gov.onelogin.sharing.orchestration.prerequisites

import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.AuthorizationRequest
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.AuthorizationResponse

/**
 * Sealed interface that contains abstractions designed to verify the device state during the
 * 'Pre-flight' stage of the User journey.
 *
 * @see uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
 * @see uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Preflight
 */
sealed interface PrerequisiteGate {
    /**
     * Abstraction for authorizing observable capabilities.
     */
    fun interface Authorization : PrerequisiteGate {
        /**
         * Validate the [request]ed capabilities are authorized.
         */
        fun checkAuthorization(request: AuthorizationRequest): AuthorizationResponse
    }
}
