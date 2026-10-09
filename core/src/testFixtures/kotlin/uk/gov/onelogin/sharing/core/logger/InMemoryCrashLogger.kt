package uk.gov.onelogin.sharing.core.logger

import uk.gov.logging.api.v2.CrashLogger
import uk.gov.logging.api.v2.errorKeys.ErrorKeys

class InMemoryCrashLogger :
    CrashLogger,
    Iterable<InMemoryCrashLogger.LogEvent> {
    private var events = mutableListOf<LogEvent>()

    override fun iterator(): Iterator<LogEvent> = events.iterator()

    override fun log(message: String) {
        events.add(LogEvent.Message(message))
    }

    override fun log(throwable: Throwable) {
        events.add(LogEvent.Error.Basic(throwable))
    }

    override fun log(throwable: Throwable, vararg errorKeys: ErrorKeys) {
        events.add(LogEvent.Error.Keyed(throwable, errorKeys.toList()))
    }

    sealed interface LogEvent {
        data class Message(val result: String) : LogEvent

        sealed interface Error : LogEvent {
            val throwable: Throwable

            data class Basic(override val throwable: Throwable) : Error
            data class Keyed(override val throwable: Throwable, val errorKeys: List<ErrorKeys>) :
                Error,
                Iterable<ErrorKeys> by errorKeys
        }
    }
}
