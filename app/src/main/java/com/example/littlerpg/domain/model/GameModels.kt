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
    val itemName: String? = null
)

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
    val inventory: List<String> = listOf("Une épée rouillée", "Une gourde", "Une potion de soin")
) {
    fun getAttributeValue(attribute: Attribute): Int {
        return when (attribute) {
            Attribute.STRENGTH -> strength
            Attribute.INTELLIGENCE -> intelligence
            Attribute.AGILITY -> agility
        }
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
