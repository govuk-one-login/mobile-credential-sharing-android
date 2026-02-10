package uk.gov.onelogin.sharing.orchestration.session

/**
 * Functional interface that allows implementations to transition to different [State]s.
 */
fun interface StateTransitional<State : Any> {
    /**
     * Updates the internal state based on validations.
     *
     * @throws IllegalStateException when the provided [state] cannot be transitioned to.
     */
    @Throws(IllegalStateException::class)
    fun transitionTo(state: State)
}