package uk.gov.onelogin.sharing.prerequisites

interface MissingPrerequisite : Recoverable, Actionable<PrerequisiteAction> {
    val prerequisite: Prerequisite
}
