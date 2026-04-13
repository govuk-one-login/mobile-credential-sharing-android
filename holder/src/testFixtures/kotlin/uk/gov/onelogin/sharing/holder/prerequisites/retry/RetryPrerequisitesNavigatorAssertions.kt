package uk.gov.onelogin.sharing.holder.prerequisites.retry

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator

private typealias AssertionType = Triple<
    String,
    RetryPrerequisitesNavigator.NavigationEvent,
    RetryHolderPrerequisitesScreenRule.() -> Unit
    >

class RetryPrerequisitesNavigatorAssertions : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf<AssertionType>(
            Triple(
                "Passing prerequisites call 'onPassPrerequisites'",
                RetryPrerequisitesNavigator.NavigationEvent.PassedPrerequisites,
                RetryHolderPrerequisitesScreenRule::assertHasPassedPrerequisites
            ),
            Triple(
                "Obtaining an unrecoverable error calls 'onUnrecoverableError'",
                RetryPrerequisitesNavigator.NavigationEvent.UnrecoverableError,
                RetryHolderPrerequisitesScreenRule::assertHasUnrecoverableError
            )
        ).map { (name, event, assertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("event", event)
                .addParameter("assertion", assertion)
                .build()
        }
}
