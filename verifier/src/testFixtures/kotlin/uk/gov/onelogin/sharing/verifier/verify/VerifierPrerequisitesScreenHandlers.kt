package uk.gov.onelogin.sharing.verifier.verify

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs

class VerifierPrerequisitesScreenHandlers : TestParametersValuesProvider() {

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf<
            Triple<String, VerifierSessionState, VerifyCredentialRule.() -> Unit>
            >(
            Triple(
                "Called `onPreflight` lambda",
                VerifierSessionStateStubs.preflightEmptyPermissions,
                VerifyCredentialRule::assertHasNavigatedToPreflight
            ),
            Triple(
                "Called `onNavigateToScanner` lambda",
                VerifierSessionState.ReadyToScan,
                VerifyCredentialRule::assertHasNavigatedToScanner
            ),
            Triple(
                "Called `onUnrecoverableError` lambda",
                VerifierSessionStateStubs.userJourneyFailure,
                VerifyCredentialRule::assertHasUnrecoverableError
            )
        ).map { (name, state, handlerAssertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("state", state)
                .addParameter("handlerAssertion", handlerAssertion)
                .build()
        }
}
