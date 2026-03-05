package uk.gov.onelogin.sharing.orchestration.prerequisites

import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason

sealed class PrerequisiteResponse {
    data object MeetsPrerequisites : PrerequisiteResponse()
    data class Incapable(val reason: IncapableReason) : PrerequisiteResponse()
    data class NotReady(val reason: NotReadyReason) : PrerequisiteResponse()
    data class Unauthorized(val reason: UnauthorizedReason) : PrerequisiteResponse()
}
