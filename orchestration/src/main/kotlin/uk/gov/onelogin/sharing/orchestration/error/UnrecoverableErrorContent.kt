package uk.gov.onelogin.sharing.orchestration.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@Composable
fun UnrecoverableErrorContent(
    failureState: SessionError,
    modifier: Modifier = Modifier,
    onExitJourney: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacingSingle)
    ) {
        Text(transformReasonToTitle(failureState.reason))
        Text(failureState.message)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { coroutineScope.launch { onExitJourney() } }
        ) {
            Text("Exit journey")
        }
    }
}

/**
 * Suppresses `CyclomaticComplexMethod` due to the amount of [SessionErrorReason] derivatives.
 */
@Suppress("CyclomaticComplexMethod")
private fun transformReasonToTitle(reason: SessionErrorReason): String = when (reason) {
    SessionErrorReason.CannotBuildSessionEstablishment -> "CannotBuildSessionEstablishment"
    SessionErrorReason.CannotDecryptDeviceResponse -> "CannotDecryptDeviceResponse"
    SessionErrorReason.CannotEncryptDeviceRequest -> "CannotEncryptDeviceRequest"
    is SessionErrorReason.CannotProcessEngagement -> "CannotProcessEngagement"
    SessionErrorReason.CannotSendMessage -> "CannotSendMessage"
    is SessionErrorReason.DeviceRequestProcessingError -> "DeviceRequestProcessingError"
    SessionErrorReason.DocumentNotReturned -> "DocumentNotReturned"
    is SessionErrorReason.InvalidBluetoothState -> "InvalidBluetoothState"
    SessionErrorReason.InvalidSessionDataPayload -> "InvalidSessionDataPayload"
    SessionErrorReason.MissingCryptoContext -> "MissingCryptoContext"
    SessionErrorReason.PeerTermination -> "PeerTermination"
    SessionErrorReason.ServiceUuidNotFound -> "ServiceUuidNotFound"
    is SessionErrorReason.StatusError -> "StatusError"
    is SessionErrorReason.UnrecoverablePrerequisite -> "UnrecoverablePrerequisite"
    is SessionErrorReason.UnrecoverableThrowable -> "UnrecoverableThrowable"
    is SessionErrorReason.UnsupportedQrCodeFormat -> "UnsupportedQrCodeFormat"
    is SessionErrorReason.UnverifiableDocument -> "UnverifiableDocument"
    is SessionErrorReason.AgeOverNNRequestLimit -> "AgeOverNNRequestLimit"
}
