// Shows the match result, top performer and player ratings.

package com.formup.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.home.components.FormUpBottomBar
import com.formup.app.ui.home.components.FormUpTopBar
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
fun MatchReportScreen(
    state: MatchReportUiState,
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
    modifier: Modifier = Modifier,
    onSelectTab: (HomeTab) -> Unit = {}
) {
    val localizedStatusLine = when {
        state.statusLine == "NO MATCHES" ->
            stringResource(R.string.stats_badge_no_matches)

        state.statusLine.startsWith("FULL TIME") ->
            state.statusLine.replaceFirst(
                "FULL TIME",
                stringResource(R.string.stats_full_time)
            )

        state.statusLine.startsWith("SCHEDULED") ->
            state.statusLine.replaceFirst(
                "SCHEDULED",
                stringResource(R.string.stats_scheduled)
            )

        else -> state.statusLine
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FormUpColors.Background,
        topBar = {
            FormUpTopBar(hasUnread = false, onNotifications = onNotifications)
        },
        bottomBar = {
            FormUpBottomBar(
                selected = HomeTab.Stats,
                onSelect = onSelectTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = stringResource(R.string.ui_back),
                        tint = FormUpColors.TextPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.stats_match_report),
                    style = MaterialTheme.typography.titleMedium,
                    color = FormUpColors.TextSecondary
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { MatchScoreHeader(localizedStatusLine, state.awayTeam, state.homeTeam) }
                item { OverviewCard(state.overview) }
                item { MyPerformanceCard(state.myPerformance) }
                item { RatingsCard(state.ratings) }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FormUpColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = FormUpColors.TextPrimary
        )
    }
}

@Composable
private fun MatchScoreHeader(statusLine: String, away: TeamScoreInfo, home: TeamScoreInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statusLine,
            fontFamily = Mono,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = FormUpColors.TextSecondary
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamBadge(team = away, modifier = Modifier.weight(1f))
            Text(
                text = "${away.score} - ${home.score}",
                fontFamily = Mono,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = FormUpColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            TeamBadge(team = home, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TeamBadge(team: TeamScoreInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (team.isFormUp) FormUpColors.Primary else FormUpColors.Surface),
            contentAlignment = Alignment.Center
        ) {
            if (team.isFormUp) {
                Icon(
                    imageVector = Icons.Filled.SportsSoccer,
                    contentDescription = null,
                    tint = FormUpColors.Surface,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = team.abbreviation,
                    fontFamily = Mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FormUpColors.TextSecondary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = team.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (team.isFormUp) FontWeight.Bold else FontWeight.Normal,
            color = FormUpColors.TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OverviewCard(overview: String) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            icon = Icons.Filled.Description,
            title = stringResource(R.string.stats_overview)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = overview,
            style = MaterialTheme.typography.bodyMedium,
            color = FormUpColors.TextSecondary
        )
    }
}

@Composable
private fun MyPerformanceCard(performance: PerformanceSummary) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            icon = Icons.Filled.Person,
            title = stringResource(R.string.stats_top_performer)
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .border(width = 3.dp, color = FormUpColors.Primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = performance.rating,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = FormUpColors.PrimaryDeep
                    )
                    Text(
                        text = if (performance.ratingLabel == "NO STATS LOGGED") {
                            stringResource(R.string.stats_no_stats_logged)
                        } else {
                            performance.ratingLabel
                        },
                        fontFamily = Mono,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = FormUpColors.PrimaryDeep
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PerformanceMetric(
                label = stringResource(R.string.stats_minutes).uppercase(),
                value = performance.minutes,
                modifier = Modifier.weight(1f)
            )

            PerformanceMetric(
                label = stringResource(R.string.goals).uppercase(),
                value = performance.goals,
                modifier = Modifier.weight(1f)
            )

            PerformanceMetric(
                label = stringResource(R.string.assists).uppercase(),
                value = performance.assists,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PerformanceMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = Mono,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = FormUpColors.TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = FormUpColors.TextPrimary
        )
    }
}

@Composable
private fun RatingsCard(ratings: List<PlayerRating>) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            icon = Icons.Filled.Groups,
            title = stringResource(R.string.stats_player_ratings)
        )
        Spacer(Modifier.height(6.dp))
        if (ratings.isEmpty()) {
            Text(
                text = stringResource(R.string.stats_no_player_stats),
                style = MaterialTheme.typography.bodyMedium,
                color = FormUpColors.TextSecondary,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
        ratings.forEachIndexed { index, rating ->
            if (index > 0) HorizontalDivider(color = FormUpColors.Hairline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${rating.rank}",
                    fontFamily = Mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FormUpColors.TextSecondary,
                    modifier = Modifier.width(30.dp)
                )
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rating.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = FormUpColors.TextPrimary
                        )
                        if (rating.isStandout) {
                            Spacer(Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.Filled.SportsSoccer,
                                contentDescription = stringResource(R.string.stats_man_of_match),
                                tint = FormUpColors.Amber,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = rating.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = FormUpColors.TextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (rating.isStandout) FormUpColors.AmberTint else FormUpColors.PrimaryTint
                ) {
                    Text(
                        text = rating.rating,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (rating.isStandout) FormUpColors.AmberIcon else FormUpColors.PrimaryDeep,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
