package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.RequestReceived

/**
 * Convenience alias for defining a [Map] of [HolderSessionState] types to a [Set] of
 * applicable [HolderSessionState].
 */
typealias HolderSessionStateTransitions =
    Map<KClass<out HolderSessionState>, Set<KClass<out HolderSessionState>>>

/**
 * Convenience [Set] for containing both User journey completion error types
 *
 * @sample validHolderTransitions
 */
private val fullErrorHandling: Set<KClass<out HolderSessionState>> = setOf(
    Cancelled::class,
    Failed::class
)

/**
 * The [HolderSessionStateTransitions] [Map] containing [HolderSessionState] classes as keys.
 * The provided values are then a [Set] of applicable [HolderSessionState]s.
 *
 * @sample HolderSessionImpl.transitionTo
 */
val validHolderTransitions: HolderSessionStateTransitions = mapOf(
    NotStarted::class to setOf(
        Preflight::class
    ),
    Preflight::class to setOf(
        ReadyToPresent::class
    ) + fullErrorHandling,
    ReadyToPresent::class to setOf(
        PresentingEngagement::class
    ) + fullErrorHandling,
    PresentingEngagement::class to setOf(
        Connecting::class,
        Cancelled::class
    ),
    Connecting::class to setOf(
        RequestReceived::class
    ) + fullErrorHandling,
    RequestReceived::class to setOf(
        ProcessingResponse::class
    ) + fullErrorHandling,
    ProcessingResponse::class to setOf(
        Success::class
    ) + fullErrorHandling
)
