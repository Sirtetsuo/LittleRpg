package com.example.littlerpg.domain.model

import java.util.UUID

enum class UniverseType(val displayName: String, val description: String) {
    FANTASTIC(
        displayName = "Fantastique",
        description = "Monde de magie, de donjons, de créatures anciennes et de reliques oubliées."
    ),
    CONTEMPORARY(
        displayName = "Contemporain",
        description = "Monde moderne rempli d'enquêtes sombres, d'espionnage et de conspirations."
    ),
    FUTURISTIC(
        displayName = "Futuriste",
        description = "Univers cyberpunk et spatial avec haute technologie, néons et cyber-implants."
    )
}

enum class Attribute(val displayName: String) {
    STRENGTH("Force"),
    INTELLIGENCE("Intelligence"),
    AGILITY("Agilité")
}

enum class Skill(val displayName: String, val attribute: Attribute) {
    // FORCE
    COMBAT("Combat", Attribute.STRENGTH),
    LIFT_PUSH("Pousser / Soulever", Attribute.STRENGTH),
    BREAK("Casser", Attribute.STRENGTH),

    // INTELLIGENCE
    MAGIC("Magie", Attribute.INTELLIGENCE),
    LOCK_TRAP("Serrure et piège", Attribute.INTELLIGENCE),
    PERSUASION("Persuasion", Attribute.INTELLIGENCE),

    // AGILITÉ
    SHOOT("Tir", Attribute.AGILITY),
    CLIMB("Escalade", Attribute.AGILITY),
    EXTREME_JUMP("Saut extrême", Attribute.AGILITY);

    companion object {
        fun getByAttribute(attribute: Attribute): List<Skill> {
            return entries.filter { it.attribute == attribute }
        }
    }
}

data class ActionOption(
    val id: Int,
    val label: String,
    val skill: Skill,
    val isItemAction: Boolean = false,
    val itemName: String? = null,
    val orientation: ScenarioOrientation? = null
)

/**
 * Types d'effets standardisés pour les objets du jeu.
 */
sealed interface ItemEffect {
    data class Heal(val dice: Boolean = false, val amount: Int = 0) : ItemEffect
    data class TempBoost(val attribute: Attribute, val amount: Int = 3) : ItemEffect
    data class AutoSuccess(val attribute: Attribute) : ItemEffect
    data class PermanentBonus(
        val strBonus: Int = 0,
        val intBonus: Int = 0,
        val agiBonus: Int = 0,
        val hpBonus: Int = 0
    ) : ItemEffect
}

/**
 * Modèle représentatif d'un objet de l'inventaire.
 */
