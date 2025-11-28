package uk.gov.onelogin.sharing.testapp.destination

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider

/**
 * [TestParameterValuesProvider] implementation that provides
 * [PrimaryTabDestinationData.expectedVerifierMenuItems] for use in parameterized testing.
 */
class VerifierModuleEntriesProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<String> =
        PrimaryTabDestinationData.expectedVerifierMenuItems
}
