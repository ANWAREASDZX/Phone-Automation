package com.example.agent

import com.example.model.ActionPlan
import com.example.model.ActionStep
import com.example.model.ActionType
import com.example.model.PlanStatus
import com.example.model.RiskLevel
import java.util.UUID

class OfflineIntentParser {

    // Fast-path intent patterns for zero-latency execution (<10ms)
    private val fastCache = mapOf(
        "شغل الواي فاي" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "on")),
        "شغل الوايفاي" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "on")),
        "شغل الـ wifi" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "on")),
        "wifi on" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "on")),
        "طفي الواي فاي" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "off")),
        "سكر الوايفاي" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "off")),
        "wifi off" to Pair(ActionType.TOGGLE_WIFI, mapOf("state" to "off")),

        "شغل البلوتوث" to Pair(ActionType.TOGGLE_BLUETOOTH, mapOf("state" to "on")),
        "سكر البلوتوث" to Pair(ActionType.TOGGLE_BLUETOOTH, mapOf("state" to "off")),
        "bluetooth on" to Pair(ActionType.TOGGLE_BLUETOOTH, mapOf("state" to "on")),
        "bluetooth off" to Pair(ActionType.TOGGLE_BLUETOOTH, mapOf("state" to "off")),

        "شغل الكشاف" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "on")),
        "طفي الكشاف" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "off")),
        "شغل الفلاش" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "on")),
        "طفي الفلاش" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "off")),
        "طفي الضو" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "off")),
        "شعل الضو" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "on")),
        "flashlight on" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "on")),
        "flashlight off" to Pair(ActionType.FLASHLIGHT, mapOf("state" to "off")),

        "وضع الصامت" to Pair(ActionType.SET_DND, mapOf("state" to "silent")),
        "صامت" to Pair(ActionType.SET_DND, mapOf("state" to "silent")),
        "حطو سايلنت" to Pair(ActionType.SET_DND, mapOf("state" to "silent")),
        "حط التلفون صامت" to Pair(ActionType.SET_DND, mapOf("state" to "silent")),
        "فعّل عدم الإزعاج" to Pair(ActionType.SET_DND, mapOf("state" to "dnd")),
        "dnd on" to Pair(ActionType.SET_DND, mapOf("state" to "dnd")),
        "mute phone" to Pair(ActionType.SET_DND, mapOf("state" to "silent")),
        "الغاء الصامت" to Pair(ActionType.SET_DND, mapOf("state" to "normal")),
        "الوضع العام" to Pair(ActionType.SET_DND, mapOf("state" to "normal"))
    )

    // Hard blocked absolute keywords (L3)
    private val absoluteBlockKeywords = listOf(
        "ضبط المصنع", "factory reset", "فرمت", "format",
        "تحويل بنكي", "دفع", "bank transfer", "pay",
        "احذف كل جهات الاتصال", "امسح كل الصور", "حذف جماعي",
        "إرسال جماعي", "broadcast to all"
    )

    // Ambiguous and dangerous keywords requiring mandatory clarification
    private val ambiguousKeywords = listOf(
        "نظف الهاتف", "نظّف الموبايل", "clean phone",
        "رتب اموري", "رتّب أموري", "organize my things",
        "احذف كل شي", "امسح كل شي", "delete everything"
    )

    fun parse(rawInput: String, isDryRun: Boolean = false): ActionPlan {
        val trimmed = rawInput.trim()
        val normalized = trimmed.lowercase()
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")

        val planId = "plan_" + UUID.randomUUID().toString().take(8)

        // 1. Check Absolute Blacklist (L3)
        for (kw in absoluteBlockKeywords) {
            if (normalized.contains(kw)) {
                val step = ActionStep(
                    id = "step_1",
                    actionType = ActionType.CLARIFY_QUESTION,
                    target = "SECURITY_BLOCK",
                    riskLevel = RiskLevel.L3,
                    requiresConfirmation = true,
                    undoable = false,
                    verifyAfter = false,
                    descriptionAr = "تم حظر الإجراء نهائياً: يتنافى مع سياسة أمان وحماية الهاتف (L3).",
                    descriptionEn = "Strictly blocked: violates safety policy (L3)."
                )
                return ActionPlan(
                    planId = planId,
                    userPrompt = rawInput,
                    intentName = "BLOCKED_DANGEROUS",
                    steps = listOf(step),
                    overallRisk = RiskLevel.L3,
                    requiresConfirmation = true,
                    status = PlanStatus.BLOCKED,
                    isDryRun = isDryRun
                )
            }
        }

        // 2. Check Catastrophic Ambiguity
        for (kw in ambiguousKeywords) {
            if (normalized.contains(kw)) {
                val step = ActionStep(
                    id = "step_1",
                    actionType = ActionType.CLARIFY_QUESTION,
                    target = "AMBIGUITY_RESOLVER",
                    riskLevel = RiskLevel.L1,
                    requiresConfirmation = true,
                    undoable = false,
                    verifyAfter = false,
                    descriptionAr = "أمر عام وغامض. ماذا تقصد تحديداً؟",
                    descriptionEn = "Vague command requires explicit clarification."
                )
                return ActionPlan(
                    planId = planId,
                    userPrompt = rawInput,
                    intentName = "AMBIGUOUS_COMMAND",
                    steps = listOf(step),
                    overallRisk = RiskLevel.L1,
                    requiresConfirmation = true,
                    status = PlanStatus.AMBIGUOUS_NEEDS_CLARIFICATION,
                    isDryRun = isDryRun,
                    clarificationQuestionAr = "أمرك غير محدد وقد يسبب فقداً للبيانات إذا خُمِّن خطأً: هل تقصد مسح ذاكرة التخزين المؤقت للتطبيقات (Cache) أم ترتيب الشاشة الرئيسية؟"
                )
            }
        }

        // 3. Fast Intent Cache Match
        fastCache[trimmed.lowercase()]?.let { (actionType, params) ->
            val descAr = when (actionType) {
                ActionType.TOGGLE_WIFI -> if (params["state"] == "on") "تفعيل شبكة الواي فاي" else "إيقاف شبكة الواي فاي"
                ActionType.TOGGLE_BLUETOOTH -> if (params["state"] == "on") "تفعيل البلوتوث" else "إيقاف البلوتوث"
                ActionType.FLASHLIGHT -> if (params["state"] == "on") "تشغيل كشاف الهاتف" else "إطفاء كشاف الهاتف"
                ActionType.SET_DND -> if (params["state"] == "normal") "إعادة الهاتف إلى الوضع العام" else "تفعيل الوضع الصامت (عدم الإزعاج)"
                else -> "تنفيذ الإجراء السريع"
            }
            val step = ActionStep(
                id = "step_fast_1",
                actionType = actionType,
                target = params["state"] ?: "default",
                params = params,
                riskLevel = RiskLevel.L2, // Low-risk whitelisted toggle
                requiresConfirmation = false,
                undoable = true,
                verifyAfter = true,
                descriptionAr = descAr,
                descriptionEn = "Execute fast-path cached intent"
            )
            return ActionPlan(
                planId = planId,
                userPrompt = rawInput,
                intentName = actionType.name,
                steps = listOf(step),
                overallRisk = RiskLevel.L2,
                requiresConfirmation = false,
                status = PlanStatus.PENDING_APPROVAL,
                isDryRun = isDryRun
            )
        }

        // 4. Grammar & Dialect Intent Recognition
        return parseComplexIntents(planId, rawInput, normalized, isDryRun)
    }

    private fun parseComplexIntents(planId: String, rawInput: String, normalized: String, isDryRun: Boolean): ActionPlan {
        // App opening (افتح، افتحلي، open, launch)
        if (normalized.startsWith("افتح") || normalized.startsWith("افتحلي") || normalized.startsWith("open") || normalized.startsWith("launch")) {
            val appTarget = extractAppTarget(normalized)
            val step = ActionStep(
                id = "step_app_1",
                actionType = ActionType.OPEN_APP,
                target = appTarget.first,
                params = mapOf("packageName" to appTarget.second),
                riskLevel = RiskLevel.L2,
                requiresConfirmation = false,
                undoable = false,
                verifyAfter = true,
                descriptionAr = "فتح تطبيق ${appTarget.first}",
                descriptionEn = "Launch ${appTarget.first}"
            )
            return ActionPlan(
                planId = planId,
                userPrompt = rawInput,
                intentName = "OPEN_APP",
                steps = listOf(step),
                overallRisk = RiskLevel.L2,
                requiresConfirmation = false,
                status = PlanStatus.PENDING_APPROVAL,
                isDryRun = isDryRun
            )
        }

        // Alarm / Timer (منبه، منبّه، مؤقت، timer, alarm)
        if (normalized.contains("منبه") || normalized.contains("منبه") || normalized.contains("alarm")) {
            val hour = extractTimeHour(normalized)
            val step = ActionStep(
                id = "step_alarm_1",
                actionType = ActionType.SET_ALARM,
                target = "AlarmClock",
                params = mapOf("hour" to hour.toString(), "minute" to "0"),
                riskLevel = RiskLevel.L1,
                requiresConfirmation = true,
                undoable = true,
                verifyAfter = true,
                descriptionAr = "ضبط منبه في تمام الساعة $hour:00",
                descriptionEn = "Set alarm for $hour:00"
            )
            return ActionPlan(
                planId = planId,
                userPrompt = rawInput,
                intentName = "SET_ALARM",
                steps = listOf(step),
                overallRisk = RiskLevel.L1,
                requiresConfirmation = true,
                status = PlanStatus.PENDING_APPROVAL,
                isDryRun = isDryRun
            )
        }

        if (normalized.contains("مؤقت") || normalized.contains("تايمر") || normalized.contains("timer")) {
            val minutes = extractTimerMinutes(normalized)
            val step = ActionStep(
                id = "step_timer_1",
                actionType = ActionType.SET_TIMER,
                target = "Timer",
                params = mapOf("minutes" to minutes.toString()),
                riskLevel = RiskLevel.L1,
                requiresConfirmation = true,
                undoable = true,
                verifyAfter = true,
                descriptionAr = "بدء مؤقت تنازلي لمدة $minutes دقيقة",
                descriptionEn = "Start timer for $minutes minutes"
            )
            return ActionPlan(
                planId = planId,
                userPrompt = rawInput,
                intentName = "SET_TIMER",
                steps = listOf(step),
                overallRisk = RiskLevel.L1,
                requiresConfirmation = true,
                status = PlanStatus.PENDING_APPROVAL,
                isDryRun = isDryRun
            )
        }

        // Messaging (ابعت، أرسل، ارسل، رسالة، send message, whatsapp)
        if (normalized.contains("رساله") || normalized.contains("ارسل") || normalized.contains("ابعت") || normalized.contains("send message")) {
            val contact = extractContact(rawInput)
            val messageText = extractMessageText(rawInput)

            // If contact is ambiguous ("أحمد", "صديقي"), require confirmation
            val step = ActionStep(
                id = "step_msg_1",
                actionType = ActionType.SEND_MESSAGE,
                target = contact,
                params = mapOf("recipient" to contact, "message" to messageText),
                riskLevel = RiskLevel.L1, // Messaging requires explicit confirmation (L1)
                requiresConfirmation = true,
                undoable = false,
                verifyAfter = true,
                descriptionAr = "إرسال رسالة إلى [$contact]: \"$messageText\"",
                descriptionEn = "Send message to [$contact]: \"$messageText\""
            )
            return ActionPlan(
                planId = planId,
                userPrompt = rawInput,
                intentName = "SEND_MESSAGE",
                steps = listOf(step),
                overallRisk = RiskLevel.L1,
                requiresConfirmation = true,
                status = PlanStatus.PENDING_APPROVAL,
                isDryRun = isDryRun
            )
        }

        // Media control (شغل موسيقى، وقف الصوت، play music, pause)
        if (normalized.contains("موسيقى") || normalized.contains("اغنيه") || normalized.contains("play") || normalized.contains("pause") || normalized.contains("صوت")) {
            val isMute = normalized.contains("كتم") || normalized.contains("صامت") || normalized.contains("وطي")
            val step = ActionStep(
                id = "step_media_1",
                actionType = if (isMute) ActionType.SET_VOLUME else ActionType.MEDIA_CONTROL,
                target = if (isMute) "volume_down" else "media_play_pause",
                params = mapOf("action" to if (isMute) "mute" else "toggle"),
                riskLevel = RiskLevel.L2,
                requiresConfirmation = false,
                undoable = true,
                verifyAfter = true,
                descriptionAr = if (isMute) "خفض مستوى الصوت وكتم النغمات" else "تبديل تشغيل/إيقاف الوسائط والموسيقى",
                descriptionEn = "Control media playback or volume"
            )
            return ActionPlan(
                planId = planId,
                userPrompt = rawInput,
                intentName = "MEDIA_CONTROL",
                steps = listOf(step),
                overallRisk = RiskLevel.L2,
                requiresConfirmation = false,
                status = PlanStatus.PENDING_APPROVAL,
                isDryRun = isDryRun
            )
        }

        // General fallback suggestion
        val fallbackStep = ActionStep(
            id = "step_generic_1",
            actionType = ActionType.TAKE_NOTE,
            target = "Notes",
            params = mapOf("content" to rawInput),
            riskLevel = RiskLevel.L0,
            requiresConfirmation = true,
            undoable = true,
            verifyAfter = true,
            descriptionAr = "حفظ نص الأمر في الملاحظات السريعة: \"$rawInput\"",
            descriptionEn = "Record text to quick notes"
        )
        return ActionPlan(
            planId = planId,
            userPrompt = rawInput,
            intentName = "TAKE_NOTE",
            steps = listOf(fallbackStep),
            overallRisk = RiskLevel.L0,
            requiresConfirmation = true,
            status = PlanStatus.PENDING_APPROVAL,
            isDryRun = isDryRun
        )
    }

    private fun extractAppTarget(normalized: String): Pair<String, String> {
        return when {
            normalized.contains("واتساب") || normalized.contains("whatsapp") -> Pair("واتساب (WhatsApp)", "com.whatsapp")
            normalized.contains("تيليغرام") || normalized.contains("telegram") -> Pair("تيليغرام (Telegram)", "org.telegram.messenger")
            normalized.contains("كاميرا") || normalized.contains("camera") -> Pair("الكاميرا", "com.android.camera")
            normalized.contains("حاسبه") || normalized.contains("calculator") -> Pair("الحاسبة", "com.google.android.calculator")
            normalized.contains("يوتيوب") || normalized.contains("youtube") -> Pair("يوتيوب (YouTube)", "com.google.android.youtube")
            normalized.contains("اعدادات") || normalized.contains("settings") -> Pair("إعدادات النظام", "com.android.settings")
            normalized.contains("خرائط") || normalized.contains("maps") -> Pair("خرائط Google", "com.google.android.apps.maps")
            normalized.contains("ساعه") || normalized.contains("clock") -> Pair("الساعة والمنبه", "com.google.android.deskclock")
            else -> Pair("التطبيق الافتراضي", "com.android.settings")
        }
    }

    private fun extractTimeHour(normalized: String): Int {
        val numbers = Regex("\\d+").find(normalized)?.value?.toIntOrNull()
        if (numbers != null && numbers in 1..24) return numbers
        if (normalized.contains("سبع")) return 7
        if (normalized.contains("ثمان") || normalized.contains("تمان")) return 8
        if (normalized.contains("تسع")) return 9
        if (normalized.contains("عشر")) return 10
        if (normalized.contains("ست")) return 6
        return 7 // default
    }

    private fun extractTimerMinutes(normalized: String): Int {
        val numbers = Regex("\\d+").find(normalized)?.value?.toIntOrNull()
        if (numbers != null && numbers in 1..180) return numbers
        if (normalized.contains("عشر") || normalized.contains("10")) return 10
        if (normalized.contains("خمس") || normalized.contains("5")) return 5
        if (normalized.contains("ربع ساعه")) return 15
        if (normalized.contains("نص ساعه")) return 30
        return 10
    }

    private fun extractContact(rawInput: String): String {
        val lower = rawInput.lowercase()
        return when {
            lower.contains("امي") || lower.contains("أمي") || lower.contains("ماما") -> "أمي"
            lower.contains("ابي") || lower.contains("أبي") || lower.contains("بابا") -> "أبي"
            lower.contains("المدير") || lower.contains("مديري") -> "المدير (العمل)"
            lower.contains("سامي") -> "سامي"
            lower.contains("أحمد") || lower.contains("احمد") -> "أحمد"
            lower.contains("ساره") || lower.contains("سارة") -> "سارة"
            lower.contains("john") -> "John"
            else -> "جهة الاتصال المحددة"
        }
    }

    private fun extractMessageText(rawInput: String): String {
        val keywords = listOf("نصها", "وقل له", "واكتب", "text", "message", "saying")
        for (kw in keywords) {
            val idx = rawInput.indexOf(kw)
            if (idx != -1) {
                val extracted = rawInput.substring(idx + kw.length).trim().removePrefix(":").trim()
                if (extracted.isNotBlank()) return extracted
            }
        }
        return "أنا في الطريق وسأتصل بك قريباً"
    }
}
