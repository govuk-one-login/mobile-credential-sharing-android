package uk.gov.onelogin.sharing.holder.consent

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@Composable
internal fun HolderConsentScreen(
    viewModel: HolderConsentViewModel = metroViewModel(),
    onGenericError: () -> Unit = {}
) {
    BackHandler(enabled = true) { }

    val state by viewModel.holderSessionState.collectAsStateWithLifecycle()
    val latestOnGenericError by rememberUpdatedState(onGenericError)

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is HolderConsentNavEvents.NavigateToGenericError ->
                    latestOnGenericError()
            }
        }
    }

    val consentState = state as? HolderSessionState.AwaitingUserConsent ?: return

    HolderConsentContent(consentState.request)
}

@Composable
internal fun HolderConsentContent(request: DeviceRequest) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.holder_consent_title),
            style = MaterialTheme.typography.headlineSmall
        )

        request.docRequests.forEach { docRequest ->
            Text(
                text = docRequest.itemsRequest.docType,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            docRequest.itemsRequest.nameSpaces.forEach { (nameSpace, elements) ->
                Text(
                    text = nameSpace,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )

                elements.forEach { (identifier, intentToRetain) ->
                    Text(
                        text = "$identifier — ${
                            stringResource(
                                R.string.holder_consent_intent_to_retain,
                                intentToRetain
                            )
                        }",
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {}) {
                Text(stringResource(R.string.holder_consent_deny))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {}) {
                Text(stringResource(R.string.holder_consent_accept))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
internal fun HolderConsentScreenPreview() {
    HolderConsentContent(
        request = DeviceRequest(
            version = "1.0",
            docRequests = listOf(
                DocRequest(
                    ItemsRequest(
                        docType = "org.iso.18013.5.1.mDL",
                        nameSpaces = mapOf(
                            "org.iso.18013.5.1" to mapOf(
                                "family_name" to false,
                                "document_number" to false,
                                "portrait" to false
                            )
                        )
                    )
                )
            )
        )
    )
}
