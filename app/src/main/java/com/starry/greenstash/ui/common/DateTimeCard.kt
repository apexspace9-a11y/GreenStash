/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.starry.greenstash.R
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.ui.screens.settings.dateStyleToDisplayFormat
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.weakHapticFeedback
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val VietnameseLocale = Locale.forLanguageTag("vi-VN")

@Composable
fun DateTimeCard(
    selectedDateTime: LocalDateTime,
    dateStyle: () -> DateStyle,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .liquidGlass(radius = 24.dp, blurAmount = 26.dp)
            .clickable {
                view.weakHapticFeedback()
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_dw_date),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = selectedDateTime.format(
                        DateTimeFormatter.ofPattern(
                            dateStyleToDisplayFormat(dateStyle()),
                            VietnameseLocale
                        )
                    ),
                    fontFamily = greenstashFont,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Row {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_dw_time),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = selectedDateTime.format(
                        DateTimeFormatter.ofPattern("HH:mm", VietnameseLocale)
                    ),
                    fontFamily = greenstashFont,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}
