package uk.gov.onelogin.sharing.core.sessionTimer

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.di.ApplicationScope

/**
 * Coroutine-based implementation of [SessionTimer].
 *
 * @param scope The [CoroutineScope] in which to run the timer job.
 */
@ContributesBinding(scope = AppScope::class, binding = binding<SessionTimer>())
class SessionTimerImpl(@param:ApplicationScope private val scope: CoroutineScope) : SessionTimer {
    private var timeoutJob: Job? = null
    private var onTimeoutAction: (suspend () -> Unit)? = null
    private var timeoutDuration: Duration? = null

    override fun start(duration: Duration, onTimeout: suspend () -> Unit) {
        this.timeoutDuration = duration
        this.onTimeoutAction = onTimeout
        reset()
    }

    override fun reset() {
        val duration = timeoutDuration ?: return
        val action = onTimeoutAction ?: return

        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(duration)
            action()
        }
    }

    override fun stop() {
        timeoutJob?.cancel()
        timeoutJob = null
        onTimeoutAction = null
        timeoutDuration = null
    }
}
