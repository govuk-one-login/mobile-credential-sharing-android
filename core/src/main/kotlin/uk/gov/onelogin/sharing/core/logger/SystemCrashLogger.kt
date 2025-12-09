package uk.gov.onelogin.sharing.core.logger

import uk.gov.logging.api.CrashLogger

/**
 * Dependency for AndroidLogger.
 *
 * This is to be replaced with Firebase Crashlytics
 */
class SystemCrashLogger : CrashLogger {
    override fun log(throwable: Throwable) {
        println("Crash logged: ${throwable.message}")
    }

    override fun log(message: String) {
        println("Crash logged: $message")
    }
}
