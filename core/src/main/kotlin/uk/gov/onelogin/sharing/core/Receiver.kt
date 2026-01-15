package uk.gov.onelogin.sharing.core

/**
 * Functional interface that encapsulates (hides) behaviour.
 *
 * Implementations use this interface to receive [Event] objects. Performed behaviour is contextual
 * to the underlying implementation.
 */
fun interface Receiver<Event : Any> {
    /**
     * Performs behaviour based on the provided [event].
     * @see Function1.invoke
     */
    fun receive(event: Event)
}
