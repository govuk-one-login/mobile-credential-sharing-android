package uk.gov.onelogin.sharing.models.mdoc.engagment

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

internal class HasVersion(private val matcher: Matcher<in String>) :
    TypeSafeMatcher<DeviceEngagementDto>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: DeviceEngagementDto?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.version, mismatchDescription)

    override fun matchesSafely(item: DeviceEngagementDto?): Boolean = matcher.matches(item?.version)
}
