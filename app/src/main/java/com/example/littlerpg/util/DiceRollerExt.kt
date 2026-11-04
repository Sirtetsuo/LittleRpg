package com.example.littlerpg.util

import kotlin.random.Random

data class DiceRollResult(
    val player: String,
    val skillName: String,
    val targetThreshold: Int,
    val rollValue: Int,
    val isSuccess: Boolean
) {
    fun toChatMessage(): String = """
        🎲 [JET DE DÉ d100] pour $player sur "$skillName"
        • Seuil de compétence : $targetThreshold%
        • Résultat du d100 : $rollValue
        ➔ Résultat : ${if (isSuccess) "🎉 SUCCÈS ! (Action réussie)" else "💥 ÉCHEC ! (Blessure subie ou action ratée)"}
    """.trimIndent()
}

fun executeD100Roll(
    player: String,
    skillName: String,
    targetThreshold: Int
): DiceRollResult {
    val rollValue = Random.nextInt(1, 101)
    val isSuccess = rollValue <= targetThreshold
    return DiceRollResult(
        player = player,
        skillName = skillName,
        targetThreshold = targetThreshold,
        rollValue = rollValue,
        isSuccess = isSuccess
    )
}
