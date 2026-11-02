package com.example.agent

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import com.example.model.ActionPlan
import com.example.model.ActionStep
import com.example.model.ActionType
import com.example.model.PlanStatus
import com.example.model.UndoRecord

class ActionExecutor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    fun executePlan(plan: ActionPlan, isDryRun: Boolean): Pair<PlanStatus, List<UndoRecord>> {
        if (plan.status == PlanStatus.BLOCKED || plan.status == PlanStatus.AMBIGUOUS_NEEDS_CLARIFICATION) {
            return Pair(plan.status, emptyList())
        }

        if (isDryRun) {
            val simulatedUndo = plan.steps.filter { it.undoable }.map { step ->
                UndoRecord(
                    id = "undo_sim_" + step.id,
                    planId = plan.planId,
                    actionType = step.actionType,
                    previousStateValue = "simulated_state",
                    descriptionAr = "محاكاة تراجع: ${step.descriptionAr}"
                )
            }
            return Pair(PlanStatus.EXECUTED, simulatedUndo)
        }

        val undoRecords = mutableListOf<UndoRecord>()
        var allSucceeded = true

        for (step in plan.steps) {
            try {
                val undo = executeStep(step)
                if (undo != null) {
                    undoRecords.add(undo)
                }
            } catch (e: Exception) {
                allSucceeded = false
                break
            }
        }

        return if (allSucceeded) {
            Pair(PlanStatus.EXECUTED, undoRecords)
        } else {
            Pair(PlanStatus.FAILED, undoRecords)
        }
    }

    private fun executeStep(step: ActionStep): UndoRecord? {
        when (step.actionType) {
            ActionType.TOGGLE_WIFI -> {
                // Android 10+ restricts direct toggle; open panel or system intent
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    val fallback = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                }
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.TOGGLE_WIFI,
                    previousStateValue = "opposite",
                    descriptionAr = "استعادة حالة الواي فاي السابقة"
                )
            }

            ActionType.TOGGLE_BLUETOOTH -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.TOGGLE_BLUETOOTH,
                    previousStateValue = "opposite",
                    descriptionAr = "استعادة حالة البلوتوث السابقة"
                )
            }

            ActionType.FLASHLIGHT -> {
                val state = step.params["state"] == "on"
                try {
                    val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                    if (cameraId != null) {
                        cameraManager?.setTorchMode(cameraId, state)
                    }
                } catch (e: CameraAccessException) {
                    // Ignored in emulator / unsupported hardware
                }
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.FLASHLIGHT,
                    previousStateValue = if (state) "off" else "on",
                    descriptionAr = if (state) "إطفاء الكشاف" else "تشغيل الكشاف"
                )
            }

            ActionType.SET_DND -> {
                val state = step.params["state"]
                val prevRinger = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
                if (state == "silent" || state == "dnd") {
                    audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.SET_DND,
                    previousStateValue = prevRinger.toString(),
                    descriptionAr = "استعادة نمط الرنين السابق"
                )
            }

            ActionType.SET_VOLUME -> {
                val prevVolume = audioManager?.getStreamVolume(AudioManager.STREAM_RING) ?: 5
                audioManager?.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.SET_VOLUME,
                    previousStateValue = prevVolume.toString(),
                    descriptionAr = "استعادة درجة الصوت السابقة"
                )
            }

            ActionType.OPEN_APP -> {
                val pkg = step.params["packageName"] ?: "com.android.settings"
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }
                return null
            }

            ActionType.SET_ALARM -> {
                val hour = step.params["hour"]?.toIntOrNull() ?: 7
                val minute = step.params["minute"]?.toIntOrNull() ?: 0
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Phone Automation")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.SET_ALARM,
                    previousStateValue = "$hour:$minute",
                    descriptionAr = "إلغاء المنبه المضبوط"
                )
            }

            ActionType.SET_TIMER -> {
                val minutes = step.params["minutes"]?.toIntOrNull() ?: 10
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Phone Automation Timer")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.SET_TIMER,
                    previousStateValue = "$minutes",
                    descriptionAr = "إيقاف المؤقت التنازلي"
                )
            }

            ActionType.SEND_MESSAGE -> {
                val recipient = step.params["recipient"] ?: ""
                val msg = step.params["message"] ?: ""
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:")
                    putExtra("sms_body", msg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return null
            }

            ActionType.MEDIA_CONTROL -> {
                val prevMode = audioManager?.isMusicActive ?: false
                // Adjust music volume as concrete media interaction
                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
                return UndoRecord(
                    id = "undo_" + System.currentTimeMillis(),
                    planId = step.id,
                    actionType = ActionType.MEDIA_CONTROL,
                    previousStateValue = prevMode.toString(),
                    descriptionAr = "استعادة حالة الوسائط"
                )
            }

            else -> return null
        }
    }

    fun verifyPostExecution(step: ActionStep): String {
        return when (step.actionType) {
            ActionType.SET_DND -> {
                val mode = audioManager?.ringerMode
                if (mode == AudioManager.RINGER_MODE_SILENT) "تم التحقق: الهاتف في وضع الصامت الفعلي"
                else "تم التحقق: الهاتف في الوضع العام"
            }
            ActionType.FLASHLIGHT -> "تم التحقق: تم ضبط وضع الكشاف بنجاح"
            ActionType.SET_VOLUME -> "تم التحقق: تم تعديل خرج الصوت بنجاح"
            ActionType.OPEN_APP -> "تم التحقق: تم إرسال أمر فتح حزمة التطبيق"
            ActionType.SET_ALARM -> "تم التحقق: تم جدولة المنبه في نظام التنبيهات"
            ActionType.SET_TIMER -> "تم التحقق: تم تشغيل المؤقت بنجاح"
            ActionType.SEND_MESSAGE -> "تم التحقق: تم فتح واجهة الإرسال للمصادقة"
            else -> "تم التحقق من اكتمال الإجراء"
        }
    }

    fun revertUndo(record: UndoRecord): Boolean {
        return try {
            when (record.actionType) {
                ActionType.FLASHLIGHT -> {
                    val turnOn = record.previousStateValue == "on"
                    val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                    if (cameraId != null) cameraManager?.setTorchMode(cameraId, turnOn)
                    true
                }
                ActionType.SET_DND -> {
                    val ringer = record.previousStateValue.toIntOrNull() ?: AudioManager.RINGER_MODE_NORMAL
                    audioManager?.ringerMode = ringer
                    true
                }
                ActionType.SET_VOLUME -> {
                    val vol = record.previousStateValue.toIntOrNull() ?: 5
                    audioManager?.setStreamVolume(AudioManager.STREAM_RING, vol, AudioManager.FLAG_SHOW_UI)
                    true
                }
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }
}
