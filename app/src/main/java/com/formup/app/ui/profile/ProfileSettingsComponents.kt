// Shows language, notification and team settings.

package com.formup.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.home.components.IconBubble
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
internal fun ProfileLanguageSelector(
    selected: LanguageOption,
    onSelected: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(size = 38.dp, background = FormUpColors.PrimaryTint) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = FormUpColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.titleMedium,
                    color = FormUpColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text =  stringResource(R.string.app_notification_language),
                    style = MaterialTheme.typography.bodySmall,
                    color = FormUpColors.TextSecondary
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = selected.nativeLabel,
                fontFamily = Mono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FormUpColors.PrimaryDeep
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = FormUpColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupportedLanguages.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.nativeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (option.code == selected.code) {
                                FormUpColors.PrimaryDeep
                            } else {
                                FormUpColors.TextPrimary
                            }
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
internal fun ProfileToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = FormUpColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = FormUpColors.TextSecondary
            )
        }
        Spacer(Modifier.size(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FormUpColors.Surface,
                checkedTrackColor = FormUpColors.Primary,
                uncheckedThumbColor = FormUpColors.Surface,
                uncheckedTrackColor = FormUpColors.Hairline,
                uncheckedBorderColor = FormUpColors.Hairline
            )
        )
    }
}

@Composable
internal fun TeamInfoCard(info: TeamInfo, onEdit: () -> Unit) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(size = 38.dp, background = FormUpColors.PrimaryTint) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = FormUpColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = info.teamName,
                    style = MaterialTheme.typography.titleMedium,
                    color = FormUpColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = info.squad,
                    style = MaterialTheme.typography.bodySmall,
                    color = FormUpColors.TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.edit_team_information),
                tint = FormUpColors.TextSecondary,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onEdit)
                    .padding(12.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = FormUpColors.Hairline)
        Spacer(Modifier.height(6.dp))
        ProfileInfoRow(label = stringResource(R.string.squad), value = info.squad)
    }
}

@Composable
internal fun ProfileInfoRow(label: String, value: String, mono: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = Mono,
            fontSize = 12.sp,
            letterSpacing = 0.6.sp,
            color = FormUpColors.TextSecondary
        )
        Spacer(Modifier.weight(1f))
        if (mono) {
            Text(
                text = value,
                fontFamily = Mono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FormUpColors.PrimaryDeep
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = FormUpColors.TextPrimary
            )
        }
    }
}
