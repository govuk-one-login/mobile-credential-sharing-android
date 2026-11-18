package uk.gov.onelogin.sharing.holder.consent

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentCard

object HolderConsentUiStateMatchers {

    fun hasConsentCard(expected: ConsentCard) = hasConsentCard(equalTo(expected))

    fun hasConsentCard(matcher: Matcher<in ConsentCard>): Matcher<in HolderConsentUiState> =
        HolderConsentUiStateMatcher(matcher) {
            (it as? HolderConsentUiState.Loaded)?.card
        }

    fun hasNormalisedOrganisationName(expected: String) =
        hasNormalisedOrganisationName(equalTo(expected))

    fun hasNormalisedOrganisationName(
        matcher: Matcher<in String>
    ): Matcher<in HolderConsentUiState> = HolderConsentUiStateMatcher(matcher) {
        (it as? HolderConsentUiState.Loaded)?.normalisedOrganizationName
    }

    fun hasOrganisationName(expected: String) = hasOrganisationName(equalTo(expected))

    fun hasOrganisationName(matcher: Matcher<in String>): Matcher<in HolderConsentUiState> =
        HolderConsentUiStateMatcher(matcher) {
            (it as? HolderConsentUiState.Loaded)?.organizationName
        }

    fun hasPrivacyPolicyUrl(expected: String) = hasPrivacyPolicyUrl(equalTo(expected))

    fun hasPrivacyPolicyUrl(matcher: Matcher<in String>): Matcher<in HolderConsentUiState> =
        HolderConsentUiStateMatcher(matcher) {
            (it as? HolderConsentUiState.Loaded)?.privacyPolicyUrl
        }

    fun isBusy(expected: Boolean = true): Matcher<in HolderConsentUiState> =
        HolderConsentUiStateMatcher(
            equalTo(expected)
        ) {
            (it as? HolderConsentUiState.Loaded)?.isBusy
        }

    fun isLoading(): Matcher<in HolderConsentUiState> = instanceOf(
        HolderConsentUiState.Loading::class.java
    )

    fun isLoaded(): Matcher<in HolderConsentUiState> = instanceOf(
        HolderConsentUiState.Loaded::class.java
    )

    fun isShowingMoreAboutDvs(expected: Boolean = true): Matcher<in HolderConsentUiState> =
        HolderConsentUiStateMatcher(
            equalTo(expected)
        ) {
            (it as? HolderConsentUiState.Loaded)?.shouldShowMoreAboutDvs
        }

    private class HolderConsentUiStateMatcher<T>(
        private val matcher: Matcher<T>,
        private val transformer: (HolderConsentUiState?) -> T?
    ) : TypeSafeMatcher<HolderConsentUiState>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: HolderConsentUiState?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: HolderConsentUiState?): Boolean =
            matcher.matches(transformer(item))
    }
}
