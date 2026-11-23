// Draws the FormUp logo and app name.

package com.formup.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.theme.FormUpColors

@Composable
fun FormUpBrand(compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Image(painterResource(R.drawable.formup_mark), contentDescription = null, modifier = Modifier.size(if (compact) 36.dp else 56.dp))
        Column {
            Text("FormUp", style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge,
                color = FormUpColors.TextPrimary)
            if (!compact) Text(stringResource(R.string.mod_brand_tagline), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
        }
    }
}
