package com.example.model

enum class RiskLevel(val labelAr: String, val badgeColorHex: Long) {
    L0("L0 • اقتراح فقط", 0xFF5B8C6E),
    L1("L1 • تأكيد دائماً", 0xFFC9A227),
    L2("L2 • تلقائي مشروط", 0xFF8B8778),
    L3("L3 • محظور نهائياً", 0xFFD6573F)
}

enum class ActionType(val labelAr: String) {
    OPEN_APP("فتح تطبيق"),
    TOGGLE_WIFI("الواي فاي"),
    TOGGLE_BLUETOOTH("البلوتوث"),
    SET_VOLUME("مستوى الصوت"),
    SET_DND("وضع عدم الإزعاج"),
    SET_BRIGHTNESS("سطوع الشاشة"),
    SEND_MESSAGE("إرسال رسالة"),
    SET_ALARM("ضبط منبه"),
    SET_TIMER("مؤقت تنازلي"),
    MEDIA_CONTROL("الوسائط والموسيقى"),
    FLASHLIGHT("كشاف الإضاءة"),
    TAKE_NOTE("تدوين ملاحظة"),
    CLARIFY_QUESTION("سؤال توضيحي إجباري")
}

enum class PlanStatus {
    PENDING_APPROVAL,
    EXECUTED,
    FAILED,
    REVERTED,
    BLOCKED,
    AMBIGUOUS_NEEDS_CLARIFICATION
}

data class ActionStep(
    val id: String,
    val actionType: ActionType,
    val target: String,
    val params: Map<String, String> = emptyMap(),
    val riskLevel: RiskLevel,
    val requiresConfirmation: Boolean,
    val undoable: Boolean,
    val verifyAfter: Boolean,
    val descriptionAr: String,
    val descriptionEn: String
)

data class ActionPlan(
    val planId: String,
    val userPrompt: String,
    val intentName: String,
    val steps: List<ActionStep>,
    val overallRisk: RiskLevel,
    val requiresConfirmation: Boolean,
    val status: PlanStatus = PlanStatus.PENDING_APPROVAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isDryRun: Boolean = false,
    val clarificationQuestionAr: String? = null
)

data class UndoRecord(
    val id: String,
    val planId: String,
    val actionType: ActionType,
    val previousStateValue: String,
    val descriptionAr: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReverted: Boolean = false
)

enum class TriggerType(val labelAr: String) {
    SCHEDULED("جدول زمني"),
    CONDITION("شرط محدد"),
    MANUAL("تشغيل يدوي")
}

data class Routine(
    val id: String,
    val nameAr: String,
    val descriptionAr: String,
    val triggerType: TriggerType,
    val triggerTime: String, // e.g. "23:00" or "07:30"
    val daysOfWeek: List<Int>, // 1 = Monday .. 7 = Sunday
    val priority: Int, // 1 (Highest) to 5 (Lowest)
    val isEnabled: Boolean,
    val plan: ActionPlan,
    val conflictWarning: String? = null
)

enum class PatternStatus(val labelAr: String) {
    OBSERVING("قيد الملاحظة"),
    PROPOSED("اقتراح معلّق"),
    ADOPTED("مُعتمد ومؤتمت"),
    PAUSED("موقوف مؤقتاً")
}

data class LearnedPattern(
    val id: String,
    val triggerPhrase: String,
    val intentName: String,
    val defaultParams: Map<String, String> = emptyMap(),
    val executionCount: Int,
    val consecutiveSuccesses: Int,
    val status: PatternStatus,
    val rationaleAr: String,
    val driftScore: Float, // 0.0 to 1.0; > 0.20 triggers safety freeze
    val lastObserved: Long = System.currentTimeMillis()
)

data class AuditLogEntry(
    val id: Long = 0,
    val timestamp: Long,
    val commandText: String,
    val planSummary: String,
    val riskLevel: RiskLevel,
    val executionResult: String, // "نجاح", "محظور", "تم التراجع", "فشل"
    val latencyMs: Long,
    val isDryRun: Boolean,
    val verifiedState: String?
)
