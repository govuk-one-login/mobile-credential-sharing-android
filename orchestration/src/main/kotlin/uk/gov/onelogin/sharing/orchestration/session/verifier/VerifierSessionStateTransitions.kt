package uk.gov.onelogin.sharing.orchestration.session.verifier

import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Preflight

/**
 * Convenience alias for defining a [Map] of [VerifierSessionState] types to a [Set] of
 * applicable [VerifierSessionState].
 */
typealias VerifierSessionStateTransitions =
    Map<KClass<out VerifierSessionState>, Set<KClass<out VerifierSessionState>>>

/**
 * The [VerifierSessionStateTransitions] [Map] containing [VerifierSessionState] classes as keys.
 * The provided values are then a [Set] of applicable [VerifierSessionState]s.
 *
 * @sample VerifierSessionImpl.transitionTo
 */
val validVerifierTransitions: VerifierSessionStateTransitions = mapOf(
    NotStarted::class to setOf(
        Preflight::class
    )
)
