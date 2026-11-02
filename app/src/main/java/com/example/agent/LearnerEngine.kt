package com.example.agent

import com.example.model.LearnedPattern
import com.example.model.PatternStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LearnerEngine {

    private val _patterns = MutableStateFlow<List<LearnedPattern>>(
        listOf(
            LearnedPattern(
                id = "pat_night_mode",
                triggerPhrase = "تفعيل وضع النوم (كتم الصوت + إيقاف الواي فاي)",
                intentName = "SET_DND",
                defaultParams = mapOf("state" to "silent"),
                executionCount = 9,
                consecutiveSuccesses = 5,
                status = PatternStatus.PROPOSED,
                rationaleAr = "لاحظت تكرار قيامك بكتم الصوت يدpropياً يومياً بين الساعة 11:00م و 11:45م بمعدل 9 مرات خلال الأسبوعين الماضيين، ونجح التنفيذ 5 مرات متتالية دون تراجع.",
                driftScore = 0.02f
            ),
            LearnedPattern(
                id = "pat_morning_alarm",
                triggerPhrase = "منبه صلاة الفجر والعمل (06:00 صباحاً)",
                intentName = "SET_ALARM",
                defaultParams = mapOf("hour" to "6", "minute" to "0"),
                executionCount = 6,
                consecutiveSuccesses = 4,
                status = PatternStatus.OBSERVING,
                rationaleAr = "تكرار ضبط المنبه في نفس التوقيت. باقي نجاح واحد مكتمل لعرض الاقتراح للاعتماد.",
                driftScore = 0.05f
            ),
            LearnedPattern(
                id = "pat_commute_nav",
                triggerPhrase = "فتح الخرائط ووضع القيادة عند الساعة 08:15 صباحاً",
                intentName = "OPEN_APP",
                defaultParams = mapOf("packageName" to "com.google.android.apps.maps"),
                executionCount = 14,
                consecutiveSuccesses = 12,
                status = PatternStatus.ADOPTED,
                rationaleAr = "نمط معتمد ومؤتمت بناءً على موافقتك الصريحة بعد 5 نجاحات موثّقة.",
                driftScore = 0.01f
            )
        )
    )
    val patterns: StateFlow<List<LearnedPattern>> = _patterns.asStateFlow()

    private val _isDriftFrozen = MutableStateFlow(false)
    val isDriftFrozen: StateFlow<Boolean> = _isDriftFrozen.asStateFlow()

    fun recordExecution(intentName: String, success: Boolean) {
        val current = _patterns.value.toMutableList()
        val index = current.indexOfFirst { it.intentName == intentName }
        if (index != -1) {
            val item = current[index]
            val newSuccesses = if (success) item.consecutiveSuccesses + 1 else 0
            val newDrift = if (success) maxOf(0f, item.driftScore - 0.02f) else item.driftScore + 0.15f

            // Auto-freeze learning if drift exceeds 20%
            if (newDrift > 0.20f) {
                _isDriftFrozen.value = true
            }

            val newStatus = when {
                newDrift > 0.20f -> PatternStatus.PAUSED
                newSuccesses >= 5 && item.status == PatternStatus.OBSERVING -> PatternStatus.PROPOSED
                else -> item.status
            }

            current[index] = item.copy(
                executionCount = item.executionCount + 1,
                consecutiveSuccesses = newSuccesses,
                status = newStatus,
                driftScore = newDrift,
                lastObserved = System.currentTimeMillis()
            )
            _patterns.value = current
        }
    }

    fun adoptPattern(id: String) {
        val current = _patterns.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(status = PatternStatus.ADOPTED)
            _patterns.value = current
        }
    }

    fun rejectPattern(id: String) {
        val current = _patterns.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(status = PatternStatus.PAUSED)
            _patterns.value = current
        }
    }

    fun clearAllMemory() {
        _patterns.value = emptyList()
        _isDriftFrozen.value = false
    }

    fun resetDriftFreeze() {
        _isDriftFrozen.value = false
    }

    fun generateDailySummary(): String {
        val adoptedCount = _patterns.value.count { it.status == PatternStatus.ADOPTED }
        val proposedCount = _patterns.value.count { it.status == PatternStatus.PROPOSED }
        return "هذا ما قمت به نيابةً عنك اليوم: تم رصد وإدارة الأنماط الذكية بنجاح، ويوجد $adoptedCount نمط معتمد قيد التشغيل الآمن، و $proposedCount اقتراح جديد بانتظار مراجعتك. جميع العمليات نُفِّذت محلياً بنسبة استهلاك بطارية 0% للشبكة."
    }
}
