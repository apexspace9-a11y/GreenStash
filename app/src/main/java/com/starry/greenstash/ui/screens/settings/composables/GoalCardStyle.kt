package com.starry.greenstash.ui.screens.settings.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.starry.greenstash.MainActivity
import com.starry.greenstash.R
import com.starry.greenstash.ui.screens.home.GoalCardStyle
import com.starry.greenstash.ui.screens.home.composables.GoalItemClassic
import com.starry.greenstash.ui.screens.home.composables.GoalItemCompact
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.PreferenceUtil
import com.starry.greenstash.utils.getActivity
import com.starry.greenstash.utils.weakHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCardStyle(navController: NavController) {
    val view = LocalView.current
    val context = navController.context
    val preferenceUtil = PreferenceUtil(context)
    val settingsVM = (context.getActivity() as MainActivity).settingsViewModel
    val currentStyle = settingsVM.goalCardStyle
        .observeAsState(GoalCardStyle.Classic)
        .value
    val currency = preferenceUtil.getString(
        PreferenceUtil.DEFAULT_CURRENCY_STR,
        "VND"
    ) ?: "VND"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.goal_card_settings_header),
                        fontFamily = greenstashFont,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.weakHapticFeedback()
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(radius = 26.dp, blurAmount = 16.dp)
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.preview),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    fontFamily = greenstashFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AnimatedContent(
                    targetState = currentStyle,
                    label = "goal-card-style"
                ) { style ->
                    when (style) {
                        GoalCardStyle.Classic -> {
                            GoalItemClassic(
                                title = stringResource(R.string.preview_example_title),
                                primaryText = stringResource(R.string.preview_example_primary_text)
                                    .format(
                                        NumberUtils.formatCurrency(500.00, currency),
                                        NumberUtils.formatCurrency(5000.00, currency)
                                    ),
                                secondaryText = stringResource(R.string.preview_example_secondary_text)
                                    .format(
                                        NumberUtils.formatCurrency(58.83, currency),
                                        NumberUtils.formatCurrency(416.67, currency),
                                        NumberUtils.formatCurrency(2500.00, currency)
                                    ),
                                goalProgress = 0.6f,
                                goalImage = null,
                                isGoalCompleted = false,
                                onDepositClicked = {},
                                onWithdrawClicked = {},
                                onInfoClicked = {},
                                onEditClicked = {},
                                onDeleteClicked = {},
                                onArchivedClicked = {}
                            )
                        }

                        GoalCardStyle.Compact -> {
                            GoalItemCompact(
                                title = stringResource(R.string.preview_example_title),
                                savedAmount = NumberUtils.formatCurrency(1000.00, currency),
                                daysLeftText = stringResource(R.string.info_card_remaining_days)
                                    .format(12),
                                goalProgress = 0.8f,
                                goalIcon = ImageVector.vectorResource(R.drawable.ic_nav_rating),
                                isGoalCompleted = false,
                                onDepositClicked = {},
                                onWithdrawClicked = {},
                                onInfoClicked = {},
                                onEditClicked = {},
                                onDeleteClicked = {},
                                onArchivedClicked = {}
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(radius = 26.dp, blurAmount = 16.dp)
                    .padding(vertical = 6.dp)
            ) {
                GoalCardStyle.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == currentStyle,
                                onClick = { settingsVM.setGoalCardStyle(option) }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentStyle,
                            onClick = { settingsVM.setGoalCardStyle(option) }
                        )
                        Text(
                            text = when (option) {
                                GoalCardStyle.Classic -> stringResource(R.string.goal_card_option1)
                                GoalCardStyle.Compact -> stringResource(R.string.goal_card_option2)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp),
                            fontFamily = greenstashFont
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = currentStyle == GoalCardStyle.Compact,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .liquidGlass(radius = 22.dp, blurAmount = 14.dp)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.goal_card_settings_tip),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = greenstashFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
