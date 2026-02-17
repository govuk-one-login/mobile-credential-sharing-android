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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi

@Composable
fun TestAppScreen(
    ui: CredentialSharingUi,
    sdk: CredentialSharingSdk,
    modifier: Modifier = Modifier
) {
    var destination by rememberSaveable {
        mutableStateOf<CredentialSharingDestination?>(null)
    }

    val sharingDialogVisible by remember {
        derivedStateOf { destination != null }
    }

    TestAppScreenContent(
        modifier = modifier,
        onOpenHolder = { destination = CredentialSharingDestination.HolderRoot },
        onOpenVerifier = { destination = CredentialSharingDestination.VerifierRoot },
        onOpenDevMenu = { destination = CredentialSharingDestination.DevMenu },
        onCloseFlow = { destination = null },
        sharingDialogVisible = sharingDialogVisible,
        content = {
            destination?.let { sharingDestination ->
                ui.Render(
                    sdk = sdk,
                    startDestination = sharingDestination,
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
    onOpenDevMenu: () -> Unit,
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
                    text = "Credential Sharing Test App",
                    modifier = Modifier.padding(bottom = 64.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(onClick = onOpenHolder, modifier = Modifier.padding(16.dp)) {
                    Text("Holder")
                }

                OutlinedButton(onClick = onOpenVerifier, modifier = Modifier.padding(16.dp)) {
                    Text("Verifier")
                }

                OutlinedButton(onClick = onOpenDevMenu, modifier = Modifier.padding(16.dp)) {
                    Text("Dev Menu")
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
                .padding(16.dp),
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
                    modifier = Modifier.align(Alignment.TopStart)
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
        onOpenDevMenu = {},
        onCloseFlow = {},
        sharingDialogVisible = false,
        content = {}
    )
}
