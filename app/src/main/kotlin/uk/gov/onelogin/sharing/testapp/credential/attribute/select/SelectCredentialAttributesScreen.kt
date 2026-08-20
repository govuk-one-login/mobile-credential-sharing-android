package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
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
            UserDropdownMenu(
                modifier = Modifier.fillMaxWidth().testTag("attribute_group_menu"),
                label = { Text("Attribute group") },
                onTextFieldClickLabel = "Select attribute group",
                textFieldValue = selected.displayName,
                isDropdownExpanded = isAttributeGroupExpanded,
                onToggleDropdownExpansion = { isAttributeGroupExpanded = it },
                dropdownMenuContents = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacingSingle)
                    ) {
                        VerifierAttributeOption.entries.forEach { option ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .testTag(ATTRIBUTE_GROUP_ITEM_TAG),
                                text = { Text(option.displayName) },
                                onClick = {
                                    isAttributeGroupExpanded = false
                                    selected = option
                                }
                            )
                        }
                    }
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

@Composable
private fun UserDropdownMenu(
    textFieldValue: String,
    isDropdownExpanded: Boolean,
    dropdownMenuContents: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    textFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    label: @Composable () -> Unit = {},
    onTextFieldClickLabel: String? = null,
    onToggleDropdownExpansion: (Boolean) -> Unit = {}
) {
    val attributeGroupTrailingIcon by remember {
        derivedStateOf {
            if (isDropdownExpanded) {
                R.drawable.outline_arrow_drop_up_24
            } else {
                R.drawable.outline_arrow_drop_down_24
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            colors = textFieldColors,
            value = textFieldValue,
            onValueChange = { },
            label = label,
            readOnly = true,
            trailingIcon = {
                Icon(
                    painterResource(attributeGroupTrailingIcon),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dropdown_text")
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .testTag("expand_dropdown")
                .clickable(
                    role = Role.Button,
                    onClickLabel = onTextFieldClickLabel
                ) { onToggleDropdownExpansion(!isDropdownExpanded) }
        )

        DropdownMenu(
            containerColor = MaterialTheme.colorScheme.surface,
            expanded = isDropdownExpanded,
            border = BorderStroke(
                Dp.Hairline,
                MaterialTheme.colorScheme.onSurface
            ),
            onDismissRequest = { onToggleDropdownExpansion(false) },
            modifier = Modifier
                .semantics {
                    role = Role.Button
                }
                .clickable { onToggleDropdownExpansion(!isDropdownExpanded) }
        ) {
            GdsTheme {
                dropdownMenuContents()
            }
        }
    }
}
