package uk.gov.onelogin.sharing.core.accessibility

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import kotlin.reflect.KClass

object AccessibilityViewCheckResultMatchers {

    fun hasSourceCheckClass(
        klass: KClass<out AccessibilityCheck>
    ) = hasSourceCheckClass(klass.java)

    fun hasSourceCheckClass(
        clazz: Class<out AccessibilityCheck>
    ) = hasSourceCheckClass(equalTo(clazz))

    fun hasSourceCheckClass(
        matcher: Matcher<in Class<out AccessibilityCheck>>
    ): Matcher<in AccessibilityViewCheckResult> = AccessibilityViewCheckResultMatcher(matcher) {
        it?.sourceCheckClass
    }

    private class AccessibilityViewCheckResultMatcher<T>(
        private val matcher: Matcher<in T>,
        private val transformer: (AccessibilityViewCheckResult?) -> T?
    ) : TypeSafeMatcher<AccessibilityViewCheckResult>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: AccessibilityViewCheckResult?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(
            item: AccessibilityViewCheckResult?
        ): Boolean = matcher.matches(transformer(item))
    }
}