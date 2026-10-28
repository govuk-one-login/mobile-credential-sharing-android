package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

@Composable
internal fun HolderConsentScreen(viewModel: HolderConsentViewModel = metroViewModel()) {
    val presentation by viewModel.presentation.collectAsStateWithLifecycle()
    val showPrivacyPolicy by viewModel.showPrivacyPolicy.collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderConsentScreen")
    }

    presentation?.let {
        HolderConsentContent(
            state = it,
            showPrivacyPolicy = showPrivacyPolicy,
            onShowPrivacyPolicy = viewModel::onShowPrivacyPolicy,
            onClosePrivacyPolicy = viewModel::onClosePrivacyPolicy,
            onAccept = viewModel::onAccept,
            onDeny = viewModel::onDeny
        )
    } ?: CircularProgressIndicator()
}

@Suppress("LongMethod")
@Composable
internal fun HolderConsentContent(
    state: HolderConsentUiState,
    showPrivacyPolicy: Boolean = false,
    onShowPrivacyPolicy: () -> Unit = {},
    onClosePrivacyPolicy: () -> Unit = {},
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var showDenyDialog by remember { mutableStateOf(false) }

    val privacyPolicyUrl = state.privacyPolicyUrl
    if (showPrivacyPolicy && privacyPolicyUrl != null) {
        PrivacyPolicyWebView(
            url = privacyPolicyUrl,
            onClose = onClosePrivacyPolicy
        )
        return
    }

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

        state.card?.let { card ->
            Spacer(modifier = Modifier.height(16.dp))
            AgreeToShareCard(
                card = card,
                organizationName = state.normalisedOrganizationName,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        state.normalisedOrganizationName?.let { orgName ->
            Text(
                text = stringResource(R.string.holder_consent_organization_sentence, orgName),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (privacyPolicyUrl != null) {
            PrivacyPolicyLink(onClick = onShowPrivacyPolicy)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                enabled = !state.isBusy,
                onClick = { showDenyDialog = true }
            ) {
                Text(stringResource(R.string.holder_consent_deny))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = !state.isBusy,
                onClick = { scope.launch { onAccept() } }
            ) {
                Text(stringResource(R.string.holder_consent_accept))
            }
        }
    }
}

@Composable
private fun PrivacyPolicyLink(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.holder_consent_privacy_policy_link),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        ),
        modifier = Modifier
            .padding(top = 8.dp)
            .clickable(onClick = onClick)
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

@PreviewContent
@Composable
internal fun HolderConsentScreenPreview() {
    val card = ConsentCard.from(
        ConsentAttributeResolver.toDisplayAttributes(
            listOf(
                ConsentAttribute("family_name", false, nameSpace = "org.iso.18013.5.1"),
                ConsentAttribute("document_number", false, nameSpace = "org.iso.18013.5.1"),
                ConsentAttribute("age_over_21", true, nameSpace = "org.iso.18013.5.1")
            )
        )
    )
    GdsTheme {
        Surface {
            HolderConsentContent(
                state = HolderConsentUiState(
                    card = card,
                    privacyPolicyUrl = "https://verifier.example/privacy",
                    normalisedOrganizationName = "Organisation Ltd."
                )
            )
        }
    }
}
