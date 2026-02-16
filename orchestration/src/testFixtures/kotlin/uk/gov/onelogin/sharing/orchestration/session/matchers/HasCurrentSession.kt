package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory

class HasCurrentSession<Session : Any>(private val matcher: Matcher<in Session>) :
    TypeSafeMatcher<FakeSessionFactory<Session>>() {
    override fun describeTo(description: Description?) {
        matcher.describeTo(description)
    }

    override fun describeMismatchSafely(
        item: FakeSessionFactory<Session>?,
        mismatchDescription: Description?
    ) {
        matcher.describeMismatch(item?.getCurrentSession(), mismatchDescription)
    }

    override fun matchesSafely(item: FakeSessionFactory<Session>?): Boolean =
        matcher.matches(item?.getCurrentSession())
}
