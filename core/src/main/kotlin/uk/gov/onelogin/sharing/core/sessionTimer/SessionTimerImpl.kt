package uk.gov.onelogin.sharing.core.sessionTimer

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag

/**
 * Coroutine-based implementation of [SessionTimer].
 *
 * @param scope The [CoroutineScope] in which to run the timer job.
 */
@ContributesBinding(scope = SharingSessionScope::class, binding = binding<SessionTimer>())
class SessionTimerImpl(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val logger: Logger
) : SessionTimer {
    private var timeoutJob: Job? = null
    private var onTimeoutAction: (suspend () -> Unit)? = null
    private var timeoutDuration: Duration? = null

    override fun start(duration: Duration, onTimeout: suspend () -> Unit) {
        logger.debug(logTag, "Session timer started")
        this.timeoutDuration = duration
        this.onTimeoutAction = onTimeout
        reset()
    }

    override fun reset() {
        logger.debug(logTag, "Session timer reset")
        val duration = timeoutDuration ?: return
        val action = onTimeoutAction ?: return

        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(duration)
            action()
        }
    }

    override fun stop() {
        logger.debug(logTag, "Session timer stopped")
        timeoutJob?.cancel()
        timeoutJob = null
        onTimeoutAction = null
        timeoutDuration = null
    }
}
