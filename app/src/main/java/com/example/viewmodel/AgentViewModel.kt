package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.ActionExecutor
import com.example.agent.LearnerEngine
import com.example.agent.OfflineIntentParser
import com.example.agent.SafetyGate
import com.example.agent.UndoManager
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.model.ActionPlan
import com.example.model.ActionStep
import com.example.model.ActionType
import com.example.model.AuditLogEntry
import com.example.model.LearnedPattern
import com.example.model.PlanStatus
import com.example.model.RiskLevel
import com.example.model.Routine
import com.example.model.TriggerType
import com.example.model.UndoRecord
import com.example.service.AgentForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class Screen(val titleAr: String) {
    ONBOARDING("التفعيل والإذونات"),
    HOME("الرئيسية"),
    STUDIO("استوديو الروتينات"),
    MEMORY("الذاكرة والتعلّم"),
    AUDIT("السجل والتدقيق"),
    SETTINGS("الإعدادات")
}

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val parser = OfflineIntentParser()
    private val executor = ActionExecutor(application)
    val undoManager = UndoManager(executor)
    val safetyGate = SafetyGate()
    val learnerEngine = LearnerEngine()

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Command Center state
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _activePlan = MutableStateFlow<ActionPlan?>(null)
    val activePlan: StateFlow<ActionPlan?> = _activePlan.asStateFlow()

    private val _lastExecutionMessage = MutableStateFlow<String?>(null)
    val lastExecutionMessage: StateFlow<String?> = _lastExecutionMessage.asStateFlow()

    // Settings & Toggles
    private val _isDryRunMode = MutableStateFlow(false)
    val isDryRunMode: StateFlow<Boolean> = _isDryRunMode.asStateFlow()

    private val _isLightweightMode = MutableStateFlow(false)
    val isLightweightMode: StateFlow<Boolean> = _isLightweightMode.asStateFlow()

    private val _selectedAsrEngine = MutableStateFlow("Whisper.cpp Quantized (موصى به)")
    val selectedAsrEngine: StateFlow<String> = _selectedAsrEngine.asStateFlow()

    private val _autoPrune30Days = MutableStateFlow(true)
    val autoPrune30Days: StateFlow<Boolean> = _autoPrune30Days.asStateFlow()

    // Audit logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Routines
    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines.asStateFlow()

    // Stats
    private val _totalCommands = MutableStateFlow(24)
    val totalCommands: StateFlow<Int> = _totalCommands.asStateFlow()

    private val _successRate = MutableStateFlow("96.4%")
    val successRate: StateFlow<String> = _successRate.asStateFlow()

    private val _avgLatency = MutableStateFlow(18) // milliseconds
    val avgLatency: StateFlow<Int> = _avgLatency.asStateFlow()

    init {
        // Start Foreground Service
        AgentForegroundService.start(application)

        // Seed initial routines with conflict detection demonstration
        seedInitialRoutines()
        seedInitialLogs()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun toggleListening() {
        val next = !_isListening.value
        _isListening.value = next
        if (next) {
            // Simulate voice transcript sample
            viewModelScope.launch {
                kotlinx.coroutines.delay(1600)
                if (_isListening.value) {
                    _inputText.value = "شغل الواي فاي وحط التلفون صامت"
                    _isListening.value = false
                    submitCommand()
                }
            }
        }
    }

    fun toggleDryRun() {
        _isDryRunMode.value = !_isDryRunMode.value
    }

    fun toggleLightweightMode() {
        _isLightweightMode.value = !_isLightweightMode.value
    }

    fun setAsrEngine(engine: String) {
        _selectedAsrEngine.value = engine
    }

    fun submitCommand(overrideText: String? = null) {
        val text = overrideText ?: _inputText.value
        if (text.isBlank()) return

        val startTime = System.currentTimeMillis()
        val rawPlan = parser.parse(text, isDryRun = _isDryRunMode.value)
        val evaluatedPlan = safetyGate.evaluatePlan(rawPlan, consecutiveSuccesses = 5)
        val latency = System.currentTimeMillis() - startTime

        _activePlan.value = evaluatedPlan
        _inputText.value = ""

        // If the plan does not require confirmation (e.g. whitelisted L2), execute immediately!
        if (!evaluatedPlan.requiresConfirmation && evaluatedPlan.status != PlanStatus.BLOCKED && evaluatedPlan.status != PlanStatus.AMBIGUOUS_NEEDS_CLARIFICATION) {
            confirmAndExecutePlan(evaluatedPlan, latency)
        }
    }

    fun confirmAndExecutePlan(plan: ActionPlan, latencyMs: Long = 14) {
        viewModelScope.launch {
            val (status, undos) = withContext(Dispatchers.IO) {
                executor.executePlan(plan, isDryRun = _isDryRunMode.value)
            }

            undoManager.pushRecords(undos)
            learnerEngine.recordExecution(plan.intentName, success = (status == PlanStatus.EXECUTED))

            val verification = if (plan.steps.isNotEmpty()) executor.verifyPostExecution(plan.steps.first()) else "تم التنفيذ"
            _lastExecutionMessage.value = if (_isDryRunMode.value) {
                "وضع المحاكاة (Dry-Run): تم اختبار الخطة بنجاح دون أي تغييرات حقيقية على النظام."
            } else {
                verification
            }

            _activePlan.value = plan.copy(status = status)

            // Record to Audit Logs
            val resultStr = if (_isDryRunMode.value) "محاكاة ناجحة" else if (status == PlanStatus.EXECUTED) "نجاح" else "فشل"
            val newEntry = AuditLogEntry(
                id = System.currentTimeMillis(),
                timestamp = System.currentTimeMillis(),
                commandText = plan.userPrompt,
                planSummary = plan.steps.joinToString(" • ") { it.descriptionAr },
                riskLevel = plan.overallRisk,
                executionResult = resultStr,
                latencyMs = latencyMs,
                isDryRun = _isDryRunMode.value,
                verifiedState = verification
            )

            _auditLogs.value = listOf(newEntry) + _auditLogs.value
            _totalCommands.value = _totalCommands.value + 1
        }
    }

    fun dismissActivePlan() {
        _activePlan.value = null
    }

    fun revertTopUndo() {
        val (success, msg) = undoManager.revertTop()
        _lastExecutionMessage.value = msg
        if (success) {
            val revertLog = AuditLogEntry(
                id = System.currentTimeMillis(),
                timestamp = System.currentTimeMillis(),
                commandText = "أمر التراجع السريع (Undo)",
                planSummary = msg,
                riskLevel = RiskLevel.L2,
                executionResult = "تم التراجع",
                latencyMs = 8,
                isDryRun = _isDryRunMode.value,
                verifiedState = "تم استعادة الحالة السابقة"
            )
            _auditLogs.value = listOf(revertLog) + _auditLogs.value
        }
    }

    fun revertSpecificUndo(id: String) {
        val (success, msg) = undoManager.revertItem(id)
        _lastExecutionMessage.value = msg
    }

    fun toggleRoutine(routineId: String) {
        val current = _routines.value.toMutableList()
        val index = current.indexOfFirst { it.id == routineId }
        if (index != -1) {
            val r = current[index]
            current[index] = r.copy(isEnabled = !r.isEnabled)
            _routines.value = detectConflicts(current)
        }
    }

    fun adoptLearnedPattern(patternId: String) {
        learnerEngine.adoptPattern(patternId)
        _lastExecutionMessage.value = "تم اعتماد النمط وترقيته إلى الأتمتة الموثّقة بنجاح!"
    }

    fun rejectLearnedPattern(patternId: String) {
        learnerEngine.rejectPattern(patternId)
        _lastExecutionMessage.value = "تم رفض الاقتراح وتوقيف مراقبته."
    }

    fun clearLearnedMemory() {
        learnerEngine.clearAllMemory()
        _lastExecutionMessage.value = "تم مسح كافة سجلات التعلّم والأنماط بالكامل."
    }

    fun clearAuditLogs() {
        _auditLogs.value = emptyList()
        _lastExecutionMessage.value = "تم تفريغ سجل التدقيق المحلي بالكامل."
    }

    private fun detectConflicts(list: List<Routine>): List<Routine> {
        val active = list.filter { it.isEnabled && it.triggerType == TriggerType.SCHEDULED }
        return list.map { r ->
            if (!r.isEnabled || r.triggerType != TriggerType.SCHEDULED) {
                r.copy(conflictWarning = null)
            } else {
                val conflicting = active.firstOrNull { it.id != r.id && it.triggerTime == r.triggerTime }
                if (conflicting != null) {
                    val winner = if (r.priority < conflicting.priority) r.nameAr else conflicting.nameAr
                    r.copy(conflictWarning = "تعارض زمني عند ${r.triggerTime} مع [${conflicting.nameAr}]. الأولوية لـ: $winner")
                } else {
                    r.copy(conflictWarning = null)
                }
            }
        }
    }

    private fun seedInitialRoutines() {
        val samplePlan1 = ActionPlan(
            planId = "plan_sleep",
            userPrompt = "وضع النوم العميق",
            intentName = "DEEP_SLEEP",
            steps = listOf(
                ActionStep("s1", ActionType.SET_DND, "silent", mapOf("state" to "silent"), RiskLevel.L2, false, true, true, "تفعيل الوضع الصامت (DND)", "Enable DND"),
                ActionStep("s2", ActionType.TOGGLE_WIFI, "off", mapOf("state" to "off"), RiskLevel.L2, false, true, true, "إيقاف شبكة الواي فاي", "Turn off Wi-Fi")
            ),
            overallRisk = RiskLevel.L2,
            requiresConfirmation = false
        )

        val samplePlan2 = ActionPlan(
            planId = "plan_work",
            userPrompt = "وضع العمل والتركيز",
            intentName = "WORK_FOCUS",
            steps = listOf(
                ActionStep("s3", ActionType.SET_VOLUME, "mute", mapOf("action" to "mute"), RiskLevel.L2, false, true, true, "كتم نغمات الرنين والإشعارات", "Mute ringtone"),
                ActionStep("s4", ActionType.OPEN_APP, "calendar", mapOf("packageName" to "com.google.android.calendar"), RiskLevel.L2, false, false, true, "فتح التقويم وجدول الأعمال", "Open calendar")
            ),
            overallRisk = RiskLevel.L2,
            requiresConfirmation = false
        )

        val samplePlan3 = ActionPlan(
            planId = "plan_powersave",
            userPrompt = "توفير الطاقة الليلي",
            intentName = "NIGHT_SAVER",
            steps = listOf(
                ActionStep("s5", ActionType.TOGGLE_BLUETOOTH, "off", mapOf("state" to "off"), RiskLevel.L2, false, true, true, "إيقاف البلوتوث", "Turn off Bluetooth"),
                ActionStep("s6", ActionType.TOGGLE_WIFI, "off", mapOf("state" to "off"), RiskLevel.L2, false, true, true, "إيقاف الواي فاي", "Turn off Wi-Fi")
            ),
            overallRisk = RiskLevel.L2,
            requiresConfirmation = false
        )

        val initialList = listOf(
            Routine(
                id = "rout_1",
                nameAr = "وضع النوم الهادئ",
                descriptionAr = "كتم كافة الإشعارات وإيقاف الواي فاي تلقائياً للنوم دون إزعاج",
                triggerType = TriggerType.SCHEDULED,
                triggerTime = "23:30",
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                priority = 1,
                isEnabled = true,
                plan = samplePlan1
            ),
            Routine(
                id = "rout_2",
                nameAr = "وضع العمل والتركيز",
                descriptionAr = "كتم الرنين وعرض جدول المواعيد عند بدء دوام العمل",
                triggerType = TriggerType.SCHEDULED,
                triggerTime = "08:30",
                daysOfWeek = listOf(1, 2, 3, 4, 5),
                priority = 2,
                isEnabled = true,
                plan = samplePlan2
            ),
            Routine(
                id = "rout_3",
                nameAr = "توفير الطاقة الليلي",
                descriptionAr = "تعارض تجريبي: مجدول في نفس توقيت وضع النوم 23:30 لاختبار كشف التعارض",
                triggerType = TriggerType.SCHEDULED,
                triggerTime = "23:30",
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                priority = 3,
                isEnabled = true,
                plan = samplePlan3
            )
        )

        _routines.value = detectConflicts(initialList)
    }

    private fun seedInitialLogs() {
        _auditLogs.value = listOf(
            AuditLogEntry(
                id = 1,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
                commandText = "شغل الواي فاي",
                planSummary = "تفعيل شبكة الواي فاي (Intent Cache)",
                riskLevel = RiskLevel.L2,
                executionResult = "نجاح",
                latencyMs = 11,
                isDryRun = false,
                verifiedState = "تم التحقق: لوحة الشبكة جاهزة"
            ),
            AuditLogEntry(
                id = 2,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                commandText = "حط التلفون صامت",
                planSummary = "تفعيل وضع عدم الإزعاج والوضع الصامت",
                riskLevel = RiskLevel.L2,
                executionResult = "نجاح",
                latencyMs = 14,
                isDryRun = false,
                verifiedState = "تم التحقق: الهاتف في وضع الصامت الفعلي"
            ),
            AuditLogEntry(
                id = 3,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                commandText = "فرمت الجهاز وامسح كل شي",
                planSummary = "تم اعتراض الأمر: إجراء عالي الخطورة محظور نهائياً",
                riskLevel = RiskLevel.L3,
                executionResult = "محظور",
                latencyMs = 4,
                isDryRun = false,
                verifiedState = "محمي بواسطة بوابة الأمان L3"
            )
        )
    }
}
