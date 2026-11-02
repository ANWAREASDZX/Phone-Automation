package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LearnedPattern
import com.example.model.PatternStatus
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeAccentSoft
import com.example.ui.theme.ThemeBg
import com.example.ui.theme.ThemeBorder
import com.example.ui.theme.ThemeDanger
import com.example.ui.theme.ThemeGlassBorder
import com.example.ui.theme.ThemeMuted
import com.example.ui.theme.ThemeSuccess
import com.example.ui.theme.ThemeSurface
import com.example.ui.theme.ThemeSurface2
import com.example.ui.theme.ThemeText
import com.example.viewmodel.AgentViewModel

@Composable
fun MemoryScreen(viewModel: AgentViewModel) {
    val patterns by viewModel.learnerEngine.patterns.collectAsState()
    val isDriftFrozen by viewModel.learnerEngine.isDriftFrozen.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ThemeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "الذاكرة ومحرّك التعلّم",
                        style = MaterialTheme.typography.titleLarge,
                        color = ThemeText
                    )
                    Text(
                        text = "القفص الذهبي: تعلّم استنتاجي مع اعتماد بشري صريح",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ThemeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Daily Summary Card
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الملخص اليومي • ماذا فعلت نيابةً عنك",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.learnerEngine.generateDailySummary(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ThemeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Drift Freeze Warning
            if (isDriftFrozen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ThemeDanger.copy(alpha = 0.15f))
                        .border(1.dp, ThemeDanger, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ThemeDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تحذير: كشف انحراف التعلّم (Drift Alert)",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 14.sp,
                                color = ThemeDanger
                            )
                            Text(
                                text = "تم رصد ارتفاع في نسبة الخطأ (>20%)، تم تجميد التعلّم الآلي تلقائياً لمنع العشوائية.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = ThemeText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        items(patterns) { pattern ->
            PatternCard(
                pattern = pattern,
                onAdopt = { viewModel.adoptLearnedPattern(pattern.id) },
                onReject = { viewModel.rejectLearnedPattern(pattern.id) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { showClearConfirm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeDanger.copy(alpha = 0.15f),
                    contentColor = ThemeDanger
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clear_learning_memory_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "امسح ذاكرة التعلّم بالكامل",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    text = "مسح ذاكرة التعلّم؟",
                    style = MaterialTheme.typography.titleMedium,
                    color = ThemeText
                )
            },
            text = {
                Text(
                    text = "سيتم حذف كافة الأنماط المستنتجة والروتينات الملاحظة فوراً وتصفير عداد النجاحات. هذا الإجراء لا يمكن التراجع عنه.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ThemeMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLearnedMemory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeDanger)
                ) {
                    Text("نعم، احذف الذاكرة", color = ThemeText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("إلغاء", color = ThemeMuted)
                }
            },
            containerColor = ThemeSurface
        )
    }
}

@Composable
private fun PatternCard(
    pattern: LearnedPattern,
    onAdopt: () -> Unit,
    onReject: () -> Unit
) {
    var expandedRationale by remember { mutableStateOf(false) }

    LuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ThemeSurface,
        borderColor = ThemeGlassBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pattern.triggerPhrase,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                    color = ThemeText,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (pattern.status) {
                                PatternStatus.ADOPTED -> ThemeSuccess.copy(alpha = 0.2f)
                                PatternStatus.PROPOSED -> ThemeAccentSoft
                                else -> ThemeSurface2
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = pattern.status.labelAr,
                        color = when (pattern.status) {
                            PatternStatus.ADOPTED -> ThemeSuccess
                            PatternStatus.PROPOSED -> ThemeAccent
                            else -> ThemeMuted
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar towards 5 successes
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "مؤشر الثبات والنجاح المتتالي:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeMuted
                    )
                    Text(
                        text = "${pattern.consecutiveSuccesses}/5 نجاحات",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (pattern.consecutiveSuccesses / 5f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ThemeAccent,
                    trackColor = ThemeSurface2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable "لماذا اقترحت هذا؟"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedRationale = !expandedRationale }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "لماذا اقترحت هذا النمط؟",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeAccent
                )
                Icon(
                    imageVector = if (expandedRationale) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ThemeAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expandedRationale) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ThemeSurface2)
                        .padding(10.dp)
                ) {
                    Text(
                        text = pattern.rationaleAr,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = ThemeText
                    )
                }
            }

            if (pattern.status == PatternStatus.PROPOSED) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("reject_pattern_${pattern.id}")
                    ) {
                        Text(text = "رفض", color = ThemeMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onAdopt,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeAccent,
                            contentColor = ThemeBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("adopt_pattern_${pattern.id}")
                    ) {
                        Text(text = "اعتماد الأتمتة", color = ThemeBg)
                    }
                }
            }
        }
    }
}
