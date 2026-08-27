package uk.gov.onelogin.sharing.testapp.credential.attribute.select

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.testapp.ATTRIBUTE_GROUP_ITEM_TAG
import uk.gov.onelogin.sharing.testapp.R
import uk.gov.onelogin.sharing.testapp.VERIFY_CREDENTIAL_BUTTON_TAG

@Composable
internal fun SelectCredentialAttributesScreen(
    modifier: Modifier = Modifier,
    viewModel: SelectCredentialsViewModel = hiltViewModel(),
    onSelectAttributeGroup: (AttributeGroup) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("SelectCredentialAttributesScreen")
    }

    val selectedAttributeGroup by viewModel.verifierAttributeOption.collectAsStateWithLifecycle()
    var isAttributeGroupExpanded by remember { mutableStateOf(false) }

    val selectedReaderAuth: ReaderAuthOption by viewModel.readerAuthOption
        .collectAsStateWithLifecycle()
    var isReaderAuthExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.Gray),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacingSingle)
        ) {
            UserInputs(
                selectedAttributeGroup = selectedAttributeGroup,
                isAttributeGroupExpanded = isAttributeGroupExpanded,
                selectedReaderAuth = selectedReaderAuth,
                isReaderAuthExpanded = isReaderAuthExpanded,
                onToggleAttributeGroupDropdown = { isAttributeGroupExpanded = it },
                onToggleReaderAuthOptionDropdown = { isReaderAuthExpanded = it },
                onSelectAttributeOption = {
                    isAttributeGroupExpanded = false
                    viewModel.update(it)
                },
                onSelectReaderAuthOption = {
                    isReaderAuthExpanded = false
                    viewModel.update(it)
                },
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        onSelectAttributeGroup(selectedAttributeGroup.attributeGroup)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .testTag(VERIFY_CREDENTIAL_BUTTON_TAG)
            ) {
                Text(stringResource(R.string.verify_credential))
            }
        }
    }
}

@Composable
@Suppress("LongParameterList", "kotlin:S107")
private fun UserInputs(
    selectedAttributeGroup: VerifierAttributeOption,
    isAttributeGroupExpanded: Boolean,
    selectedReaderAuth: ReaderAuthOption,
    isReaderAuthExpanded: Boolean,
    onSelectAttributeOption: (VerifierAttributeOption) -> Unit,
    onToggleAttributeGroupDropdown: (Boolean) -> Unit,
    onSelectReaderAuthOption: (ReaderAuthOption) -> Unit,
    onToggleReaderAuthOptionDropdown: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AttributeGroupDropdown(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("attribute_group_menu"),
            textFieldValue = selectedAttributeGroup.displayName,
            isAttributeGroupExpanded = isAttributeGroupExpanded,
            onToggleDropdownExpansion = onToggleAttributeGroupDropdown,
            onSelectAttributeOption = onSelectAttributeOption
        )

        ReaderAuthDropdown(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reader_auth_menu"),
            textFieldValue = selectedReaderAuth.displayName,
            isAttributeGroupExpanded = isReaderAuthExpanded,
            onToggleDropdownExpansion = onToggleReaderAuthOptionDropdown,
            onSelectOption = onSelectReaderAuthOption
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttributeGroupDropdown(
    textFieldValue: String,
    isAttributeGroupExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleDropdownExpansion: (Boolean) -> Unit = {},
    onSelectAttributeOption: (VerifierAttributeOption) -> Unit = {},
) {
    UserDropdownMenu(
        modifier = modifier,
        label = { Text("Attribute group") },
        textFieldValue = textFieldValue,
        isDropdownExpanded = isAttributeGroupExpanded,
        onToggleDropdownExpansion = onToggleDropdownExpansion,
        dropdownMenuContents = {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                VerifierAttributeOption.entries
                    .sortedBy(VerifierAttributeOption::displayName)
                    .forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier
                                .padding(ExposedDropdownMenuDefaults.ItemContentPadding)
                                .testTag(ATTRIBUTE_GROUP_ITEM_TAG),
                            text = { Text(option.displayName) },
                            onClick = {
                                onToggleDropdownExpansion(false)
                                onSelectAttributeOption(option)
                            }
                        )
                    }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderAuthDropdown(
    textFieldValue: String,
    isAttributeGroupExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleDropdownExpansion: (Boolean) -> Unit = {},
    onSelectOption: (ReaderAuthOption) -> Unit = {},
) {
    UserDropdownMenu(
        modifier = modifier,
        label = { Text("Reader Auth certificate") },
        textFieldValue = textFieldValue,
        isDropdownExpanded = isAttributeGroupExpanded,
        onToggleDropdownExpansion = onToggleDropdownExpansion,
        dropdownMenuContents = {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                ReaderAuthOption.entries
                    .sortedBy(ReaderAuthOption::displayName)
                    .forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier
                                .padding(ExposedDropdownMenuDefaults.ItemContentPadding)
                                .testTag("reader_auth_item"),
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
    onToggleDropdownExpansion: (Boolean) -> Unit = {},
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
