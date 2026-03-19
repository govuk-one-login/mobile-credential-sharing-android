package uk.gov.onelogin.sharing.holder.prerequisites

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.cryptoService.engagement.EngagementGeneratorStub.BASE64_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.preflightEmptyPermissions

class HolderPrerequisitesScreenStates : TestParametersValuesProvider() {

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf<
            Triple<String, HolderSessionState, HolderPrerequisitesScreenRule.() -> Unit>
            >(
            Triple(
                "Informs the User that prerequisite checks are happening",
                HolderSessionState.NotStarted
            ) { assertNotStartedTextIsDisplayed() },
            Triple(
                "Didn't successfully complete prerequisite checks",
                preflightEmptyPermissions
            ) { assertPreflightTextIsDisplayed() },
            Triple(
                "Successfully completed checks",
                HolderSessionState.ReadyToPresent
            ) { assertReadyToPresentTextIsDisplayed() },
            Triple(
                "Successfully generated QR code content",
                HolderSessionState.PresentingEngagement(
                    BASE64_ENCODED_DEVICE_ENGAGEMENT
                )
            ) { assertPresentingEngagementTextIsDisplayed() }

        ).map { (name, state, composeTestRuleAssertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("state", state)
                .addParameter("composeTestRuleAssertion", composeTestRuleAssertion)
                .build()
        }
}
