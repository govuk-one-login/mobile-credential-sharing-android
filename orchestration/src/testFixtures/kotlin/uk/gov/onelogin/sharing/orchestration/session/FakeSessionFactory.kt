package uk.gov.onelogin.sharing.orchestration.session

/**
 * [SessionFactory] implementation that defers to the internal [sessions] list for instances.
 *
 * Note that errors occur when not providing enough [sessions].
 */
class FakeSessionFactory<out Session : Any>(private val sessions: List<Session>) :
    SessionFactory<Session> {
    private var callCount: Int = 0

    constructor(
        vararg sessions: Session
    ) : this(
        sessions = sessions.asList()
    )

    override fun create(): Session = sessions[callCount].also {
        ++callCount
    }

    fun getCurrentSession(): Session = sessions[callCount - 1]
}
