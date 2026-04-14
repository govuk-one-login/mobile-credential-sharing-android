package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.ui.impl.ShareCredential
import uk.gov.onelogin.sharing.ui.impl.VerifyCredential

// DCMAW-19086 Refactor to use state hoisting instead of VM forwarding
@Suppress("ktlint:compose:vm-forwarding-check")
@Composable
fun TestAppScreen(
    presentCredentialSdk: PresentCredentialSdk,
    mockCredentials: List<MockCredential>,
    credentialVerifier: CredentialVerifier,
    modifier: Modifier = Modifier
) {
    var destination by rememberSaveable {
        mutableStateOf<CredentialSharingDestination?>(null)
    }

    var credentialPresenter by remember {
        mutableStateOf<CredentialPresenter?>(null)
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
        onOpenVerifier = { destination = CredentialSharingDestination.Verifier },
        onCloseFlow = {
            when (destination) {
                is CredentialSharingDestination.Holder ->
                    credentialPresenter?.orchestrator

                is CredentialSharingDestination.Verifier ->
                    credentialVerifier.orchestrator

                else -> null
            }?.cancel()

            destination = null
            credentialPresenter = null
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

                destination == CredentialSharingDestination.Verifier -> VerifyCredential(
                    component = credentialVerifier,
                    modifier = Modifier.fillMaxSize()
                )

                else -> {
                    // do nothing with null destination
                }
            }
        }
    )
}

@Composable
fun TestAppScreenContent(
    mockCredentials: List<MockCredential>,
    onOpenHolder: (MockCredential) -> Unit,
    onOpenVerifier: () -> Unit,
    onCloseFlow: () -> Unit,
    sharingDialogVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showCredentialPicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(modifier = modifier) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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

                OutlinedButton(onClick = onOpenVerifier, modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.verifier))
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

            if (sharingDialogVisible) {
                SharingDialog(
                    content = content,
                    onCloseFlow = onCloseFlow
                )
            }
        }
    }
}

@Composable
private fun CredentialPickerDialog(
    mockCredentials: List<MockCredential>,
    onSelect: (MockCredential) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.select_credential),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(mockCredentials, key = { it.id }) { credential ->
                        OutlinedButton(
                            onClick = { onSelect(credential) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag(CREDENTIAL_ITEM_TAG)
                        ) {
                            Text(credential.displayName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharingDialog(onCloseFlow: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onCloseFlow,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag(SHARING_DIALOG_TAG),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                content()

                IconButton(
                    onClick = onCloseFlow,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .testTag(CLOSE_DIALOG_BUTTON_TAG)
                ) {
                    Icon(
                        painter = painterResource(
                            android.R.drawable.ic_menu_close_clear_cancel
                        ),
                        contentDescription = "Close"
                    )
                }
            }
        }
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
