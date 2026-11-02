package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Routine
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeAccentSoft
import com.example.ui.theme.ThemeBg
import com.example.ui.theme.ThemeBorder
import com.example.ui.theme.ThemeDanger
import com.example.ui.theme.ThemeGlassBorder
import com.example.ui.theme.ThemeMuted
import com.example.ui.theme.ThemeSurface
import com.example.ui.theme.ThemeSurface2
import com.example.ui.theme.ThemeText
import com.example.viewmodel.AgentViewModel

@Composable
fun StudioScreen(viewModel: AgentViewModel) {
    val routines by viewModel.routines.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBg)
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ThemeAccentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = ThemeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "استوديو الروتينات",
                        style = MaterialTheme.typography.titleLarge,
                        color = ThemeText
                    )
                    Text(
                        text = "جدولة المهام والأتمتة المشروطة مع كشف التعارض",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ThemeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(routines) { routine ->
            RoutineCard(
                routine = routine,
                onToggle = { viewModel.toggleRoutine(routine.id) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    onToggle: () -> Unit
) {
    LuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ThemeSurface,
        borderColor = if (routine.conflictWarning != null) ThemeDanger.copy(alpha = 0.5f) else ThemeGlassBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (routine.isEnabled) ThemeAccent else ThemeMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = routine.nameAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (routine.isEnabled) ThemeText else ThemeMuted
                    )
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ThemeBg,
                        checkedTrackColor = ThemeAccent,
                        uncheckedThumbColor = ThemeMuted,
                        uncheckedTrackColor = ThemeSurface2
                    ),
                    modifier = Modifier.testTag("routine_toggle_${routine.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = routine.descriptionAr,
                style = MaterialTheme.typography.bodyMedium,
                color = ThemeMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeSurface2)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "التوقيت: ${routine.triggerTime} • الأولوية P${routine.priority}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeText
                    )
                }

                Text(
                    text = "${routine.plan.steps.size} إجراءات مبرمجة",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeMuted
                )
            }

            // Conflict Warning Alert
            if (routine.conflictWarning != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ThemeDanger.copy(alpha = 0.12f))
                        .border(1.dp, ThemeDanger.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ThemeDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = routine.conflictWarning,
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeDanger,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
