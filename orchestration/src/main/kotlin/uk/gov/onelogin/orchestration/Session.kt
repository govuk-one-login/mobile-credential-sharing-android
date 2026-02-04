package uk.gov.onelogin.orchestration

fun interface Session {
    fun transitionToState(state: String)
}
