package uk.gov.onelogin.sharing.core.logger

import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger


object AndroidLoggerFactory {
    fun create(): Logger = SystemLogger()
}

val Any.logTag: String
    get() = this.javaClass.simpleName