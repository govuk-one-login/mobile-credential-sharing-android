package uk.gov.onelogin.sharing.core.sessionTimer

import kotlin.time.Duration

class FakeSessionTimer : SessionTimer {
    var startCalls = 0
    var resetCalls = 0
    var stopCalls = 0
    var lastDuration: Duration? = null
    var onTimeout: (suspend () -> Unit)? = null

    override fun start(duration: Duration, onTimeout: suspend () -> Unit) {
        startCalls++
        lastDuration = duration
        this.onTimeout = onTimeout
    }

    override fun reset() {
        resetCalls++
    }

    override fun stop() {
        stopCalls++
    }
}
