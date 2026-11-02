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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeBg
import com.example.ui.theme.ThemeBorder
import com.example.ui.theme.ThemeMuted
import com.example.ui.theme.ThemeSuccess
import com.example.ui.theme.ThemeSurface
import com.example.ui.theme.ThemeSurface2
import com.example.ui.theme.ThemeText

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBg)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Phone Automation",
            style = MaterialTheme.typography.displayLarge,
            color = ThemeAccent
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "وكيل التحكم الذكي بالهاتف • أمان محلي 100%",
            style = MaterialTheme.typography.bodyMedium,
            color = ThemeMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        OnboardingStepCard(
            stepNumber = "1",
            title = "الصلاحيات المحلية المباشرة",
            description = "يعتمد التطبيق على نوايا النظام وإمكانية الوصول المحلية لأداء مهامك. لا يوجد أي تصريح بالوصول إلى سجل مكالماتك أو رسائلك الخاصة.",
            icon = Icons.Default.Security
        )

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingStepCard(
            stepNumber = "2",
            title = "صفر اتصال إنترنت • صفر تتبّع",
            description = "المسار الأساسي يعمل دون اتصال بنسبة 100%. لا خوادم خارجية، لا حسابات تسجيل دخول، ولا جمع بيانات أو تحليلات سرية.",
            icon = Icons.Default.Lock
        )

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingStepCard(
            stepNumber = "3",
            title = "إبقاء الوكيل حياً ضد إغلاق النظام",
            description = "لضمان استجابة الأتمتة على أجهزة Xiaomi و Samsung و Huawei، يتم تشغيل خدمة خلفية خفيفة بإشعار دائم، مع استثناء تحسين البطارية الذكي.",
            icon = Icons.Default.BatteryAlert
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_complete_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ThemeAccent,
                contentColor = ThemeBg
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بدء استخدام الوكيل",
                    style = MaterialTheme.typography.titleMedium,
                    color = ThemeBg
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnboardingStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector
) {
    LuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ThemeSurface,
        borderColor = ThemeBorder
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ThemeSurface2),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ThemeAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "الخطوة $stepNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = ThemeAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ThemeText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ThemeMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
