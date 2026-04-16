package uk.gov.onelogin.sharing.orchestration.prerequisites

/**
 * Abstraction for performing various forms of checks based on [Prerequisite] input.
 *
 * @sample PrerequisiteGateImpl
 */
fun interface PrerequisiteGate {
    fun evaluatePrerequisites(prerequisites: Iterable<Prerequisite>): List<MissingPrerequisite>

    fun evaluatePrerequisites(vararg prerequisites: Prerequisite): List<MissingPrerequisite> =
        evaluatePrerequisites(
            prerequisites.toList()
        )
}
