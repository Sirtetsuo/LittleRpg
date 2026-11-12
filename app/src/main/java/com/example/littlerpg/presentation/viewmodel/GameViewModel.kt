package com.example.littlerpg.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littlerpg.data.repository.ScenarioRepositoryImpl
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.NextEventTagStrategy
import com.example.littlerpg.domain.engine.StarvationNextEventTagStrategy
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.GameItem
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.ScenarioRepository
import com.example.littlerpg.domain.usecase.ClearSavedGameUseCase
import com.example.littlerpg.domain.usecase.HasSavedGameUseCase
import com.example.littlerpg.domain.usecase.LoadGameUseCase
import com.example.littlerpg.domain.usecase.ResolveActionUseCase
import com.example.littlerpg.domain.usecase.SaveGameUseCase
import com.example.littlerpg.presentation.state.GameUiState
import com.example.littlerpg.presentation.state.ScreenStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val resolveActionUseCase: ResolveActionUseCase,
    private val narrativeEngine: NarrativeEngine,
    private val saveGameUseCase: SaveGameUseCase,
    private val loadGameUseCase: LoadGameUseCase,
    private val hasSavedGameUseCase: HasSavedGameUseCase,
    private val clearSavedGameUseCase: ClearSavedGameUseCase,
    private val scenarioRepository: ScenarioRepository = ScenarioRepositoryImpl(),
    private val tagStrategy: NextEventTagStrategy = StarvationNextEventTagStrategy()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        checkSavedGame()
    }

    fun checkSavedGame() {
        viewModelScope.launch {
            val hasSave = hasSavedGameUseCase()
            _uiState.update { it.copy(hasSavedGame = hasSave) }
        }
    }

    fun onNewGameClicked() {
        _uiState.update { it.copy(currentStep = ScreenStep.UniverseSelection) }
    }

    fun onContinueGameClicked() {
        viewModelScope.launch {
            val savedData = loadGameUseCase()
            if (savedData != null) {
                val initialOptions = narrativeEngine.generateActionOptions(savedData.universe, 0, savedData.character)
                _uiState.update {
                    it.copy(
                        currentStep = ScreenStep.MainGame(savedData.universe, savedData.character),
                        selectedUniverse = savedData.universe,
                        character = savedData.character,
                        messages = savedData.messages,
                        currentOptions = initialOptions,
                        selectedOption = initialOptions.firstOrNull()
                    )
                }
            } else {
                _uiState.update { it.copy(hasSavedGame = false) }
            }
        }
    }

    fun onSelectUniverse(universe: UniverseType) {
        _uiState.update {
            it.copy(
                selectedUniverse = universe,
                currentStep = ScreenStep.CharacterCreation(universe)
            )
        }
    }

    fun onCreateCharacter(
        name: String,
        nickname: String,
        age: Int,
        str: Int,
        intVal: Int,
        agi: Int
    ) {
        val character = Character(
            name = name.ifBlank { "Héros" },
            nickname = nickname,
            age = age,
            strength = str,
            intelligence = intVal,
            agility = agi,
            hp = 20,
            maxHp = 20
        )
        val universe = _uiState.value.selectedUniverse

        viewModelScope.launch {
            val introTemplate = scenarioRepository.getRandomIntroTemplate(universe)
            val introMessageText = narrativeEngine.generateIntro(universe, character, introTemplate)
            val introMessage = ChatMessage(isFromGameMaster = true, text = introMessageText)
            val initialMessages = listOf(introMessage)
            val initialOptions = narrativeEngine.generateActionOptions(universe, 0, character)

            _uiState.update {
                it.copy(
                    character = character,
                    messages = initialMessages,
                    currentOptions = initialOptions,
                    selectedOption = initialOptions.firstOrNull(),
                    turnCount = 0,
                    currentStep = ScreenStep.MainGame(universe, character)
                )
            }

            saveGameUseCase(character, universe, initialMessages)
            _uiState.update { it.copy(hasSavedGame = true) }
        }
    }

    fun onOptionSelect(option: ActionOption) {
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun onOpenCharacterSheet() {
        _uiState.update { it.copy(showCharacterSheetDialog = true) }
    }

    fun onCloseCharacterSheet() {
        _uiState.update { it.copy(showCharacterSheetDialog = false) }
    }

    fun onOpenItemSelectionDialog() {
        _uiState.update { it.copy(showItemSelectionDialog = true) }
    }

    fun onCloseItemSelectionDialog() {
        _uiState.update { it.copy(showItemSelectionDialog = false) }
    }

    fun onUseItem(itemName: String) {
        val state = _uiState.value
        val character = state.character
        val item = GameItem.fromName(itemName)

        val (updatedCharacter, effectMsgText) = when {
            item.name.contains("potion", ignoreCase = true) -> {
                val roll = kotlin.random.Random.nextInt(1, 7)
                val newHp = (character.hp + roll).coerceAtMost(character.maxHp)
                val healed = newHp - character.hp
                val newInventory = character.inventory.toMutableList().apply { remove(itemName) }
                val charUpdated = character.copy(hp = newHp, inventory = newInventory)
                Pair(
                    charUpdated,
                    "🧪 POTION UTILISÉE ! (Jet 1d6: +$roll PV)\n" +
                            "Résultat : +$healed PV restaurés ! (${charUpdated.hp}/${charUpdated.maxHp} PV actuels). " +
                            "La potion est consommée."
                )
            }
            item.name.contains("gourde", ignoreCase = true) -> {
                val newHp = (character.hp + 1).coerceAtMost(character.maxHp)
                val healed = newHp - character.hp
                val newInventory = character.inventory.toMutableList().apply { remove(itemName) }
                val charUpdated = character.copy(hp = newHp, inventory = newInventory)
                Pair(
                    charUpdated,
                    "💧 GOURDE UTILISÉE !\n" +
                            "Résultat : +$healed PV restauré ! (${charUpdated.hp}/${charUpdated.maxHp} PV actuels). " +
                            "La gourde est consommée."
                )
            }
            else -> {
                Pair(
                    character,
                    "🛡️ ÉQUIPEMENT : ${item.name} (${item.effectDescription}). " +
                            "L'objet est actif."
                )
            }
        }

        val playerMsg = ChatMessage(isFromGameMaster = false, text = "🎒 Utilisation d'objet : $itemName")
        val gmMsg = ChatMessage(isFromGameMaster = true, text = effectMsgText)
        val updatedMessages = state.messages + playerMsg + gmMsg

        _uiState.update {
            it.copy(
                character = updatedCharacter,
                messages = updatedMessages,
                showItemSelectionDialog = false
            )
        }

        viewModelScope.launch {
            saveGameUseCase(updatedCharacter, state.selectedUniverse, updatedMessages)
        }
    }

    fun onSaveGameClicked() {
        val state = _uiState.value
        viewModelScope.launch {
            saveGameUseCase(state.character, state.selectedUniverse, state.messages)
            _uiState.update {
                it.copy(
                    hasSavedGame = true,
                    saveNotificationText = "Partie sauvegardée dans la base SQLite !"
                )
            }
            delay(2500)
            _uiState.update { it.copy(saveNotificationText = null) }
        }
    }

    fun onExecuteActionClicked() {
        val state = _uiState.value
        val option = state.selectedOption ?: return

        viewModelScope.launch {
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

                val playerMsg = ChatMessage(isFromGameMaster = false, text = "Action choisie : ${option.label}")
                val gmText = "🎯 VOIE SCELLÉE : ${orientation.displayName} (${orientation.actionText})\n\n" +
                        "📍 ÉPREUVE 1/$totalEvents — $challengeTitle :\n$challengeDesc"
                val gmMsg = ChatMessage(isFromGameMaster = true, text = gmText)

                val turn1Options = firstEvent.toActionOptions(state.character.inventory.firstOrNull())
                val updatedMessages = state.messages + playerMsg + gmMsg

                _uiState.update {
                    it.copy(
                        selectedOrientation = orientation,
                        currentTag = firstEvent.tag,
                        currentEvent = firstEvent,
                        messages = updatedMessages,
                        currentOptions = turn1Options,
                        selectedOption = turn1Options.firstOrNull(),
                        turnCount = 1
                    )
                }

                saveGameUseCase(state.character, state.selectedUniverse, updatedMessages)
                _uiState.update { it.copy(hasSavedGame = true) }
                return@launch
            }

            val playerMsg = ChatMessage(isFromGameMaster = false, text = "Action choisie : ${option.label}")
            val targetTurn = state.turnCount + 1

            if (option.label.startsWith("Acheter :")) {
                val boughtItemName = option.label.substringAfter("Acheter : ").trim()
                val updatedInventory = state.character.inventory + boughtItemName
                val updatedChar = state.character.copy(inventory = updatedInventory)

                val nextOptionsForTurn: List<ActionOption>
                var drawnTag: EventTag? = state.currentTag
                var nextEvt: StandardEvent? = null

                val gmMsgText = if (targetTurn <= totalEvents) {
                    val nextTag = tagStrategy.determineNextTag(
                        previousTag = state.currentTag,
                        currentStep = targetTurn,
                        totalSteps = totalEvents,
                        orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                    )
                    nextEvt = scenarioRepository.getStandardEventByTag(state.selectedUniverse, nextTag)
                    drawnTag = nextEvt.tag
                    nextOptionsForTurn = nextEvt.toActionOptions(updatedChar.inventory.firstOrNull())
                    "🛍️ OBJET ACQUIS !\n" +
                            "Vous obtenez l'objet '$boughtItemName' qui est immédiatement ajouté à votre inventaire !\n\n" +
                            "📍 ÉPREUVE $targetTurn/$totalEvents — ${nextEvt.title} :\n${nextEvt.description}"
                } else if (targetTurn == totalEvents + 1) {
                    val orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                    val climax = scenarioRepository.getClimaxEvent(state.selectedUniverse, orientation)
                    nextOptionsForTurn = narrativeEngine.generateActionOptions(state.selectedUniverse, targetTurn, updatedChar)
                    "🛍️ OBJET ACQUIS !\n" +
                            "Vous obtenez '$boughtItemName' dans votre inventaire !\n\n" +
                            "🔥 CLIMAX FINAL — ${climax.title} !\n${climax.description}\nAdversaire ultime : ${climax.finalBossName}"
                } else {
                    val orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                    val conclusion = scenarioRepository.getLockedConclusion(orientation)
                    nextOptionsForTurn = emptyList()
                    "🛍️ OBJET ACQUIS !\n\n🏆 VICTOIRE !\n\n${conclusion.victoryText}"
                }

                val finalGmMsg = ChatMessage(isFromGameMaster = true, text = gmMsgText)
                val updatedMessages = state.messages + playerMsg + finalGmMsg

                _uiState.update {
                    it.copy(
                        character = updatedChar,
                        currentTag = drawnTag,
                        currentEvent = nextEvt,
                        messages = updatedMessages,
                        currentOptions = nextOptionsForTurn,
                        selectedOption = nextOptionsForTurn.firstOrNull(),
                        turnCount = targetTurn
                    )
                }

                saveGameUseCase(updatedChar, state.selectedUniverse, updatedMessages)
                _uiState.update { it.copy(hasSavedGame = true) }
                return@launch
            }

            if (state.currentEvent?.tag == EventTag.RIDDLE && !option.isItemAction) {
                val currentEvt = state.currentEvent ?: return@launch
                val isCorrect = (option.id == currentEvt.correctActionIndex)

                if (isCorrect) {
                    var nextOptionsForTurn = state.currentOptions
                    var drawnTag: EventTag? = state.currentTag
                    var nextEvt: StandardEvent? = null

                    val gmMsgText = if (targetTurn <= totalEvents) {
                        val nextTag = tagStrategy.determineNextTag(
                            previousTag = state.currentTag,
                            currentStep = targetTurn,
                            totalSteps = totalEvents,
                            orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                        )
                        nextEvt = scenarioRepository.getStandardEventByTag(state.selectedUniverse, nextTag)
                        drawnTag = nextEvt.tag
                        nextOptionsForTurn = nextEvt.toActionOptions(state.character.inventory.firstOrNull())
                        "🧩 ÉNIGME RÉSOLUE !\n" +
                                "'${option.label}' est la BONNE RÉPONSE ! Le mécanisme se déverrouille.\n\n" +
                                "📍 ÉPREUVE $targetTurn/$totalEvents — ${nextEvt.title} :\n${nextEvt.description}"
                    } else if (targetTurn == totalEvents + 1) {
                        val orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                        val climax = scenarioRepository.getClimaxEvent(state.selectedUniverse, orientation)
                        nextOptionsForTurn = narrativeEngine.generateActionOptions(state.selectedUniverse, targetTurn, state.character)
                        "🧩 ÉNIGME RÉSOLUE !\n" +
                                "'${option.label}' est la BONNE RÉPONSE ! Le mécanisme se déverrouille.\n\n" +
                                "🔥 CLIMAX FINAL — ${climax.title} !\n${climax.description}\nAdversaire ultime : ${climax.finalBossName}"
                    } else {
                        val orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                        val conclusion = scenarioRepository.getLockedConclusion(orientation)
                        nextOptionsForTurn = emptyList()
                        "🧩 ÉNIGME RÉSOLUE !\n\n🏆 VICTOIRE !\n\n${conclusion.victoryText}"
                    }

                    val finalGmMsg = ChatMessage(isFromGameMaster = true, text = gmMsgText)
                    val updatedMessages = state.messages + playerMsg + finalGmMsg

                    _uiState.update {
                        it.copy(
                            currentTag = drawnTag,
                            currentEvent = nextEvt,
                            messages = updatedMessages,
                            currentOptions = nextOptionsForTurn,
                            selectedOption = nextOptionsForTurn.firstOrNull(),
                            turnCount = targetTurn
                        )
                    }

                    saveGameUseCase(state.character, state.selectedUniverse, updatedMessages)
                    _uiState.update { it.copy(hasSavedGame = true) }
                    return@launch
                } else {
                    val newHp = (state.character.hp - 1).coerceAtLeast(0)
                    val updatedChar = state.character.copy(hp = newHp)
                    val isDead = newHp <= 0

                    val gmMsgText = "❌ MAUVAISE RÉPONSE !\n" +
                            "'${option.label}' est incorrect. Le piège de l'énigme se déclenche et vous inflige -1 PV " +
                            "(${updatedChar.hp}/${updatedChar.maxHp} PV restants).\n\n" +
                            "⚠️ Retentez votre chance avec une autre réponse !"

                    val finalGmMsg = ChatMessage(isFromGameMaster = true, text = gmMsgText)
                    val updatedMessages = state.messages + playerMsg + finalGmMsg

                    _uiState.update {
                        val nextStep = if (isDead) ScreenStep.GameOver else it.currentStep
                        it.copy(
                            character = updatedChar,
                            messages = updatedMessages,
                            currentOptions = if (isDead) emptyList() else state.currentOptions,
                            selectedOption = if (isDead) null else option,
                            currentStep = nextStep
                        )
                    }

                    saveGameUseCase(updatedChar, state.selectedUniverse, updatedMessages)
                    _uiState.update { it.copy(hasSavedGame = true) }
                    return@launch
                }
            }

            val quadruple = resolveActionUseCase(
                universe = state.selectedUniverse,
                character = state.character,
                option = option,
                currentOptions = state.currentOptions,
                turnCount = targetTurn
            )

            val isDead = quadruple.updatedCharacter.hp <= 0
            val isSuccess = quadruple.resolution.isSuccess
            val newTurnCount = if (isSuccess) targetTurn else state.turnCount

            var nextOptionsForTurn = state.currentOptions
            var drawnTag: EventTag? = state.currentTag

            val gmMsgText = if (!isSuccess) {
                quadruple.gmMessage.text
            } else if (newTurnCount <= totalEvents) {
                val nextTag = tagStrategy.determineNextTag(
                    previousTag = state.currentTag,
                    currentStep = newTurnCount,
                    totalSteps = totalEvents,
                    orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                )
                val nextEvent = scenarioRepository.getStandardEventByTag(state.selectedUniverse, nextTag)
                drawnTag = nextEvent.tag
                nextOptionsForTurn = nextEvent.toActionOptions(quadruple.updatedCharacter.inventory.firstOrNull())
                val skillName = quadruple.resolution.skill.displayName
                val statName = quadruple.resolution.skill.attribute.displayName
                "🎲 RÉUSSITE ! [Action : ${option.label}] ($skillName - $statName)\n" +
                        "(Jet: ${quadruple.resolution.diceRoll} <= Cible: ${quadruple.resolution.targetValue})\n\n" +
                        "📍 ÉPREUVE $newTurnCount/$totalEvents — ${nextEvent.title} :\n${nextEvent.description}"
            } else if (newTurnCount == totalEvents + 1) {
                val orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                val climax = scenarioRepository.getClimaxEvent(state.selectedUniverse, orientation)
                nextOptionsForTurn = narrativeEngine.generateActionOptions(state.selectedUniverse, newTurnCount, quadruple.updatedCharacter)
                val skillName = quadruple.resolution.skill.displayName
                val statName = quadruple.resolution.skill.attribute.displayName
                "🎲 RÉUSSITE ! [Action : ${option.label}] ($skillName - $statName)\n" +
                        "(Jet: ${quadruple.resolution.diceRoll} <= Cible: ${quadruple.resolution.targetValue})\n\n" +
                        "🔥 CLIMAX FINAL — ${climax.title} !\n" +
                        "${climax.description}\n" +
                        "Adversaire ultime : ${climax.finalBossName}"
            } else {
                val orientation = state.selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
                val conclusion = scenarioRepository.getLockedConclusion(orientation)
                nextOptionsForTurn = emptyList()
                "🎲 RÉUSSITE SUPRÊME !\n\n" +
                        "🏆 VICTOIRE !\n\n" +
                        conclusion.victoryText
            }

            val finalGmMsg = ChatMessage(isFromGameMaster = true, text = gmMsgText)
            val updatedMessages = state.messages + playerMsg + finalGmMsg

            val nextOptions = if (isDead || newTurnCount > totalEvents + 1) {
                emptyList()
            } else if (isSuccess) {
                nextOptionsForTurn
            } else {
                state.currentOptions
            }

            _uiState.update {
                val nextStep = if (isDead) {
                    ScreenStep.GameOver
                } else if (it.currentStep is ScreenStep.MainGame) {
                    ScreenStep.MainGame(state.selectedUniverse, quadruple.updatedCharacter)
                } else {
                    it.currentStep
                }
                it.copy(
                    character = quadruple.updatedCharacter,
                    currentTag = drawnTag,
                    messages = updatedMessages,
                    currentOptions = nextOptions,
                    selectedOption = nextOptions.firstOrNull(),
                    turnCount = newTurnCount,
                    lastResolution = quadruple.resolution,
                    currentStep = nextStep
                )
            }

            saveGameUseCase(quadruple.updatedCharacter, state.selectedUniverse, updatedMessages)
            _uiState.update { it.copy(hasSavedGame = true) }
        }
    }

    fun onUpdateCharacterAttributes(strDelta: Int = 0, intDelta: Int = 0, agiDelta: Int = 0, hpDelta: Int = 0) {
        val state = _uiState.value
        val current = state.character
        val newHp = (current.hp + hpDelta).coerceIn(0, current.maxHp)
        val updatedChar = current.copy(
            strength = (current.strength + strDelta).coerceAtLeast(1),
            intelligence = (current.intelligence + intDelta).coerceAtLeast(1),
            agility = (current.agility + agiDelta).coerceAtLeast(1),
            hp = newHp
        )

        _uiState.update {
            val nextStep = if (newHp <= 0) {
                ScreenStep.GameOver
            } else if (it.currentStep is ScreenStep.MainGame) {
                ScreenStep.MainGame(state.selectedUniverse, updatedChar)
            } else {
                it.currentStep
            }
            it.copy(
                character = updatedChar,
                currentStep = nextStep
            )
        }

        viewModelScope.launch {
            saveGameUseCase(updatedChar, state.selectedUniverse, state.messages)
        }
    }

    fun onRestartAfterGameOver() {
        viewModelScope.launch {
            clearSavedGameUseCase()
            _uiState.update {
                it.copy(
                    currentStep = ScreenStep.Menu,
                    hasSavedGame = false,
                    messages = emptyList(),
                    currentOptions = emptyList(),
                    selectedOption = null
                )
            }
        }
    }

    fun onBackToMenu() {
        _uiState.update { it.copy(currentStep = ScreenStep.Menu) }
        checkSavedGame()
    }
}
