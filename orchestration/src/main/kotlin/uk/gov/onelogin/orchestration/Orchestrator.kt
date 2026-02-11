package uk.gov.onelogin.orchestration

import uk.gov.onelogin.sharing.core.Resettable

/**
 * Implements [Resettable] for clearing internal state, such as the session state machines.
 */
interface Orchestrator : Resettable {

    fun start(requiredPermissions: Set<String>)

    fun cancel()

    interface Holder : Orchestrator
    interface Verifier : Orchestrator
}
