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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    onSelectAttributeGroup: (AttributeGroup) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("SelectCredentialAttributesScreen")
    }

    var selected by rememberSaveable {
        mutableStateOf(VerifierAttributeOption.PORTRAIT_AND_AGE_OVER_21)
    }
    var isAttributeGroupExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.Gray),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AttributeGroupDropdown(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("attribute_group_menu"),
                textFieldValue = selected.displayName,
                isAttributeGroupExpanded = isAttributeGroupExpanded,
                onToggleDropdownExpansion = { isAttributeGroupExpanded = it },
                onSelectAttributeOption = {
                    isAttributeGroupExpanded = false
                    selected = it
                }
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        onSelectAttributeGroup(selected.attributeGroup)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttributeGroupDropdown(
    textFieldValue: String,
    isAttributeGroupExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleDropdownExpansion: (Boolean) -> Unit = {},
    onSelectAttributeOption: (VerifierAttributeOption) -> Unit = {}
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
                VerifierAttributeOption.entries.forEach { option ->
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
