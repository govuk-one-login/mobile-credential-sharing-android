package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

data class FakePrerequisiteAuthorizationGate(
    private val result: Map<Prerequisite, PrerequisiteResponse.Unauthorized?>
) : PrerequisiteGateLayer.Authorization {

    constructor(
        result: PrerequisiteResponse.Unauthorized? = null
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkAuthorization(
        prerequisite: Prerequisite
    ): PrerequisiteResponse.Unauthorized? = result[prerequisite]
}