data class GameItem(
    val id: Int,
    val name: String,
    val effectDescription: String,
    val isConsumable: Boolean = true,
    val universe: UniverseType = UniverseType.FANTASTIC
) {
    val cost: Int
        get() = if (isConsumable) 80 else 250

    val parsedEffect: ItemEffect
        get() = parseEffect(effectDescription, isConsumable)

    companion object {
        fun parseEffect(effectText: String, isConsumable: Boolean): ItemEffect {
            val text = effectText.trim()
            return when {
                text.contains("1d6", ignoreCase = true) -> ItemEffect.Heal(dice = true)
                text == "AUTO_FOR" -> ItemEffect.AutoSuccess(Attribute.STRENGTH)
                text == "AUTO_INT" -> ItemEffect.AutoSuccess(Attribute.INTELLIGENCE)
                text == "AUTO_AGI" -> ItemEffect.AutoSuccess(Attribute.AGILITY)
                text.contains("(1 tour)", ignoreCase = true) -> {
                    when {
                        text.contains("FOR") -> ItemEffect.TempBoost(Attribute.STRENGTH, 3)
                        text.contains("INT") -> ItemEffect.TempBoost(Attribute.INTELLIGENCE, 3)
                        text.contains("AGI") -> ItemEffect.TempBoost(Attribute.AGILITY, 3)
                        else -> ItemEffect.TempBoost(Attribute.STRENGTH, 3)
                    }
                }
                text.contains("+1 FOR, +1 INT, +1 AGI") || (text.contains("FOR") && text.contains("INT") && text.contains("AGI")) -> {
                    ItemEffect.PermanentBonus(strBonus = 1, intBonus = 1, agiBonus = 1)
                }
                !isConsumable -> {
                    var str = 0
                    var intV = 0
                    var agi = 0
                    var hp = 0
                    val parts = text.split(",")
                    for (p in parts) {
                        val pt = p.trim()
                        val num = Regex("""\+(\d+)""").find(pt)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                        when {
                            pt.contains("FOR") -> str += num
                            pt.contains("INT") -> intV += num
                            pt.contains("AGI") -> agi += num
                            pt.contains("PV") -> hp += num
                        }
                    }
                    ItemEffect.PermanentBonus(strBonus = str, intBonus = intV, agiBonus = agi, hpBonus = hp)
                }
                text.contains("PV") -> {
                    val amount = Regex("""\+(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    ItemEffect.Heal(amount = amount)
                }
                else -> ItemEffect.Heal(amount = 0)
            }
        }

        fun fromName(name: String): GameItem {
            val trimmed = name.trim()
            return when {
                trimmed.equals("Épée rouillée", ignoreCase = true) || trimmed.contains("épée rouillée", ignoreCase = true) ->
                    GameItem(0, "Épée rouillée", "Arme de départ (aucun bonus)", isConsumable = false, universe = UniverseType.FANTASTIC)
                trimmed.equals("Pistolet", ignoreCase = true) ->
                    GameItem(1000, "Pistolet", "Arme de départ (aucun bonus)", isConsumable = false, universe = UniverseType.CONTEMPORARY)
                trimmed.equals("Blaster", ignoreCase = true) ->
                    GameItem(2000, "Blaster", "Arme de départ (aucun bonus)", isConsumable = false, universe = UniverseType.FUTURISTIC)
                else -> GameItemCatalog.getItemByName(name) ?: GameItem(
                    id = name.hashCode(),
                    name = name,
                    effectDescription = "Effet spécial",
                    isConsumable = true
                )
            }
        }
    }
}



data class Character(
    val name: String,
    val nickname: String,
    val age: Int,
    val strength: Int,
    val intelligence: Int,
    val agility: Int,
    val hp: Int = 20,
    val maxHp: Int = 20,
    val level: Int = 1,
    val inventory: List<String> = listOf("Épée rouillée"),
    val gold: Int = 100,
    val temporaryBoosts: Map<Attribute, Int> = emptyMap()
) {
    fun getAttributeValue(attribute: Attribute): Int {
        return when (attribute) {
            Attribute.STRENGTH -> strength
            Attribute.INTELLIGENCE -> intelligence
            Attribute.AGILITY -> agility
        }
    }

    fun getPermanentItemBonus(attribute: Attribute): Int {
        var bonus = 0
        for (itemName in inventory) {
            val item = GameItem.fromName(itemName)
            if (!item.isConsumable) {
                val effect = item.parsedEffect
                if (effect is ItemEffect.PermanentBonus) {
                    bonus += when (attribute) {
                        Attribute.STRENGTH -> effect.strBonus
                        Attribute.INTELLIGENCE -> effect.intBonus
                        Attribute.AGILITY -> effect.agiBonus
                    }
                }
            }
        }
        return bonus
    }

    fun getPermanentHpBonus(): Int {
        var hpBonus = 0
        for (itemName in inventory) {
            val item = GameItem.fromName(itemName)
            if (!item.isConsumable) {
                val effect = item.parsedEffect
                if (effect is ItemEffect.PermanentBonus) {
                    hpBonus += effect.hpBonus
                }
            }
        }
        return hpBonus
    }

    fun getEffectiveMaxHp(): Int = maxHp + getPermanentHpBonus()

    fun getEffectiveAttributeValue(attribute: Attribute): Int {
        val base = getAttributeValue(attribute)
        val perm = getPermanentItemBonus(attribute)
        val temp = temporaryBoosts[attribute] ?: 0
        return base + perm + temp
    }
}

data class ActionResolution(
    val diceRoll: Int,
    val targetValue: Int,
    val isSuccess: Boolean,
    val hpLost: Int,
    val remainingHp: Int,
    val skill: Skill
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromGameMaster: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

