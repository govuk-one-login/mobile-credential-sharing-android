package uk.gov.onelogin.sharing.security.cbor.decoders

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.security.cbor.deriveSessionTranscript

class SessionTranscriptDecoders : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?> =
        listOf<Pair<String, (String, ByteArray, Logger) -> ByteArray>>(
            "Decoder entry function"
                to { deviceEngagement, eReaderKeyTagged, logger ->
                    deriveSessionTranscript(
                        cborBase64Url = deviceEngagement,
                        eReaderKeyTagged = eReaderKeyTagged,
                        logger = logger
                    )
                },
            "SessionTranscriptDecoderImpl implementation"
                to { deviceEngagement, eReaderKeyTagged, logger ->
                    SessionTranscriptDecoderImpl(logger).deriveSessionTranscript(
                        cborBase64Url = deviceEngagement,
                        taggedEReaderKey = eReaderKeyTagged
                    )
                }
        ).map { (testName, functionUnderTest) ->
            TestParameters.TestParametersValues.builder()
                .name(testName)
                .addParameter("decoder", functionUnderTest)
                .build()
        }
}
