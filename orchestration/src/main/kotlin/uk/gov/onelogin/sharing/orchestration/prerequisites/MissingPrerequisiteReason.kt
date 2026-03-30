package uk.gov.onelogin.sharing.orchestration.prerequisites

import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason

sealed class MissingPrerequisiteReason(val isRecoverable: Boolean = true) {
    data class Incapable(val reason: IncapableReason) :
        MissingPrerequisiteReason(
            isRecoverable = false
        )
    data class NotReady(val reason: NotReadyReason) : MissingPrerequisiteReason()
    data class Unauthorized(val reason: UnauthorizedReason) : MissingPrerequisiteReason()
}
