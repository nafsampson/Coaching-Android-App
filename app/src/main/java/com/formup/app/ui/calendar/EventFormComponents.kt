// Provides shared event fields and date and time picker controls.

package com.formup.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.theme.FormUpColors

@Composable
internal fun EventTypeToggle(
    selected: EventKind,
    onSelect: (EventKind) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(FormUpColors.NavIndicator, RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        listOf(
            EventKind.TRAINING to R.string.event_form_training,
            EventKind.MATCH to R.string.event_form_match
        ).forEach { (value, label) ->
            val isSelected = value == selected

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .clickable { onSelect(value) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) FormUpColors.Surface else Color.Transparent
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isSelected) FormUpColors.Primary else FormUpColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
internal fun EventDateTimeField(
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = FormUpColors.Surface,
                disabledBorderColor = FormUpColors.Hairline,
                disabledTextColor = FormUpColors.TextPrimary,
                disabledPlaceholderColor = FormUpColors.TextSecondary
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

@Composable
internal fun EventFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = FormUpColors.TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
internal fun EventFormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = FormUpColors.TextSecondary) },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FormUpColors.Surface,
                unfocusedContainerColor = FormUpColors.Surface,
                focusedBorderColor = FormUpColors.Primary,
                unfocusedBorderColor = FormUpColors.Hairline,
                cursorColor = FormUpColors.Primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = FormUpColors.Danger,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}
