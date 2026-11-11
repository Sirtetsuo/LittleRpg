package com.example.littlerpg.domain.engine

import com.example.littlerpg.domain.model.ScenarioConclusion
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.ScenarioState
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.ScenarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Machine à états et gestionnaire de flux narratif (ScenarioEngine)
 * Orchestre le déroulement d'une partie selon une boucle événementielle déterministe.
 */
class ScenarioEngine(
    private val repository: ScenarioRepository,
    private val tagStrategy: NextEventTagStrategy = StarvationNextEventTagStrategy()
) {

    private val _state = MutableStateFlow<ScenarioState>(
        ScenarioState.Intro(
            welcomeMessage = "Bienvenue dans l'aventure. Choisissez votre voie initiale."
        )
    )
    val state: StateFlow<ScenarioState> = _state.asStateFlow()

    private var currentUniverse: UniverseType = UniverseType.FANTASTIC
    private var totalEvents: Int = 2
    private var lockedConclusion: ScenarioConclusion? = null
    private var selectedOrientation: ScenarioOrientation = ScenarioOrientation.BOUNTY_HUNT

    /**
     * Formule déterministe du nombre d'épreuves intermédiaires : nbEvents = 2 + floor(playerLevel / 2)
     */
    fun calculateEventCount(playerLevel: Int): Int {
        val safeLevel = if (playerLevel < 1) 1 else playerLevel
        return 2 + (safeLevel / 2)
    }

    /**
     * Phase 1 : Initialisation de l'accroche narrative et calcul du nombre d'étapes.
     */
    suspend fun startScenario(universe: UniverseType, playerLevel: Int): ScenarioState = withContext(Dispatchers.Default) {
        currentUniverse = universe
        totalEvents = calculateEventCount(playerLevel)
        lockedConclusion = null

        val introState = ScenarioState.Intro(
            welcomeMessage = generateWelcomeMessage(universe)
        )
        _state.value = introState
        introState
    }

    /**
     * Phase 1 -> Phase 2 : Sélection de l'orientation initiale et verrouillage de la conclusion.
     */
    suspend fun selectOrientation(orientation: ScenarioOrientation): ScenarioState = withContext(Dispatchers.IO) {
        selectedOrientation = orientation
        val conclusion = repository.getLockedConclusion(orientation)
        lockedConclusion = conclusion

        val firstTag = tagStrategy.determineNextTag(
            previousTag = null,
            currentStep = 1,
            totalSteps = totalEvents,
            orientation = orientation
        )

        val firstEvent = repository.getStandardEventByTag(currentUniverse, firstTag)

        val midGameState = ScenarioState.MidGameEvent(
            currentEventIndex = 1,
            totalEvents = totalEvents,
            currentTag = firstTag,
            event = firstEvent,
            lockedConclusion = conclusion
        )
        _state.value = midGameState
        midGameState
    }

    /**
     * Phase 2 : Résolution de l'épreuve intermédiaire et passage au tag/épreuve suivant (ou transition Climax).
     */
    suspend fun resolveCurrentEvent(isSuccess: Boolean): ScenarioState = withContext(Dispatchers.IO) {
        val currentState = _state.value
        if (currentState !is ScenarioState.MidGameEvent) return@withContext currentState

        if (currentState.currentEventIndex < currentState.totalEvents) {
            val nextIndex = currentState.currentEventIndex + 1
            val nextTag = tagStrategy.determineNextTag(
                previousTag = currentState.currentTag,
                currentStep = nextIndex,
                totalSteps = currentState.totalEvents,
                orientation = selectedOrientation
            )

            val nextEvent = repository.getStandardEventByTag(currentUniverse, nextTag)

            val updatedState = ScenarioState.MidGameEvent(
                currentEventIndex = nextIndex,
                totalEvents = currentState.totalEvents,
                currentTag = nextTag,
                event = nextEvent,
                lockedConclusion = currentState.lockedConclusion
            )
            _state.value = updatedState
            updatedState
        } else {
            // Phase 3 : Climax final
            val climaxEvent = repository.getClimaxEvent(currentUniverse, selectedOrientation)
            val climaxState = ScenarioState.ClimaxPhase(
                climaxEvent = climaxEvent,
                lockedConclusion = currentState.lockedConclusion
            )
            _state.value = climaxState
            climaxState
        }
    }

    /**
     * Phase 3 : Climax -> Dévoilement de la conclusion narrative verrouillée.
     */
    suspend fun resolveClimax(isSuccess: Boolean): ScenarioState = withContext(Dispatchers.Default) {
        val currentState = _state.value
        if (currentState !is ScenarioState.ClimaxPhase) return@withContext currentState

        val conclusionText = if (isSuccess) {
            currentState.lockedConclusion.victoryText
        } else {
            currentState.lockedConclusion.defeatText
        }

        val conclusionState = ScenarioState.ConclusionPhase(
            conclusionText = conclusionText,
            isVictory = isSuccess
        )
        _state.value = conclusionState
        conclusionState
    }

    /**
     * Transition vers l'état terminal (Finished).
     */
    fun finishGame(): ScenarioState {
        val currentState = _state.value
        val isVictory = when (currentState) {
            is ScenarioState.ConclusionPhase -> currentState.isVictory
            is ScenarioState.Finished -> currentState.isVictory
            else -> false
        }
        val finishedState = ScenarioState.Finished(isVictory = isVictory)
        _state.value = finishedState
        return finishedState
    }

    private fun generateWelcomeMessage(universe: UniverseType): String {
        return when (universe) {
            UniverseType.FANTASTIC -> "Une ombre ancienne plane sur la contrée. Quelle voie emprunterez-vous ?"
            UniverseType.CONTEMPORARY -> "Les ruelles de la métropole regorgent de secrets. Choisissez votre mission."
            UniverseType.FUTURISTIC -> "Secteur 9 : Les signaux d'urgence clignotent. Déterminez votre protocole."
        }
    }
}
