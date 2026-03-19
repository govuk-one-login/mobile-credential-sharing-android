package uk.gov.onelogin.sharing.testapp.logger

import uk.gov.logging.api.v2.CrashLogger
import uk.gov.logging.api.v2.errorKeys.ErrorKeys

/**
 * Dependency for AndroidLogger.
 *
 * This is to be replaced with Firebase Crashlytics
 */
class SystemCrashLogger : CrashLogger {
    override fun log(throwable: Throwable, vararg errorKeys: ErrorKeys) {
        val keys = errorKeys.joinToString { "${it.key}=${it.value}" }
        println("Crash logged: ${throwable.message} [$keys]")
    }

    override fun log(throwable: Throwable) {
        println("Crash logged: ${throwable.message}")
    }

    override fun log(message: String) {
        println("Crash logged: $message")
    }
}
