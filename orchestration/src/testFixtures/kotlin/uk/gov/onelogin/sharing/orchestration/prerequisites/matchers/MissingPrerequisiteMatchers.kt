package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

object MissingPrerequisiteMatchers {
    fun hasPrerequisite(expected: Prerequisite): Matcher<in MissingPrerequisite> =
        hasPrerequisite(equalTo(expected))

    fun hasPrerequisite(matcher: Matcher<in Prerequisite>): Matcher<in MissingPrerequisite> =
        HasPrerequisite(matcher)

    fun hasReason(expected: MissingPrerequisiteReason): Matcher<in MissingPrerequisite> =
        hasReason(equalTo(expected))

    fun hasReason(matcher: Matcher<in MissingPrerequisiteReason>): Matcher<in MissingPrerequisite> =
        HasMissingPrerequisiteReason(matcher)
}
