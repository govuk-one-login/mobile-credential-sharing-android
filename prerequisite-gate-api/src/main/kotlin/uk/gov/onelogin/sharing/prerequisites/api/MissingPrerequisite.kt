package uk.gov.onelogin.sharing.prerequisites.api

interface MissingPrerequisite :
    Recoverable,
    Actionable<PrerequisiteAction> {
    val prerequisite: Prerequisite
}
