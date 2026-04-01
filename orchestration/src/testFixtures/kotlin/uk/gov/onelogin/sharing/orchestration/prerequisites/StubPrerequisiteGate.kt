package uk.gov.onelogin.sharing.orchestration.prerequisites

class StubPrerequisiteGate(private val results: List<MissingPrerequisiteV2>) : PrerequisiteGate.V2 {

    constructor(
        vararg result: MissingPrerequisiteV2
    ) : this(
        results = result.toList()
    )

    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisiteV2> = results.filter { stubResult ->
        stubResult.prerequisite in prerequisites
    }
}
