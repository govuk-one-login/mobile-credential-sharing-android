package uk.gov.onelogin.sharing.orchestration.session

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2

sealed class SessionErrorReason {
    data class UnrecoverableThrowable(val exception: Throwable) : SessionErrorReason()

    data class UnrecoverablePrerequisite(
        val unrecoverablePrerequisites: List<MissingPrerequisiteV2>
    ) : SessionErrorReason(),
        Iterable<MissingPrerequisiteV2> by unrecoverablePrerequisites {
        constructor(
            vararg unrecoverablePrerequisites: MissingPrerequisiteV2
        ) : this(
            unrecoverablePrerequisites.toList()
        )
    }
}
