package com.example.littlerpg.domain.engine

import com.example.littlerpg.domain.model.ActionResolution
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.Skill
import kotlin.math.max
import kotlin.random.Random

class RuleEngine(private val random: Random = Random.Default) {

    fun rollD20(): Int {
        return random.nextInt(1, 21)
    }

    fun resolve(
        skill: Skill,
        character: Character,
        isItemAction: Boolean = false,
        forcedRoll: Int? = null
    ): Pair<ActionResolution, Character> {
        val diceRoll = if (isItemAction) 1 else (forcedRoll ?: rollD20())
        val targetValue = character.getAttributeValue(skill.attribute)
        val isSuccess = if (isItemAction) true else (diceRoll <= targetValue)
        val hpLost = if (isSuccess) 0 else 1
        val newHp = max(0, character.hp - hpLost)

        val updatedCharacter = character.copy(hp = newHp)
        val resolution = ActionResolution(
            diceRoll = diceRoll,
            targetValue = targetValue,
            isSuccess = isSuccess,
            hpLost = hpLost,
            remainingHp = newHp,
            skill = skill
        )

        return Pair(resolution, updatedCharacter)
    }
}
