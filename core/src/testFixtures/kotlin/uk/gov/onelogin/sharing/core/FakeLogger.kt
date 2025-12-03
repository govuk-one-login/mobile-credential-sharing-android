package uk.gov.onelogin.sharing.core

import uk.gov.logging.api.Logger

class FakeLogger: Logger {
    override fun debug(tag: String, msg: String) {
        println("DEBUG: $tag: $msg")
    }

    override fun info(tag: String, msg: String) {
        println("INFO: $tag: $msg")
    }

    override fun error(tag: String, msg: String, throwable: Throwable) {
        println("ERROR: $tag: $msg")
        throwable.printStackTrace()
    }

    override fun error(tag: String, msg: String) {
        println("ERROR: $tag: $msg")
    }
}