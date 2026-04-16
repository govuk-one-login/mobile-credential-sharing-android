package uk.gov.onelogin.sharing.orchestration.prerequisites

/**
 * Abstraction for performing various forms of checks based on [Prerequisite] input.
 *
 * @see PrerequisiteGateLayer
 */
fun interface PrerequisiteGate {
    fun evaluatePrerequisites(prerequisites: Iterable<Prerequisite>): List<MissingPrerequisiteV2>

    fun evaluatePrerequisites(vararg prerequisites: Prerequisite): List<MissingPrerequisiteV2> =
        evaluatePrerequisites(
            prerequisites.toList()
        )
}
