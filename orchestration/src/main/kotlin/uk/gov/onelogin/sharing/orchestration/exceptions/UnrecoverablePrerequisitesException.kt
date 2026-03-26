package uk.gov.onelogin.sharing.orchestration.exceptions

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite

data class UnrecoverablePrerequisitesException(val prerequisiteReason: List<MissingPrerequisite>) :
    IllegalStateException()
