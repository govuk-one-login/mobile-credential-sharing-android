package uk.gov.onelogin.sharing.orchestration.session

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite

sealed class SessionErrorReason {
    data class UnrecoverableThrowable(val exception: Throwable) : SessionErrorReason()

    data class UnrecoverablePrerequisite(
        val unrecoverablePrerequisites: List<MissingPrerequisite>
    ) : SessionErrorReason(),
        Iterable<MissingPrerequisite> by unrecoverablePrerequisites {
        constructor(
            vararg unrecoverablePrerequisites: MissingPrerequisite
        ) : this(
            unrecoverablePrerequisites.toList()
        )
    }
}
