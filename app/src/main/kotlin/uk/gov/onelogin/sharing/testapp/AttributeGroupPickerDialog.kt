package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AttributeGroupPickerDialog(onSelect: (VerifierAttributeOption) -> Unit, onDismiss: () -> Unit) {
    var selected by rememberSaveable {
        mutableStateOf(VerifierAttributeOption.PHOTO_AND_AGE_OVER_21)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.select_attribute_group),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                VerifierAttributeOption.entries.forEach { option ->
                    OutlinedButton(
                        onClick = { selected = option },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag(ATTRIBUTE_GROUP_ITEM_TAG),
                        border = BorderStroke(
                            width = if (selected == option) 2.dp else 1.dp,
                            color = if (selected == option) Color.Blue else Color.Gray
                        )
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option }
                        )
                        Text(option.displayName)
                    }
                }
                Button(
                    onClick = { onSelect(selected) },
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
}
