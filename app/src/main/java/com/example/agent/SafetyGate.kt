package com.example.agent

import com.example.model.ActionPlan
import com.example.model.ActionStep
import com.example.model.PlanStatus
import com.example.model.RiskLevel

class SafetyGate {

    // Set of intents explicitly approved for L2 auto-execution
    private val userWhitelistedIntents = mutableSetOf(
        "TOGGLE_WIFI",
        "FLASHLIGHT",
        "MEDIA_CONTROL"
    )

    fun evaluatePlan(plan: ActionPlan, consecutiveSuccesses: Int): ActionPlan {
        if (plan.status == PlanStatus.BLOCKED || plan.status == PlanStatus.AMBIGUOUS_NEEDS_CLARIFICATION) {
            return plan
        }

        // Assess overall risk
        val highestRisk = plan.steps.maxOfOrNull { it.riskLevel } ?: RiskLevel.L1

        val requiresConfirmation = when (highestRisk) {
            RiskLevel.L0 -> true // Suggestions always require acceptance
            RiskLevel.L1 -> true // L1 always requires manual user confirmation
            RiskLevel.L2 -> {
                // Allowed to be automatic ONLY if on whitelist AND has at least 5 consecutive successes
                val isWhitelisted = userWhitelistedIntents.contains(plan.intentName)
                val isReliable = consecutiveSuccesses >= 5
                !(isWhitelisted && isReliable)
            }
            RiskLevel.L3 -> true // Absolute blocked
        }

        val updatedStatus = if (highestRisk == RiskLevel.L3) {
            PlanStatus.BLOCKED
        } else {
            PlanStatus.PENDING_APPROVAL
        }

        return plan.copy(
            overallRisk = highestRisk,
            requiresConfirmation = requiresConfirmation,
            status = updatedStatus
        )
    }

    fun whitelistIntent(intentName: String) {
        userWhitelistedIntents.add(intentName)
    }

    fun revokeWhitelist(intentName: String) {
        userWhitelistedIntents.remove(intentName)
    }

    fun isWhitelisted(intentName: String): Boolean {
        return userWhitelistedIntents.contains(intentName)
    }
}
