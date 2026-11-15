package com.example.littlerpg.domain.model

/**
 * Catégories d'épreuves pouvant survenir au cours du scénario.
 */
enum class EventTag {
    COMBAT,
    OBSTACLE,
    NPC,
    TRAP,
    RIDDLE
}

/**
 * Orientations initiales choisies par le joueur en début de partie (Phase 1).
 */
enum class ScenarioOrientation(val displayName: String, val actionText: String) {
    BOUNTY_HUNT("Chasseur de primes", "Tuer un monstre"),
    TREASURE_HUNT("Chasseur de trésor", "Trouver un grand trésor"),
    MYSTERY("Lever un mystère", "Résoudre une énigme"),
    NPC_ENCOUNTER("Rencontre PNJ", "Rencontrer un personnage haut en couleur")
}

/**
 * Modèle d'introduction dynamique basé sur un lieu et un contexte issus de la BDD.
 */
data class IntroTemplate(
    val id: Long = 0,
    val article: String,
    val place: String,
    val contextText: String,
    val universe: UniverseType
) {
    fun buildIntroMessage(characterName: String = ""): String {
        val namePrefix = if (characterName.isNotBlank()) "Bienvenue, brave $characterName. " else ""
        return "${namePrefix}Vous êtes dans $article $place, $contextText. Vous avez plein d'opportunités. Que voulez-vous faire ?"
    }
}

/**
 * Modèle représentatif d'une épreuve standard de niveau intermédiaire (Phase 2).
 */
data class StandardEvent(
    val id: String,
    val title: String,
    val description: String,
    val tag: EventTag,
    val universe: UniverseType = UniverseType.FANTASTIC,
    val action1Text: String = "Attaquer de face",
    val action1Skill: Skill = Skill.COMBAT,
    val action2Text: String = "Contourner discrètement",
    val action2Skill: Skill = Skill.LOCK_TRAP,
    val action3Text: String = "Forcer le passage",
    val action3Skill: Skill = Skill.LIFT_PUSH,
    val isMerchant: Boolean = false,
    val correctActionIndex: Int = 1
) {
    val requiredSkill: Skill
        get() = action1Skill

    fun toActionOptions(inventoryItem: String? = null): List<ActionOption> {
        if (isMerchant) {
            return listOf(
                ActionOption(1, action1Text, action1Skill),
                ActionOption(2, action2Text, action2Skill),
                ActionOption(3, action3Text, action3Skill),
                ActionOption(4, "🚫 Rien acheter", Skill.PERSUASION)
            )
        }
        val options = mutableListOf(
            ActionOption(1, action1Text, action1Skill),
            ActionOption(2, action2Text, action2Skill),
            ActionOption(3, action3Text, action3Skill)
        )
        val itemLabel = inventoryItem?.let { "Utiliser un objet : $it" } ?: "Utiliser un objet"
        options.add(
            ActionOption(
                id = 4,
                label = itemLabel,
                skill = Skill.MAGIC,
                isItemAction = true,
                itemName = inventoryItem
            )
        )
        return options
    }
}

/**
 * Modèle représentatif d'une épreuve de dénouement final (Phase 3).
 */
data class ClimaxEvent(
    val id: String,
    val title: String,
    val description: String,
    val finalBossName: String,
    val requiredSkill: Skill
)

/**
 * Conclusion narrative verrouillée dès le choix initial et révélée en fin de partie.
 */
data class ScenarioConclusion(
    val id: String,
    val orientation: ScenarioOrientation,
    val victoryText: String,
    val defeatText: String
)

/**
 * Machine à états finis du scénario (UDF / State-Machine).
 */
sealed interface ScenarioState {

    /**
     * Phase 1 : Introduction narrative et proposition des 4 orientations.
     */
    data class Intro(
        val welcomeMessage: String,
        val availableOrientations: List<ScenarioOrientation> = ScenarioOrientation.entries
    ) : ScenarioState

    /**
     * Phase 2 : Épreuve intermédiaire en cours.
     */
    data class MidGameEvent(
        val currentEventIndex: Int,
        val totalEvents: Int,
        val currentTag: EventTag,
        val event: StandardEvent,
        val lockedConclusion: ScenarioConclusion
    ) : ScenarioState

    /**
     * Phase 3 : Climax final.
     */
    data class ClimaxPhase(
        val climaxEvent: ClimaxEvent,
        val lockedConclusion: ScenarioConclusion
    ) : ScenarioState

    data class ConclusionPhase(
        val conclusionText: String,
        val isVictory: Boolean
    ) : ScenarioState

    /**
     * État terminal de la partie.
     */
    data class Finished(
        val isVictory: Boolean
    ) : ScenarioState
}
