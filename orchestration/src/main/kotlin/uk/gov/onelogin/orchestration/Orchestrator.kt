package uk.gov.onelogin.orchestration

interface Orchestrator {

    fun start()

    fun cancel()

    interface Holder : Orchestrator
}
