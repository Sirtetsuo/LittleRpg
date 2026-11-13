package com.example.littlerpg.domain.engine

import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.GameItem
import com.example.littlerpg.domain.model.ItemEffect
import kotlin.random.Random

data class ItemUseResult(
    val updatedCharacter: Character,
    val messageText: String,
    val isAutoSuccess: Boolean,
    val autoSuccessAttribute: com.example.littlerpg.domain.model.Attribute? = null
)

class GameItemHandler {

    fun useItem(itemName: String, character: Character): ItemUseResult {
        val item = GameItem.fromName(itemName)
        val effect = item.parsedEffect

        var isAuto = false
        var autoAttribute: com.example.littlerpg.domain.model.Attribute? = null

        val (updatedCharacter, effectMsgText) = when (effect) {
            is ItemEffect.Heal -> {
                val roll = if (effect.dice) Random.nextInt(1, 7) else effect.amount
                val maxHp = character.getEffectiveMaxHp()
                val newHp = (character.hp + roll).coerceAtMost(maxHp)
                val healed = newHp - character.hp
                val newInventory = if (item.isConsumable) character.inventory.toMutableList().apply { remove(itemName) } else character.inventory
                val charUpdated = character.copy(hp = newHp, inventory = newInventory)
                Pair(
                    charUpdated,
                    "🧪 SOIN REÇU !\n" +
                            "Objet : $itemName (${item.effectDescription})\n" +
                            "Gain : +$healed PV restaurés ! (${charUpdated.hp}/$maxHp PV actuels)." +
                            if (item.isConsumable) " L'objet est consommé." else ""
                )
            }
            is ItemEffect.TempBoost -> {
                val updatedBoosts = character.temporaryBoosts + (effect.attribute to effect.amount)
                val newInventory = if (item.isConsumable) character.inventory.toMutableList().apply { remove(itemName) } else character.inventory
                val charUpdated = character.copy(temporaryBoosts = updatedBoosts, inventory = newInventory)
                Pair(
                    charUpdated,
                    "⚡ STIMULANT ACTIF !\n" +
                            "Objet : $itemName (${item.effectDescription})\n" +
                            "Effet : +${effect.amount} en ${effect.attribute.displayName} pour la prochaine épreuve !" +
                            if (item.isConsumable) " L'objet est consommé." else ""
                )
            }
            is ItemEffect.AutoSuccess -> {
                isAuto = true
                autoAttribute = effect.attribute
                val newInventory = if (item.isConsumable) character.inventory.toMutableList().apply { remove(itemName) } else character.inventory
                val charUpdated = character.copy(inventory = newInventory)
                Pair(
                    charUpdated,
                    "⚡ RÉUSSITE AUTOMATIQUE !\n" +
                            "Objet : $itemName (${item.effectDescription})\n" +
                            "Effet : Valide automatiquement l'épreuve liée à la ${effect.attribute.displayName} !" +
                            if (item.isConsumable) " L'objet est consommé." else ""
                )
            }
            is ItemEffect.PermanentBonus -> {
                Pair(
                    character,
                    "🛡️ ÉQUIPEMENT ACTIF !\n" +
                            "Objet : $itemName (${item.effectDescription})\n" +
                            "Cet objet accorde ses bonus permanents tant qu'il reste dans votre inventaire."
                )
            }
        }

        return ItemUseResult(
            updatedCharacter = updatedCharacter,
            messageText = effectMsgText,
            isAutoSuccess = isAuto,
            autoSuccessAttribute = autoAttribute
        )
    }
}
