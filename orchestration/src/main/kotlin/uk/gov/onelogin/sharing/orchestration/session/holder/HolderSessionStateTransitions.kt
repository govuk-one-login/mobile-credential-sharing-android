package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlin.reflect.KClass

/**
 * Convenience alias for defining a [Map] of [HolderSessionState] types to a [Set] of
 * applicable [HolderSessionState].
 */
typealias HolderSessionStateTransitions =
    Map<KClass<out HolderSessionState>, Set<KClass<out HolderSessionState>>>