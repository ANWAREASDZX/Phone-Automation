package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditLogEntry
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditScreen(viewModel: AgentViewModel) {
    val logs by viewModel.auditLogs.collectAsState()
    val totalCommands by viewModel.totalCommands.collectAsState()
    val successRate by viewModel.successRate.collectAsState()
    val avgLatency by viewModel.avgLatency.collectAsState()

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
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ThemeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "سجل التدقيق المحلي (Audit Log)",
                        style = MaterialTheme.typography.titleLarge,
                        color = ThemeText
                    )
                    Text(
                        text = "توثيق شامل لكل عملية مع زمن الاستجابة والتحقق الفعلي",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ThemeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    title = "إجمالي الأوامر",
                    value = "$totalCommands",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = "نسبة النجاح",
                    value = successRate,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = "متوسط التأخير",
                    value = "${avgLatency}ms",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action row: Export & Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearAuditLogs() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = ThemeMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تفريغ السجل", color = ThemeMuted)
                }

                Button(
                    onClick = { /* Export Encrypted JSON */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeAccentSoft,
                        contentColor = ThemeAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_audit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = ThemeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تصدير السجل JSON", color = ThemeAccent)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        items(logs) { entry ->
            AuditLogCard(entry = entry)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    LuxuryCard(
        modifier = modifier,
        backgroundColor = ThemeSurface,
        borderColor = ThemeGlassBorder
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ThemeMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = ThemeAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AuditLogCard(entry: AuditLogEntry) {
    val formatter = SimpleDateFormat("HH:mm:ss • dd/MM", Locale.getDefault())
    val formattedTime = formatter.format(Date(entry.timestamp))

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
                    text = entry.commandText,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    color = ThemeText
                )
                RiskBadge(riskLevel = entry.riskLevel)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.planSummary,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = ThemeMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (entry.verifiedState != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeSurface2)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entry.verifiedState,
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeSuccess,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.executionResult} • زمن المعالجة: ${entry.latencyMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.executionResult == "نجاح") ThemeSuccess else if (entry.executionResult == "محظور") ThemeDanger else ThemeAccent
                )

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = ThemeMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}
