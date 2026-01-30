package uk.gov.onelogin.sharing.uk.gov.onelogin.sharing.testapp.preview

import uk.gov.logging.api.Logger

class NoOpLogger : Logger {
    override fun debug(tag: String, msg: String) = Unit

    override fun info(tag: String, msg: String) = Unit

    override fun error(tag: String, msg: String, throwable: Throwable) = Unit

    override fun error(tag: String, msg: String) = Unit
}
