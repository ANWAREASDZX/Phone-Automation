package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ActionPlan
import com.example.model.AuditLogEntry
import com.example.model.PlanStatus
import com.example.model.RiskLevel
import com.example.ui.components.GlowingMicButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.RiskBadge
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: AgentViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToMemory: () -> Unit
) {
    val inputText by viewModel.inputText.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val lastMessage by viewModel.lastExecutionMessage.collectAsState()
    val isDryRun by viewModel.isDryRunMode.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val avgLatency by viewModel.avgLatency.collectAsState()

    val quickPills = listOf(
        "شغل الواي فاي",
        "حط التلفون صامت",
        "شغل الكشاف",
        "منبه 7:00 ص",
        "افتح الواتساب",
        "نظف الهاتف"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBg)
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(18.dp))

            // Top Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Phone Automation",
                        style = MaterialTheme.typography.titleLarge,
                        color = ThemeText
                    )
                    Text(
                        text = "محلي 100% • سرعة الاستجابة ${avgLatency}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeAccent
                    )
                }

                // Dry-Run Badge Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDryRun) ThemeAccentSoft else ThemeSurface2)
                        .border(1.dp, if (isDryRun) ThemeAccent else ThemeGlassBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleDryRun() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("dry_run_toggle")
                ) {
                    Text(
                        text = if (isDryRun) "وضع المحاكاة: مفعّل" else "تنفيذ حقيقي للنظام",
                        color = if (isDryRun) ThemeAccent else ThemeMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Glowing Mic Listening Center
            GlowingMicButton(
                isListening = isListening,
                onClick = { viewModel.toggleListening() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isListening) "استمع لصوتك الآن..." else "اضغط للتحدث أو اكتب أمراً بالأسفل",
                style = MaterialTheme.typography.bodyMedium,
                color = ThemeMuted
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Command Input Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.setInputText(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    placeholder = {
                        Text(
                            text = "اكتب أمراً بالعربية أو الإنجليزية...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ThemeMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ThemeSurface,
                        unfocusedContainerColor = ThemeSurface,
                        focusedBorderColor = ThemeAccent,
                        unfocusedBorderColor = ThemeGlassBorder,
                        focusedTextColor = ThemeText,
                        unfocusedTextColor = ThemeText
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.submitCommand() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ThemeAccent)
                        .testTag("submit_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال الأمر",
                        tint = ThemeBg
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Dialect & Command Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPills.forEach { pill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ThemeSurface2)
                            .border(1.dp, ThemeGlassBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.setInputText(pill)
                                viewModel.submitCommand(pill)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = pill,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = ThemeText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Post-execution Status Alert Banner
            if (lastMessage != null) {
                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ThemeAccentSoft,
                    borderColor = ThemeAccent
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ThemeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = lastMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ThemeText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Today's Suggestion Card
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeBorder,
                onClick = onNavigateToMemory
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ThemeAccentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = ThemeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اقتراح التعلّم لليوم",
                            style = MaterialTheme.typography.labelSmall,
                            color = ThemeAccent
                        )
                        Text(
                            text = "وضع النوم الليلي: كتم الصوت وإيقاف الواي فاي (5/5 نجاحات)",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = ThemeText
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "عرض الاقتراح",
                        tint = ThemeMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header: Recent Commands
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر الأوامر المنفّذة",
                    style = MaterialTheme.typography.titleMedium,
                    color = ThemeText
                )

                TextButton(
                    onClick = { viewModel.revertTopUndo() },
                    modifier = Modifier.testTag("quick_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = null,
                        tint = ThemeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تراجع سريع (Undo)",
                        style = MaterialTheme.typography.labelLarge,
                        color = ThemeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // List of last 5 commands
        items(auditLogs.take(5)) { log ->
            CommandHistoryCard(
                log = log,
                onUndo = { viewModel.revertTopUndo() }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Safety Gate Plan Preview Dialog
    if (activePlan != null && activePlan?.requiresConfirmation == true) {
        SafetyGateDialog(
            plan = activePlan!!,
            onApprove = { viewModel.confirmAndExecutePlan(activePlan!!) },
            onDismiss = { viewModel.dismissActivePlan() }
        )
    }
}

@Composable
private fun CommandHistoryCard(
    log: AuditLogEntry,
    onUndo: () -> Unit
) {
    LuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ThemeSurface,
        borderColor = ThemeGlassBorder
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.commandText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    color = ThemeText
                )

                RiskBadge(riskLevel = log.riskLevel)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.planSummary,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = ThemeMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${log.executionResult} • ${log.latencyMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.executionResult == "نجاح" || log.executionResult == "محاكاة ناجحة") ThemeSuccess else ThemeDanger
                )

                if (log.executionResult == "نجاح") {
                    Text(
                        text = "قابل للتراجع",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeAccent,
                        modifier = Modifier.clickable { onUndo() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyGateDialog(
    plan: ActionPlan,
    onApprove: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ThemeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ThemeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بوابة الأمان • مراجعة الخطة",
                        style = MaterialTheme.typography.titleMedium,
                        color = ThemeText
                    )
                    RiskBadge(riskLevel = plan.overallRisk)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "الأمر المدخل:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeMuted
                )
                Text(
                    text = "\"${plan.userPrompt}\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = ThemeAccent
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (plan.status == PlanStatus.BLOCKED) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ThemeDanger.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "تم حظر هذا الإجراء نهائياً بموجب سياسة الأمان (L3). لا يُسمح بتنفيذ عمليات الحذف الشامل أو تهيئة الجهاز أو إجراء المعاملات المالية.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ThemeDanger
                        )
                    }
                } else if (plan.status == PlanStatus.AMBIGUOUS_NEEDS_CLARIFICATION) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ThemeAccentSoft)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = plan.clarificationQuestionAr ?: "أمر غير محدد. يرجى توضيح المطلوب تحديداً لتفادي الأخطاء.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ThemeText
                        )
                    }
                } else {
                    Text(
                        text = "سلسلة الإجراءات المقترحة (DSL Plan):",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    plan.steps.forEach { step ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemeSurface2)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = step.descriptionAr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ThemeText
                                )
                                Text(
                                    text = "{ action: \"${step.actionType.name}\", undoable: ${step.undoable}, verify: ${step.verifyAfter} }",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = ThemeMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_plan_button")
                    ) {
                        Text(text = "إلغاء", color = ThemeMuted)
                    }

                    if (plan.status != PlanStatus.BLOCKED && plan.status != PlanStatus.AMBIGUOUS_NEEDS_CLARIFICATION) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThemeAccent,
                                contentColor = ThemeBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("approve_plan_button")
                        ) {
                            Text(
                                text = "تأكيد وتنفيذ الخطة",
                                style = MaterialTheme.typography.labelLarge,
                                color = ThemeBg
                            )
                        }
                    }
                }
            }
        }
    }
}
