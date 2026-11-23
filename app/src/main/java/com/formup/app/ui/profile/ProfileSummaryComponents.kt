// Shows the profile header, season totals and recent activity.

package com.formup.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
internal fun ProfileSectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = FormUpColors.TextPrimary,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
internal fun ProfileHeaderCard(name: String, onEditProfile: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = FormUpColors.Ink) {
        Column(Modifier.padding(22.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(size = 60.dp, background = FormUpColors.Lime) {
                    Text(name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "F" },
                        style = MaterialTheme.typography.headlineMedium, color = FormUpColors.Ink)
                }
                Spacer(Modifier.weight(1f))
                androidx.compose.material3.IconButton(onEditProfile, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Edit, stringResource(R.string.loc_edit_profile), tint = FormUpColors.Lime)
                }
            }
            Text(name, style = MaterialTheme.typography.headlineLarge, color = androidx.compose.ui.graphics.Color.White)
            Text(stringResource(R.string.coach), style = MaterialTheme.typography.labelLarge, color = FormUpColors.Lime)
        }
    }
}

@Composable
internal fun ProfileStatTile(stat: SeasonStat, modifier: Modifier = Modifier) {
    val translatedLabel = when (stat.label) {
        "Goals" -> stringResource(R.string.goals)
        "Assists" -> stringResource(R.string.assists)
        "Matches" -> stringResource(R.string.matches)
        "Wins" -> stringResource(R.string.wins)
        else -> stat.label
    }
    SectionCard(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = translatedLabel.uppercase(),
                fontFamily = Mono,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.6.sp,
                color = FormUpColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = FormUpColors.TextSecondary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stat.value,
            style = MaterialTheme.typography.headlineMedium,
            color = FormUpColors.TextPrimary
        )
    }
}

@Composable
internal fun ProfileActivityRow(activity: ActivityItem) {
    val bubbleColor = when (activity.tone) {
        ActivityTone.Match -> FormUpColors.PrimaryTint
        ActivityTone.Training -> FormUpColors.AmberTint
    }
    val iconColor = when (activity.tone) {
        ActivityTone.Match -> FormUpColors.Primary
        ActivityTone.Training -> FormUpColors.AmberIcon
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(size = 38.dp, background = bubbleColor) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleMedium,
                color = FormUpColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = activity.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = FormUpColors.TextSecondary
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = activity.date,
            fontFamily = Mono,
            fontSize = 12.sp,
            letterSpacing = 0.6.sp,
            color = FormUpColors.TextSecondary
        )
    }
}
