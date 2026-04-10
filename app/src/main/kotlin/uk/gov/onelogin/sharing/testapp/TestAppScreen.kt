package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.ui.impl.ShareCredential
import uk.gov.onelogin.sharing.ui.impl.VerifyCredential

// DCMAW-19086 Refactor to use state hoisting instead of VM forwarding
@Suppress("ktlint:compose:vm-forwarding-check")
@Composable
fun TestAppScreen(
    credentialPresenter: CredentialPresenter,
    credentialVerifier: CredentialVerifier,
    modifier: Modifier = Modifier
) {
    var destination by rememberSaveable {
        mutableStateOf<CredentialSharingDestination?>(null)
    }

    // Remove verifierPermissionGate once SDK prerequisites screen handles permissions
    var verifierPermissionGate by rememberSaveable { mutableStateOf(false) }

    val sharingDialogVisible by remember {
        derivedStateOf { destination != null || verifierPermissionGate }
    }

    TestAppScreenContent(
        modifier = modifier,
        onOpenHolder = { destination = CredentialSharingDestination.Holder },
        onOpenVerifier = { verifierPermissionGate = true },
        onCloseFlow = {
            when (destination) {
                is CredentialSharingDestination.Holder ->
                    credentialPresenter.orchestrator

                is CredentialSharingDestination.Verifier ->
                    credentialVerifier.orchestrator

                else -> null
            }?.cancel()

            destination = null
            verifierPermissionGate = false
        },
        sharingDialogVisible = sharingDialogVisible,
        content = {
            when {
                // Remove verifierPermissionGate branch once SDK prerequisites handles permissions
                verifierPermissionGate && destination == null -> VerifierPermissionGate {
                    verifierPermissionGate = false
                    destination = CredentialSharingDestination.Verifier
                }

                destination == CredentialSharingDestination.Holder -> ShareCredential(
                    component = credentialPresenter,
                    modifier = Modifier.fillMaxSize()
                )

                destination == CredentialSharingDestination.Verifier -> VerifyCredential(
                    component = credentialVerifier,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun TestAppScreenContent(
    onOpenHolder: () -> Unit,
    onOpenVerifier: () -> Unit,
    onCloseFlow: () -> Unit,
    sharingDialogVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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

                OutlinedButton(onClick = onOpenHolder, modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.holder))
                }

                OutlinedButton(onClick = onOpenVerifier, modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.verifier))
                }
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
        onOpenHolder = {},
        onOpenVerifier = {},
        onCloseFlow = {},
        sharingDialogVisible = false,
        content = {}
    )
}
