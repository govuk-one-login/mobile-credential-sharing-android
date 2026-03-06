package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object NotReadyReasonMatchers {
    fun cameraAlreadyInUse(): Matcher<NotReadyReason> =
        equalTo(NotReadyReason.CameraAlreadyInUse)
    fun hasBluetoothTurnedOff(): Matcher<NotReadyReason> =
        equalTo(NotReadyReason.BluetoothTurnedOff)

}
