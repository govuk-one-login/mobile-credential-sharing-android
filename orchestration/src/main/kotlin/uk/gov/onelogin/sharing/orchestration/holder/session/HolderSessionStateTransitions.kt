package uk.gov.onelogin.sharing.orchestration.holder.session

import java.util.Collections.singleton
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.RequestReceived

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
        Preflight::class,
        ReadyToPresent::class
    ),
    Preflight::class to singleton(
        ReadyToPresent::class
    ) + fullErrorHandling,
    ReadyToPresent::class to singleton(
        PresentingEngagement::class
    ) + fullErrorHandling,
    PresentingEngagement::class to setOf(
        Connecting::class,
        Cancelled::class
    ),
    Connecting::class to singleton(
        RequestReceived::class
    ) + fullErrorHandling,
    RequestReceived::class to singleton(
        ProcessingResponse::class
    ) + fullErrorHandling,
    ProcessingResponse::class to singleton(
        Success::class
    ) + fullErrorHandling
)
