package uk.gov.onelogin.sharing.verifier.connect

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider

/**
 * [TestParameterValuesProvider] implementation for providing different
 * [ConnectWithHolderDeviceRule] functions that render UI.
 */
class ConnectWithHolderDeviceRenderProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<*> = listOf(
        value(ConnectWithHolderDeviceRule::renderPreview)
            .withName("via Preview render"),
        value(ConnectWithHolderDeviceRule::render)
            .withName("via composable render")
    )
}
