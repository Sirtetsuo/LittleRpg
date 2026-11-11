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
enum class ScenarioOrientation(val displayName: String) {
    BOUNTY_HUNT("Chasseur de primes"),
    TREASURE_HUNT("Chasseur de trésor"),
    MYSTERY("Lever un mystère"),
    NPC_ENCOUNTER("Rencontre PNJ")
}

/**
 * Modèle représentatif d'une épreuve standard de niveau intermédiaire (Phase 2).
 */
data class StandardEvent(
    val id: String,
    val title: String,
    val description: String,
    val tag: EventTag,
    val requiredSkill: Skill
)

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
