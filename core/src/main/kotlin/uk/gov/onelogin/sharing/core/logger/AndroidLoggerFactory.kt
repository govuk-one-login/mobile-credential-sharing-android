package uk.gov.onelogin.sharing.core.logger

import uk.gov.logging.api.Logger


object AndroidLoggerFactory {
    fun create(): Logger = AndroidLogger()
}