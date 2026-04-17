package uk.gov.onelogin.sharing.orchestration.prerequisites

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
