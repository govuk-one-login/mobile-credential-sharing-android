package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

object IncapableReasonMatchers {
    fun isMissingHardware(): Matcher<IncapableReason> = equalTo(IncapableReason.MissingHardware)
}
