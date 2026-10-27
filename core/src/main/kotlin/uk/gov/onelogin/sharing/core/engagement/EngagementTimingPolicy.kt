package uk.gov.onelogin.sharing.core.engagement

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Single source of truth for engagement (QR presentation) timing, shared between the orchestrator
 * and the presentation ViewModel so their timers cannot drift apart.
 *
 * The orchestrator owns the real session [deadline]: on expiry with no Verifier connection it
 * refreshes the session in place (new keys, new BLE UUID, new QR). The ViewModel hides the QR
 * behind a loading spinner [graceDelay] before that deadline, purely as a cosmetic cue that a
 * refresh is imminent. Because [qrVisibleDuration] is derived from [deadline] minus [graceDelay],
 * configuring [deadline] in one place keeps the cosmetic hide exactly [graceDelay] ahead of the
 * refresh.
 *
 * @param deadline The session deadline (T). The orchestrator refreshes the engagement at this
 * point. Defaults to 300s per ISO/IEC 18013-5 in-person presentation.
 * @param graceDelay How long before [deadline] the QR is cosmetically hidden. Defaults to 1s.
 */
data class EngagementTimingPolicy(
    val deadline: Duration = DEFAULT_DEADLINE,
    val graceDelay: Duration = DEFAULT_GRACE_DELAY
) {
    init {
        require(graceDelay < deadline) {
            "graceDelay ($graceDelay) must be shorter than deadline ($deadline)"
        }
    }

    /**
     * The cosmetic window for which the QR is shown before being hidden: [deadline] minus
     * [graceDelay]. Always exactly [graceDelay] shorter than the orchestrator's [deadline].
     */
    val qrVisibleDuration: Duration get() = deadline - graceDelay

    companion object {
        val DEFAULT_DEADLINE: Duration = 10.seconds
        val DEFAULT_GRACE_DELAY: Duration = 1.seconds
    }
}
