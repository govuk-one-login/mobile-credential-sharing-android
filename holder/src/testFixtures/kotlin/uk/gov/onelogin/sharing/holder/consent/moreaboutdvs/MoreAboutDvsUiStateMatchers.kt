package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsViewModel.MoreAboutDvsUiState
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation

object MoreAboutDvsUiStateMatchers {

    fun isLoading(): Matcher<in MoreAboutDvsUiState> = instanceOf(
        MoreAboutDvsUiState.Loading::class.java
    )

    fun isLoaded(): Matcher<in MoreAboutDvsUiState> = instanceOf(
        MoreAboutDvsUiState.Loaded::class.java
    )

    fun hasOrganizationName(expected: String) = hasOrganizationName(equalTo(expected))

    fun hasOrganizationName(matcher: Matcher<in String>): Matcher<in MoreAboutDvsUiState> =
        MoreAboutDvsUiStateMatcher(matcher) {
            (it as? MoreAboutDvsUiState.Loaded)?.organizationName
        }

    fun hasPrivacyPolicyUrl(expected: String) = hasPrivacyPolicyUrl(equalTo(expected))

    fun hasPrivacyPolicyUrl(matcher: Matcher<in String>): Matcher<in MoreAboutDvsUiState> =
        MoreAboutDvsUiStateMatcher(matcher) {
            (it as? MoreAboutDvsUiState.Loaded)?.privacyPolicyUrl
        }

    fun hasUiState(presentation: ConsentPresentation): Matcher<in MoreAboutDvsUiState> = allOf(
        hasOrganizationName(presentation.normalisedOrganizationName),
        hasPrivacyPolicyUrl(presentation.privacyPolicyUrl)
    )

    private class MoreAboutDvsUiStateMatcher<T>(
        private val matcher: Matcher<in T>,
        private val transformer: (MoreAboutDvsUiState?) -> T?
    ) : TypeSafeMatcher<MoreAboutDvsUiState>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: MoreAboutDvsUiState?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: MoreAboutDvsUiState?): Boolean =
            matcher.matches(transformer(item))
    }
}
