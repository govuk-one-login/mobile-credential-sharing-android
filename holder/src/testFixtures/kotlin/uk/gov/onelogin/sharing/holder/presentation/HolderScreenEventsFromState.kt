package uk.gov.onelogin.sharing.holder.presentation

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.AwaitingUserConsent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.session.SessionError

class HolderScreenEventsFromState : TestParametersValuesProvider() {

    private val inputs: List<Triple<String, HolderSessionState, HolderScreenEvents>> = listOf(
        Triple(
            "Awaiting user consent",
            AwaitingUserConsent(
                DeviceRequest(
                    "version",
                    listOf()
                )
            ),
            HolderScreenEvents.AwaitingUserContent
        ),
        Triple(
            "Conection error",
            Failed(
                SessionError(
                    "",
                    BluetoothDisconnectedException("", Exception())
                )
            ),
            HolderScreenEvents.NavigateToBluetoothError(
                BluetoothSessionError.BluetoothConnectionError
            )
        ),
        Triple(
            "Generic error: DeviceRequestDecodingException",
            Failed(
                SessionError(
                    "",
                    DeviceRequestDecodingException("")
                )
            ),
            HolderScreenEvents.NavigateToGenericError
        ),
        Triple(
            "Generic error: Unknown",
            Failed(
                SessionError(
                    "",
                    Exception()
                )
            ),
            HolderScreenEvents.NavigateToGenericError
        )
    )

    override fun provideValues(
        context: Context?
    ): List<TestParameters.TestParametersValues?>? = inputs.map { (name, state, event) ->
        TestParameters.TestParametersValues.builder()
            .name(name)
            .addParameter("state", state)
            .addParameter("expected", event)
            .build()
    }
}