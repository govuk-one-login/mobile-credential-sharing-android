package uk.gov.onelogin.sharing.security.secureArea.privatekey

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs

class KeyPairToExceptions : TestParametersValuesProvider() {
    private val inputParameters = listOf(
        Triple(
            "Null Keys",
            KeyPairGeneratorStubs.keyPairWithNullEntries,
            NullPointerException::class.java
        ),
        Triple(
            "Invalid Key class (RSA)",
            KeyPairGeneratorStubs.rsaKeyPair,
            ClassCastException::class.java
        )
    )

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        inputParameters.map { (name, keyPair, expectedExceptionClass) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("keyPair", keyPair)
                .addParameter("expectedExceptionClass", expectedExceptionClass)
                .build()
        }
}
