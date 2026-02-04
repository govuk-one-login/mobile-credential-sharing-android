package uk.gov.onelogin.orchestration

import uk.gov.onelogin.sharing.core.Receiver

interface Orchestrator {

    fun start()

    fun cancel()
}
