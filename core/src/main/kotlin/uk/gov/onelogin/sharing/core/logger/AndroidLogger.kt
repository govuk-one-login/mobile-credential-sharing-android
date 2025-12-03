package uk.gov.onelogin.sharing.core.logger

import android.util.Log
import uk.gov.logging.api.Logger

class AndroidLogger(): Logger {
    override fun debug(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun info(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun error(tag: String, msg: String, throwable: Throwable) {
        Log.e(tag, msg, throwable)
    }

    override fun error(tag: String, msg: String) {
        Log.e(tag, msg)
    }
}