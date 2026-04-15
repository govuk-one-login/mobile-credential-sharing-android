package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.cert.Certificate
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.ui.impl.ShareCredential
import uk.gov.onelogin.sharing.ui.impl.VerifyCredential

// DCMAW-19086 Refactor to use state hoisting instead of VM forwarding
@Suppress("ktlint:compose:vm-forwarding-check", "LongMethod")
@Composable
fun TestAppScreen(
    presentCredentialSdk: PresentCredentialSdk,
    mockCredentials: List<MockCredential>,
    verifyCredentialSdk: VerifyCredentialSdk,
    modifier: Modifier = Modifier
) {
    var destination by rememberSaveable {
        mutableStateOf<CredentialSharingDestination?>(null)
    }

    var credentialPresenter by remember {
        mutableStateOf<CredentialPresenter?>(null)
    }

    var credentialVerifier by remember {
        mutableStateOf<CredentialVerifier?>(null)
    }

    val sharingDialogVisible by remember {
        derivedStateOf { destination != null }
    }

    TestAppScreenContent(
        modifier = modifier,
        mockCredentials = mockCredentials,
        onOpenHolder = { credential ->
            credentialPresenter = presentCredentialSdk
                .presenter(SampleCredentialProvider(credential))
            destination = CredentialSharingDestination.Holder
        },
        onOpenVerifier = { attributeOption ->
            val verificationRequest = VerificationRequest.typed(
                documentType = DocumentType.Mdl,
                attributeGroup = attributeOption.attributeGroup
            )
            val trustedCertificates: List<Certificate> = emptyList()
            credentialVerifier = verifyCredentialSdk.verifier(
                VerifierConfig(
                    verificationRequest = verificationRequest,
                    trustedCertificates = trustedCertificates
                )
            )
            destination = CredentialSharingDestination.Verifier
        },
        onCloseFlow = {
            when (destination) {
                is CredentialSharingDestination.Holder ->
                    credentialPresenter?.orchestrator

                is CredentialSharingDestination.Verifier ->
                    credentialVerifier?.orchestrator

                else -> null
            }?.cancel()

            destination = null
            credentialPresenter = null
            credentialVerifier = null
        },
        sharingDialogVisible = sharingDialogVisible,
        content = {
            when {
                destination == CredentialSharingDestination.Holder -> credentialPresenter?.let {
                    ShareCredential(
                        component = it,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                destination == CredentialSharingDestination.Verifier -> credentialVerifier?.let {
                    VerifyCredential(
                        component = it,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // do nothing with null destination
                }
            }
        }
    )
}

@Composable
private fun TestAppScreenContent(
    mockCredentials: List<MockCredential>,
    onOpenHolder: (MockCredential) -> Unit,
    onOpenVerifier: (VerifierAttributeOption) -> Unit,
    onCloseFlow: () -> Unit,
    sharingDialogVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showCredentialPicker by rememberSaveable { mutableStateOf(false) }
    var showAttributeGroupPicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(modifier = modifier) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.test_screen_title),
                modifier = Modifier.padding(bottom = 64.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = { showCredentialPicker = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(stringResource(R.string.holder))
            }

            OutlinedButton(
                onClick = { showAttributeGroupPicker = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(stringResource(R.string.verifier))
            }
        }
    }

    if (showCredentialPicker) {
        CredentialPickerDialog(
            mockCredentials = mockCredentials,
            onSelect = { credential ->
                showCredentialPicker = false
                onOpenHolder(credential)
            },
            onDismiss = { showCredentialPicker = false }
        )
    }

    if (showAttributeGroupPicker) {
        AttributeGroupPickerDialog(
            onSelect = { option ->
                showAttributeGroupPicker = false
                onOpenVerifier(option)
            },
            onDismiss = { showAttributeGroupPicker = false }
        )
    }

    if (sharingDialogVisible) {
        SharingDialog(
            content = content,
            onCloseFlow = onCloseFlow
        )
    }
}

@Preview
@Composable
private fun TestAppScreenContentPreview() {
    TestAppScreenContent(
        mockCredentials = listOf(
            MockCredential(
                id = "1",
                displayName = "Jane Doe",
                rawCredential = byteArrayOf(),
                privateKey = byteArrayOf()
            )
        ),
        onOpenHolder = {},
        onOpenVerifier = {},
        onCloseFlow = {},
        sharingDialogVisible = false,
        content = {}
    )
}
