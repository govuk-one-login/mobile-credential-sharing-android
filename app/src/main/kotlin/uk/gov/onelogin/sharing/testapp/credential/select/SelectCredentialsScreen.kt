package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.testapp.CREDENTIAL_ITEM_TAG
import uk.gov.onelogin.sharing.testapp.R
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderRootOption

@Composable
@Suppress("LongMethod")
internal fun SelectCredentialsScreen(
    credentials: List<MockCredentialState>,
    modifier: Modifier = Modifier,
    viewModel: SelectHolderCredentialsViewModel = hiltViewModel(),
    onSelectCredential: (MockCredentialState) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("SelectCredentialsScreen")
    }

    val selectedReaderRoot by viewModel.readerRootOption.collectAsStateWithLifecycle()
    val selectedCredentialState by viewModel.selectedCredential.collectAsStateWithLifecycle()

    var isCredentialExpanded by remember { mutableStateOf(false) }
    var isReaderRootExpanded by remember { mutableStateOf(false) }

    val initialCredential = remember(credentials) {
        selectedCredentialState ?: credentials.firstOrNull()
    }

    LaunchedEffect(initialCredential) {
        if (selectedCredentialState == null && initialCredential != null) {
            viewModel.selectCredential(initialCredential)
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.Gray),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = spacingSingle)
        ) {
            Text(
                text = stringResource(R.string.select_credential),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CredentialDropdown(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("credential_menu"),
                credentials = credentials,
                textFieldValue = (selectedCredentialState ?: initialCredential)?.displayName ?: "",
                isCredentialExpanded = isCredentialExpanded,
                onToggleDropdownExpansion = { isCredentialExpanded = it },
                onSelectCredentialOption = {
                    isCredentialExpanded = false
                    viewModel.selectCredential(it)
                }
            )

            ReaderRootDropdown(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("reader_root_menu"),
                textFieldValue = selectedReaderRoot.displayName,
                isReaderRootExpanded = isReaderRootExpanded,
                onToggleDropdownExpansion = { isReaderRootExpanded = it },
                onSelectOption = {
                    isReaderRootExpanded = false
                    viewModel.update(it)
                }
            )

            Button(
                onClick = {
                    val cred = selectedCredentialState ?: initialCredential
                    if (cred != null) {
                        coroutineScope.launch { onSelectCredential(cred) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .testTag("start_sharing_button")
            ) {
                Text("Start Sharing (Holder)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CredentialDropdown(
    credentials: List<MockCredentialState>,
    textFieldValue: String,
    isCredentialExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleDropdownExpansion: (Boolean) -> Unit = {},
    onSelectCredentialOption: (MockCredentialState) -> Unit = {}
) {
    UserDropdownMenu(
        modifier = modifier,
        label = { Text("Select Credential") },
        textFieldValue = textFieldValue,
        isDropdownExpanded = isCredentialExpanded,
        onToggleDropdownExpansion = onToggleDropdownExpansion,
        dropdownMenuContents = {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                credentials.forEach { credential ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .padding(ExposedDropdownMenuDefaults.ItemContentPadding)
                            .testTag(CREDENTIAL_ITEM_TAG),
                        text = { Text(credential.displayName) },
                        onClick = {
                            onToggleDropdownExpansion(false)
                            onSelectCredentialOption(credential)
                        }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderRootDropdown(
    textFieldValue: String,
    isReaderRootExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleDropdownExpansion: (Boolean) -> Unit = {},
    onSelectOption: (ReaderRootOption) -> Unit = {}
) {
    UserDropdownMenu(
        modifier = modifier,
        label = { Text("Trusted Reader Auth Root CA") },
        textFieldValue = textFieldValue,
        isDropdownExpanded = isReaderRootExpanded,
        onToggleDropdownExpansion = onToggleDropdownExpansion,
        dropdownMenuContents = {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                ReaderRootOption.entries.forEach { option ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .padding(ExposedDropdownMenuDefaults.ItemContentPadding)
                            .testTag("reader_root_item"),
                        text = { Text(option.displayName) },
                        onClick = {
                            onToggleDropdownExpansion(false)
                            onSelectOption(option)
                        }
                    )
                }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList", "kotlin:S107")
private fun UserDropdownMenu(
    textFieldValue: String,
    isDropdownExpanded: Boolean,
    dropdownMenuContents: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    textFieldColors: TextFieldColors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    label: @Composable () -> Unit = {},
    onToggleDropdownExpansion: (Boolean) -> Unit = {}
) {
    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = onToggleDropdownExpansion,
        modifier = modifier
    ) {
        OutlinedTextField(
            colors = textFieldColors,
            value = textFieldValue,
            onValueChange = { },
            label = label,
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = isDropdownExpanded
                )
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .testTag("dropdown_text")
        )

        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { onToggleDropdownExpansion(false) },
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                Dp.Hairline,
                MaterialTheme.colorScheme.onSurface
            ),
            matchAnchorWidth = true,
            modifier = Modifier
                .testTag("dropdown_menu")
        ) {
            dropdownMenuContents()
        }
    }
}
