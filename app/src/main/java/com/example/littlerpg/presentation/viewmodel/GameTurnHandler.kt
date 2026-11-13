package com.example.littlerpg.presentation.viewmodel

import com.example.littlerpg.domain.engine.GameVictoryHandler
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.NextEventTagStrategy
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.repository.ScenarioRepository
import com.example.littlerpg.presentation.state.GameUiState
import com.example.littlerpg.presentation.state.ScreenStep

class GameTurnHandler(
    private val narrativeEngine: NarrativeEngine,
    private val scenarioRepository: ScenarioRepository,
    private val tagStrategy: NextEventTagStrategy,
    private val victoryHandler: GameVictoryHandler,
    private val riddleHandler: GameRiddleHandler,
    private val actionHandler: GameActionHandler
) {

    suspend fun processTurnExecution(state: GameUiState, option: ActionOption): GameUiState {
        val playerMsg = ChatMessage(isFromGameMaster = false, text = "Action choisie : ${option.label}")

        if (option.id in 101..104) {
            val upgradeRes = victoryHandler.applyAttributeUpgrade(option.id, state.character)
            val gmMsg = ChatMessage(isFromGameMaster = true, text = upgradeRes.second)
            val updatedMessages = state.messages + playerMsg + gmMsg

            val continueOption = ActionOption(
                id = 99,
                label = "🔄 Continuer l'aventure (Niveau ${upgradeRes.first.level})",
                skill = Skill.PERSUASION
            )
            val nextOptions = listOf(continueOption)

            return state.copy(
                character = upgradeRes.first,
                messages = updatedMessages,
                currentOptions = nextOptions,
                selectedOption = nextOptions.firstOrNull()
            )
        }

        if (option.id == 99 || option.label.contains("Continuer", ignoreCase = true)) {
            val nextLevelOptions = narrativeEngine.generateActionOptions(state.selectedUniverse, 0, state.character)
            val gmMsgText = "🌅 NOUVELLE QUÊTE (Niveau ${state.character.level}) !\n" +
                    "Vous reprenez la route avec vos ${state.character.gold} PO, votre inventaire intact et ${state.character.hp}/${state.character.getEffectiveMaxHp()} PV actuels.\n" +
                    "Choisissez votre nouvelle voie !"
            val gmMsg = ChatMessage(isFromGameMaster = true, text = gmMsgText)
            val updatedMessages = state.messages + playerMsg + gmMsg

            return state.copy(
                turnCount = 0,
                currentTag = null,
                currentEvent = null,
                messages = updatedMessages,
                currentOptions = nextLevelOptions,
                selectedOption = nextLevelOptions.firstOrNull(),
                currentStep = ScreenStep.MainGame(state.selectedUniverse, state.character)
            )
        }

        val totalEvents = 2 + (state.character.level / 2)

        if (state.turnCount == 0) {
            val orientation = option.orientation ?: when (option.id) {
                1 -> ScenarioOrientation.BOUNTY_HUNT
                2 -> ScenarioOrientation.TREASURE_HUNT
                3 -> ScenarioOrientation.MYSTERY
                else -> ScenarioOrientation.NPC_ENCOUNTER
            }

            val firstTag = tagStrategy.determineNextTag(
                previousTag = null,
                currentStep = 1,
                totalSteps = totalEvents,
                orientation = orientation
            )
            val firstEvent = scenarioRepository.getStandardEventByTag(state.selectedUniverse, firstTag)
            val challengeTitle = firstEvent.title
            val challengeDesc = firstEvent.description

            val gmText = "🎯 VOIE SCELLÉE : ${orientation.displayName} (${orientation.actionText})\n\n" +
                    "📍 ÉPREUVE 1/$totalEvents — $challengeTitle :\n$challengeDesc"
            val gmMsg = ChatMessage(isFromGameMaster = true, text = gmText)

            val turn1Options = firstEvent.toActionOptions(state.character.inventory.firstOrNull())
            val updatedMessages = state.messages + playerMsg + gmMsg

            return state.copy(
                selectedOrientation = orientation,
                currentTag = firstEvent.tag,
                currentEvent = firstEvent,
                messages = updatedMessages,
                currentOptions = turn1Options,
                selectedOption = turn1Options.firstOrNull(),
                turnCount = 1
            )
        }

        val targetTurn = state.turnCount + 1

        if (option.label.startsWith("Acheter :")) {
            val boughtItemName = option.label.substringAfter("Acheter : ").trim()
            val mResult = actionHandler.handleMerchantPurchase(
                boughtItemName = boughtItemName,
                character = state.character,
                universe = state.selectedUniverse,
                targetTurn = targetTurn,
                totalEvents = totalEvents,
                currentTag = state.currentTag,
                selectedOrientation = state.selectedOrientation
            )

            return when (mResult) {
                is MerchantResult.InsufficientGold -> {
                    val finalGmMsg = ChatMessage(isFromGameMaster = true, text = mResult.gmMsgText)
                    state.copy(messages = state.messages + playerMsg + finalGmMsg)
                }
                is MerchantResult.Success -> {
                    val finalGmMsg = ChatMessage(isFromGameMaster = true, text = mResult.gmMsgText)
                    state.copy(
                        character = mResult.updatedCharacter,
                        currentTag = mResult.drawnTag,
                        currentEvent = mResult.nextEvent,
                        messages = state.messages + playerMsg + finalGmMsg,
                        currentOptions = mResult.nextOptions,
                        selectedOption = mResult.nextOptions.firstOrNull(),
                        turnCount = targetTurn
                    )
                }
            }
        }

        if (state.currentEvent?.tag == EventTag.RIDDLE && !option.isItemAction) {
            val currentEvt = state.currentEvent
            if (currentEvt != null) {
                val riddleResult = riddleHandler.handleRiddleAction(
                    option = option,
                    currentEvt = currentEvt,
                    character = state.character,
                    universe = state.selectedUniverse,
                    targetTurn = targetTurn,
                    totalEvents = totalEvents,
                    currentTurnCount = state.turnCount,
                    currentTag = state.currentTag,
                    currentOptions = state.currentOptions,
                    selectedOrientation = state.selectedOrientation
                )

                return when (riddleResult) {
                    is RiddleResult.Success -> {
                        val finalGmMsg = ChatMessage(isFromGameMaster = true, text = riddleResult.gmMsgText)
                        state.copy(
                            character = riddleResult.updatedCharacter,
                            currentTag = riddleResult.drawnTag,
                            currentEvent = riddleResult.nextEvent,
                            messages = state.messages + playerMsg + finalGmMsg,
                            currentOptions = riddleResult.nextOptions,
                            selectedOption = riddleResult.nextOptions.firstOrNull(),
                            turnCount = targetTurn
                        )
                    }
                    is RiddleResult.Failure -> {
                        val finalGmMsg = ChatMessage(isFromGameMaster = true, text = riddleResult.gmMsgText)
                        val nextStep = if (riddleResult.isDead) ScreenStep.GameOver else state.currentStep
                        state.copy(
                            character = riddleResult.updatedCharacter,
                            messages = state.messages + playerMsg + finalGmMsg,
                            currentOptions = if (riddleResult.isDead) emptyList() else state.currentOptions,
                            selectedOption = if (riddleResult.isDead) null else option,
                            currentStep = nextStep
                        )
                    }
                }
            }
        }

        val stdResult = actionHandler.handleStandardAction(
            option = option,
            character = state.character,
            universe = state.selectedUniverse,
            targetTurn = targetTurn,
            totalEvents = totalEvents,
            currentTurnCount = state.turnCount,
            currentTag = state.currentTag,
            currentOptions = state.currentOptions,
            selectedOrientation = state.selectedOrientation
        )

        val finalGmMsg = ChatMessage(isFromGameMaster = true, text = stdResult.gmMsgText)
        val nextStep = if (stdResult.isDead) {
            ScreenStep.GameOver
        } else if (state.currentStep is ScreenStep.MainGame) {
            ScreenStep.MainGame(state.selectedUniverse, stdResult.updatedCharacter)
        } else {
            state.currentStep
        }

        return state.copy(
            character = stdResult.updatedCharacter,
            currentTag = stdResult.drawnTag,
            messages = state.messages + playerMsg + finalGmMsg,
            currentOptions = stdResult.nextOptions,
            selectedOption = stdResult.nextOptions.firstOrNull(),
            turnCount = if (stdResult.resolution.isSuccess) targetTurn else state.turnCount,
            lastResolution = stdResult.resolution,
            currentStep = nextStep
        )
    }
}
