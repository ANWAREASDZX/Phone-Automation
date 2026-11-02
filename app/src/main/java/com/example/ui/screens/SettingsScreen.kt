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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeAccentSoft
import com.example.ui.theme.ThemeBg
import com.example.ui.theme.ThemeBorder
import com.example.ui.theme.ThemeGlassBorder
import com.example.ui.theme.ThemeMuted
import com.example.ui.theme.ThemeSurface
import com.example.ui.theme.ThemeSurface2
import com.example.ui.theme.ThemeText
import com.example.viewmodel.AgentViewModel

@Composable
fun SettingsScreen(viewModel: AgentViewModel) {
    val isLightweight by viewModel.isLightweightMode.collectAsState()
    val isDryRun by viewModel.isDryRunMode.collectAsState()
    val selectedAsr by viewModel.selectedAsrEngine.collectAsState()
    val autoPrune by viewModel.autoPrune30Days.collectAsState()

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
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ThemeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "الإعدادات والنظام",
                        style = MaterialTheme.typography.titleLarge,
                        color = ThemeText
                    )
                    Text(
                        text = "ضبط الأداء، إدارة البطارية وسياسة الخصوصية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ThemeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lightweight Mode
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeGlassBorder
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = ThemeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الوضع الخفيف (أجهزة ≤ 3GB RAM)",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 14.sp,
                                color = ThemeText
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "يعتمد حصرياً على قاموس الأوامر المباشر (Intent Cache) ويلغي تحميل نماذج الذكاء الثقيلة لتوفير الذاكرة والبطارية.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = ThemeMuted
                        )
                    }

                    Switch(
                        checked = isLightweight,
                        onCheckedChange = { viewModel.toggleLightweightMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ThemeBg,
                            checkedTrackColor = ThemeAccent,
                            uncheckedThumbColor = ThemeMuted,
                            uncheckedTrackColor = ThemeSurface2
                        ),
                        modifier = Modifier.testTag("toggle_lightweight_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dry-Run Simulation Mode
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeGlassBorder
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "وضع المحاكاة التجريبي (Dry-Run)",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = ThemeText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "يسمح باختبار وتجربة كافة خطط الأتمتة وفهم النيات دون إحداث أي تغيير فعلي على إعدادات الهاتف.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = ThemeMuted
                        )
                    }

                    Switch(
                        checked = isDryRun,
                        onCheckedChange = { viewModel.toggleDryRun() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ThemeBg,
                            checkedTrackColor = ThemeAccent,
                            uncheckedThumbColor = ThemeMuted,
                            uncheckedTrackColor = ThemeSurface2
                        ),
                        modifier = Modifier.testTag("settings_dryrun_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ASR Engine Selector
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = ThemeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "محرك التعرف على الصوت المحلي (ASR)",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = ThemeText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "Whisper.cpp Quantized (موصى به)",
                        "Vosk Offline Engine",
                        "محرك الأندرويد المدمج"
                    ).forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = engine,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                color = if (selectedAsr == engine) ThemeAccent else ThemeText
                            )

                            Text(
                                text = if (selectedAsr == engine) "نشط" else "اختيار",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedAsr == engine) ThemeAccent else ThemeMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Battery Whitelisting Guide
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = ThemeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "دليل حماية الخدمة من قاتلي البطارية",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = ThemeText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• شاومي (Xiaomi/HyperOS): الإعدادات > التطبيقات > Phone Automation > تفعيل \"التشغيل التلقائي\" واختيار \"لا توجد قيود\".\n• سامسونج (Samsung One UI): إزالة التطبيق من قائمة \"التطبيقات في وضع السكون العميق\".\n• هواوي (Huawei): مدير الهاتف > تشغيل التطبيقات > إدارة يدوية.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = ThemeMuted,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy Declaration
            LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ThemeSurface,
                borderColor = ThemeGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = ThemeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "وثيقة الخصوصية والأمان الصارمة",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = ThemeText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• التطبيق لا يقوم بتسجيل أي مكالمات هاتفية على الإطلاق.\n• لا يتم رفع محتوى الشاشة أو الرسائل النصية إلى أي خادم خارجي.\n• قاعدة البيانات والأنماط مشفرة ومحفوظة محلياً فقط على هاتفك.\n• لا توجد حسابات أو اشتراكات أو أكواد تتبّع إعلانية.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = ThemeMuted,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
