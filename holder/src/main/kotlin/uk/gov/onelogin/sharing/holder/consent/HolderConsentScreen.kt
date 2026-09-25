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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation

@Composable
internal fun HolderConsentScreen(viewModel: HolderConsentViewModel = metroViewModel()) {
    BackHandler(enabled = true) { }

    val presentation by viewModel.presentation.collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderConsentScreen")
    }

    presentation?.let {
        HolderConsentContent(
            presentation = it,
            onAccept = viewModel::onAccept,
            onDeny = viewModel::onDeny
        )
    } ?: CircularProgressIndicator()
}

@Suppress("LongMethod")
@Composable
internal fun HolderConsentContent(
    presentation: ConsentPresentation,
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var showDenyDialog by remember { mutableStateOf(false) }

    if (showDenyDialog) {
        DenyConfirmationDialog(
            onConfirmDeny = {
                showDenyDialog = false
                scope.launch { onDeny() }
            },
            onDismiss = { showDenyDialog = false }
        )
    }

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

        presentation.organizationSentence?.let { sentence ->
            Text(
                text = sentence,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        presentation.documents.forEach { document ->
            ConsentDocumentSection(document)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { showDenyDialog = true }) {
                Text(stringResource(R.string.holder_consent_deny))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { scope.launch { onAccept() } }) {
                Text(stringResource(R.string.holder_consent_accept))
            }
        }
    }
}

@Composable
private fun ConsentDocumentSection(document: ConsentDocument) {
    Text(
        text = document.docType,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp)
    )

    document.namespaces.forEach { namespace ->
        ConsentNamespaceSection(namespace)
    }
}

@Composable
private fun ConsentNamespaceSection(namespace: ConsentNamespace) {
    Text(
        text = namespace.nameSpace,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 8.dp)
    )

    namespace.attributes.forEach { attribute ->
        ConsentAttributeRow(attribute)
    }
}

@Composable
private fun ConsentAttributeRow(attribute: ConsentAttribute) {
    Text(
        text = "${attribute.elementIdentifier} — ${
            stringResource(
                R.string.holder_consent_intent_to_retain,
                attribute.intentToRetain
            )
        }",
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    )
}

@Composable
private fun DenyConfirmationDialog(onConfirmDeny: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(text = stringResource(R.string.holder_consent_deny_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = onConfirmDeny) {
                Text(text = stringResource(R.string.holder_consent_deny))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.holder_consent_deny_dialog_dismiss))
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
internal fun HolderConsentScreenPreview() {
    HolderConsentContent(
        presentation = ConsentPresentation(
            documents = listOf(
                ConsentDocument(
                    docType = "org.iso.18013.5.1.mDL",
                    namespaces = listOf(
                        ConsentNamespace(
                            nameSpace = "org.iso.18013.5.1",
                            attributes = listOf(
                                ConsentAttribute("family_name", false),
                                ConsentAttribute("document_number", false),
                                ConsentAttribute("age_over_21", true)
                            )
                        )
                    )
                )
            ),
            organizationName = "Yoti Ltd"
        )
    )
}
