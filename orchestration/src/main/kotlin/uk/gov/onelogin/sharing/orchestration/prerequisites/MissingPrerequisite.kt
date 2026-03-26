package uk.gov.onelogin.sharing.orchestration.prerequisites

data class MissingPrerequisite(
    val prerequisite: Prerequisite,
    val reason: MissingPrerequisiteReason
) {
    val isRecoverable: Boolean = reason.isRecoverable
}
