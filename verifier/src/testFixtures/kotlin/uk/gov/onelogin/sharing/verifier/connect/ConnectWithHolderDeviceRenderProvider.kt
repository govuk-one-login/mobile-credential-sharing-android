package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider

/**
 * [TestParameterValuesProvider] implementation for providing different
 * [ConnectWithHolderDeviceRule] functions that render UI.
 */
class ConnectWithHolderDeviceRenderProvider : TestParameterValuesProvider() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun provideValues(context: Context): List<*> = listOf(
        value(ConnectWithHolderDeviceRule::render)
            .withName("via composable render")
    )
}
