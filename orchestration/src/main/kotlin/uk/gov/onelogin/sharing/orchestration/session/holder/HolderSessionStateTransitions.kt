package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlin.reflect.KClass

/**
 * Convenience alias for defining a [Map] of [HolderSessionState] types to a [Set] of
 * applicable [HolderSessionState].
 */
typealias HolderSessionStateTransitions =
        Map<KClass<out HolderSessionState>, Set<KClass<out HolderSessionState>>>

/**
 * The [HolderSessionStateTransitions] [Map] containing [HolderSessionState] classes as keys.
 * The provided values are then a [Set] of applicable [HolderSessionState]s.
 *
 * @sample HolderSessionImpl.transitionTo
 */
val validHolderTransitions: HolderSessionStateTransitions = mapOf(
    HolderSessionState.NotStarted::class to setOf(
        HolderSessionState.Initialising::class,
    ),
    HolderSessionState.Initialising::class to setOf(),
)
