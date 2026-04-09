package uk.gov.onelogin.sharing.holder.prerequisites

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.cryptoService.engagement.EngagementGeneratorStub
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

class HolderPrerequisitesScreenHandlers : TestParametersValuesProvider() {

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf<
            Triple<String, HolderSessionState, HolderPrerequisitesScreenRule.() -> Boolean>
            >(
            Triple(
                "Called `onPreflight` lambda",
                HolderSessionStateStubs.preflightEmptyPermissions
            ) { hasHandledPreflight },
            Triple(
                "Called `onPresentingEngagement` lambda",
                HolderSessionState.PresentingEngagement(
                    EngagementGeneratorStub.BASE64_ENCODED_DEVICE_ENGAGEMENT
                )
            ) { hasPresentedEngagement },
            Triple(
                "Called `onUnrecoverableError` lambda",
                HolderSessionStateStubs.userJourneyFailure
            ) { hasUnrecoverableError }
        ).map { (name, state, handlerAssertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("state", state)
                .addParameter("handlerAssertion", handlerAssertion)
                .build()
        }
}
