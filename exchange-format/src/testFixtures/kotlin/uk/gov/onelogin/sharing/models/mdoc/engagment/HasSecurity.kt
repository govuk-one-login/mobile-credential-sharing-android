package uk.gov.onelogin.sharing.models.mdoc.engagment

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto

internal class HasSecurity(private val matcher: Matcher<in SecurityDto>) :
    TypeSafeMatcher<DeviceEngagementDto>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: DeviceEngagementDto?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.security, mismatchDescription)

    override fun matchesSafely(item: DeviceEngagementDto?): Boolean =
        matcher.matches(item?.security)
}
