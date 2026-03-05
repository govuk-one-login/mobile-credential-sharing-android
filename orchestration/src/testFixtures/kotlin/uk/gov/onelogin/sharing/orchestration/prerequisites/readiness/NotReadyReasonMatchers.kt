package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object NotReadyReasonMatchers {
    fun hasBluetoothTurnedOff(): Matcher<NotReadyReason> =
        equalTo(NotReadyReason.BluetoothTurnedOff)
}
