package com.starry.greenstash.ui.screens.home.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starry.greenstash.MainActivity
import com.starry.greenstash.R
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.greenstashNumberFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.getActivity

@Composable
fun SavingsOverviewCard(goals: List<GoalWithTransactions>) {
    if (goals.isEmpty()) return

    val context = LocalContext.current
    val settingsViewModel = (context.getActivity() as? MainActivity)?.settingsViewModel
    val currencyCode = settingsViewModel?.getDefaultCurrencyValue() ?: "VND"

    val totalTarget = goals.sumOf { it.goal.targetAmount.coerceAtLeast(0.0) }
    val totalSaved = goals.sumOf { it.getCurrentlySavedAmount().coerceAtLeast(0.0) }
    val completedGoals = goals.count {
        it.goal.targetAmount > 0.0 && it.getCurrentlySavedAmount() >= it.goal.targetAmount
    }
    val rawProgress = if (totalTarget > 0.0) {
        (totalSaved / totalTarget).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }
    val progress by animateFloatAsState(rawProgress, label = "overall savings progress")
    val progressPercent = (progress * 100f).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .liquidGlass(radius = 30.dp, blurAmount = 30.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.savings_overview_title),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = greenstashFont,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OverviewValue(
                label = stringResource(R.string.savings_overview_saved),
                value = NumberUtils.formatCurrency(totalSaved, currencyCode)
            )
            OverviewValue(
                label = stringResource(R.string.savings_overview_target),
                value = NumberUtils.formatCurrency(totalTarget, currencyCode)
            )
        }

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    R.string.savings_overview_completed,
                    completedGoals,
                    goals.size
                ),
                fontFamily = greenstashFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.savings_overview_progress, progressPercent),
                fontFamily = greenstashFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun OverviewValue(label: String, value: String) {
    Column(modifier = Modifier.padding(end = 8.dp)) {
        Text(
            text = label,
            fontFamily = greenstashFont,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            fontFamily = greenstashNumberFont,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
