package com.example.agent

import com.example.model.UndoRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UndoManager(private val executor: ActionExecutor) {

    private val maxStackSize = 20
    private val _undoStack = MutableStateFlow<List<UndoRecord>>(emptyList())
    val undoStack: StateFlow<List<UndoRecord>> = _undoStack.asStateFlow()

    fun pushRecords(records: List<UndoRecord>) {
        if (records.isEmpty()) return
        val current = _undoStack.value.toMutableList()
        current.addAll(0, records)
        if (current.size > maxStackSize) {
            _undoStack.value = current.take(maxStackSize)
        } else {
            _undoStack.value = current
        }
    }

    fun revertTop(): Pair<Boolean, String> {
        val current = _undoStack.value.toMutableList()
        val itemToRevert = current.firstOrNull { !it.isReverted }
            ?: return Pair(false, "لا توجد عمليات قابلة للتراجع في السجل")

        val success = executor.revertUndo(itemToRevert)
        if (success) {
            val idx = current.indexOf(itemToRevert)
            current[idx] = itemToRevert.copy(isReverted = true)
            _undoStack.value = current
            return Pair(true, "تم التراجع عن: ${itemToRevert.descriptionAr}")
        }
        return Pair(false, "تعذر التراجع عن الإجراء من نظام التشغيل")
    }

    fun revertItem(id: String): Pair<Boolean, String> {
        val current = _undoStack.value.toMutableList()
        val item = current.find { it.id == id && !it.isReverted }
            ?: return Pair(false, "العملية غير موجودة أو تم التراجع عنها مسبقاً")

        val success = executor.revertUndo(item)
        if (success) {
            val idx = current.indexOf(item)
            current[idx] = item.copy(isReverted = true)
            _undoStack.value = current
            return Pair(true, "تم التراجع بنجاح")
        }
        return Pair(false, "فشل استعادة الحالة")
    }

    fun clear() {
        _undoStack.value = emptyList()
    }
}
