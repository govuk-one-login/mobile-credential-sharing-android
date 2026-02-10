package uk.gov.onelogin.sharing.orchestration.session.verifier

import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Verifying

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
    ),
    Preflight::class to emptySet(),
    ReadyToScan::class to emptySet(),
    Connecting::class to emptySet(),
    ProcessingEngagement::class to emptySet(),
    Verifying::class to emptySet(),
)
