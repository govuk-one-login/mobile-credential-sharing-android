package uk.gov.onelogin.sharing.orchestration.verifier.session

import java.util.Collections.singleton
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Verifying

/**
 * Convenience alias for defining a [Map] of [VerifierSessionState] types to a [Set] of
 * applicable [VerifierSessionState].
 */
typealias VerifierSessionStateTransitions =
    Map<KClass<out VerifierSessionState>, Set<KClass<out VerifierSessionState>>>

/**
 * Convenience [Set] for containing both User journey completion error types
 *
 * @sample validVerifierTransitions
 */
private val fullErrorHandling: Set<KClass<out VerifierSessionState>> = setOf(
    Cancelled::class,
    Failed::class
)

/**
 * The [VerifierSessionStateTransitions] [Map] containing [VerifierSessionState] classes as keys.
 * The provided values are then a [Set] of applicable [VerifierSessionState]s.
 *
 * @sample VerifierSessionImpl.transitionTo
 */
val validVerifierTransitions: VerifierSessionStateTransitions = mapOf(
    NotStarted::class to singleton(
        Preflight::class
    ),
    Preflight::class to singleton(
        ReadyToScan::class
    ) + fullErrorHandling,
    ReadyToScan::class to singleton(
        Connecting::class
    ) + fullErrorHandling,
    Connecting::class to singleton(
        ProcessingEngagement::class
    ) + fullErrorHandling,
    ProcessingEngagement::class to singleton(
        Verifying::class
    ) + fullErrorHandling,
    Verifying::class to singleton(
        Success::class
    ) + fullErrorHandling
)
