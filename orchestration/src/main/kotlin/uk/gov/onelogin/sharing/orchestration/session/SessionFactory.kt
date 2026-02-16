package uk.gov.onelogin.sharing.orchestration.session

/**
 * Functional interface for creating User journey sessions.
 *
 * @param Session The data type of the created session.
 */
fun interface SessionFactory<out Session : Any> {

    /**
     * @return A new [Session] instance.
     */
    fun create(): Session
}
