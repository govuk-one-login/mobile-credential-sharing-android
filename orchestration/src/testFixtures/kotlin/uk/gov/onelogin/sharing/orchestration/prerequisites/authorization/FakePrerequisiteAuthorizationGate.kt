package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer

data class FakePrerequisiteAuthorizationGate(
    private val result: Map<Prerequisite, MissingPrerequisiteReason.Unauthorized?>
) : PrerequisiteGateLayer.Authorization {

    constructor(
        result: MissingPrerequisiteReason.Unauthorized? = null
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkAuthorization(
        prerequisite: Prerequisite
    ): MissingPrerequisiteReason.Unauthorized? = result[prerequisite]
}
