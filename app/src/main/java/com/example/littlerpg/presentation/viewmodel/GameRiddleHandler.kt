package com.example.littlerpg.presentation.viewmodel

import com.example.littlerpg.domain.engine.GameVictoryHandler
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.NextEventTagStrategy
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.ScenarioRepository

sealed interface RiddleResult {
    data class Success(
        val updatedCharacter: Character,
        val drawnTag: EventTag?,
        val nextEvent: StandardEvent?,
        val gmMsgText: String,
        val nextOptions: List<ActionOption>
    ) : RiddleResult
    data class Failure(
        val updatedCharacter: Character,
        val isDead: Boolean,
        val gmMsgText: String
    ) : RiddleResult
}

class GameRiddleHandler(
    private val narrativeEngine: NarrativeEngine,
    private val scenarioRepository: ScenarioRepository,
    private val tagStrategy: NextEventTagStrategy,
    private val victoryHandler: GameVictoryHandler
) {

    suspend fun handleRiddleAction(
        option: ActionOption,
        currentEvt: StandardEvent,
        character: Character,
        universe: UniverseType,
        targetTurn: Int,
        totalEvents: Int,
        currentTurnCount: Int,
        currentTag: EventTag?,
        currentOptions: List<ActionOption>,
        selectedOrientation: ScenarioOrientation?
    ): RiddleResult {
        val isCorrect = (option.id == currentEvt.correctActionIndex)

        if (isCorrect) {
            var nextOptionsForTurn = currentOptions
            var drawnTag: EventTag? = currentTag
            var nextEvt: StandardEvent? = null
            var updatedChar = character

            val gmMsgText = if (targetTurn <= totalEvents) {
                val nextTag = tagStrategy.determineNextTag(
                    previousTag = currentTag,
                    currentStep = targetTurn,
                    totalSteps = totalEvents,
                    orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                )
                nextEvt = scenarioRepository.getStandardEventByTag(universe, nextTag)
                drawnTag = nextEvt.tag
                nextOptionsForTurn = nextEvt.toActionOptions(character.inventory.firstOrNull())
                "🧩 ÉNIGME RÉSOLUE !\n" +
                        "'${option.label}' est la BONNE RÉPONSE ! Le mécanisme se déverrouille.\n\n" +
                        "📍 ÉPREUVE $targetTurn/$totalEvents — ${nextEvt.title} :\n${nextEvt.description}"
            } else if (targetTurn == totalEvents + 1) {
                val orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                val climax = scenarioRepository.getClimaxEvent(universe, orientation)
                nextOptionsForTurn = narrativeEngine.generateActionOptions(universe, targetTurn, character)
                "🧩 ÉNIGME RÉSOLUE !\n" +
                        "'${option.label}' est la BONNE RÉPONSE ! Le mécanisme se déverrouille.\n\n" +
                        "🔥 CLIMAX FINAL — ${climax.title} !\n${climax.description}\nAdversaire ultime : ${climax.finalBossName}"
            } else {
                val orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                val victoryRes = victoryHandler.createVictoryState(character, orientation)
                updatedChar = victoryRes.victoriousCharacter
                nextOptionsForTurn = victoryRes.rewardOptions
                "🧩 ÉNIGME RÉSOLUE !\n\n" + victoryRes.gmMessageText
            }

            return RiddleResult.Success(
                updatedCharacter = updatedChar,
                drawnTag = drawnTag,
                nextEvent = nextEvt,
                gmMsgText = gmMsgText,
                nextOptions = nextOptionsForTurn
            )
        } else {
            val isClimaxRiddle = (currentTurnCount == totalEvents + 1)
            val hpLoss = if (isClimaxRiddle) 3 else 1
            val newHp = (character.hp - hpLoss).coerceAtLeast(0)
            val updatedChar = character.copy(hp = newHp)
            val isDead = newHp <= 0

            val gmMsgText = "❌ MAUVAISE RÉPONSE !\n" +
                    "'${option.label}' est incorrect. Le piège de l'énigme se déclenche et vous inflige -$hpLoss PV " +
                    "(${updatedChar.hp}/${updatedChar.getEffectiveMaxHp()} PV restants).\n\n" +
                    "⚠️ Retentez votre chance avec une autre réponse !"

            return RiddleResult.Failure(
                updatedCharacter = updatedChar,
                isDead = isDead,
                gmMsgText = gmMsgText
            )
        }
    }
}
