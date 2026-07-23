package uk.gov.onelogin.sharing.core.timer

interface SessionTimeout {
    fun start()

    fun restart()

    fun onComplete()
}