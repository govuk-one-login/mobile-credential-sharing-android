package uk.gov.onelogin.sharing.prerequisites

import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteGate

class StubPrerequisiteGate(private val results: List<MissingPrerequisite>) : PrerequisiteGate {

    constructor(
        vararg result: MissingPrerequisite
    ) : this(
        results = result.toList()
    )

    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisite> = results.filter { stubResult ->
        stubResult.prerequisite in prerequisites
    }
}
