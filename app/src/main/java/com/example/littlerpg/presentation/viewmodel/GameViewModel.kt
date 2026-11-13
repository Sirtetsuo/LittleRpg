package com.example.littlerpg.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littlerpg.data.repository.ScenarioRepositoryImpl
import com.example.littlerpg.domain.engine.GameItemHandler
import com.example.littlerpg.domain.engine.GameVictoryHandler
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.NextEventTagStrategy
import com.example.littlerpg.domain.engine.StarvationNextEventTagStrategy
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.Skill
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

    private val itemHandler = GameItemHandler()
    private val victoryHandler = GameVictoryHandler(scenarioRepository)
    private val riddleHandler = GameRiddleHandler(narrativeEngine, scenarioRepository, tagStrategy, victoryHandler)
    private val actionHandler = GameActionHandler(resolveActionUseCase, narrativeEngine, scenarioRepository, tagStrategy, victoryHandler)
    private val turnHandler = GameTurnHandler(narrativeEngine, scenarioRepository, tagStrategy, victoryHandler, riddleHandler, actionHandler)

    init {
        checkSavedGame()
    }

    fun checkSavedGame() {
        viewModelScope.launch {
            _uiState.update { it.copy(hasSavedGame = hasSavedGameUseCase()) }
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
        _uiState.update { it.copy(selectedUniverse = universe, currentStep = ScreenStep.CharacterCreation(universe)) }
    }

    fun onCreateCharacter(name: String, nickname: String, age: Int, str: Int, intVal: Int, agi: Int) {
        val universe = _uiState.value.selectedUniverse
        val defaultInventory = when (universe) {
            UniverseType.FANTASTIC -> listOf("Épée rouillée")
            UniverseType.CONTEMPORARY -> listOf("Pistolet")
            UniverseType.FUTURISTIC -> listOf("Blaster")
        }
        val character = Character(
            name = name.ifBlank { "Héros" },
            nickname = nickname,
            age = age,
            strength = str,
            intelligence = intVal,
            agility = agi,
            hp = 20,
            maxHp = 20,
            inventory = defaultInventory
        )

        viewModelScope.launch {
            val introTemplate = scenarioRepository.getRandomIntroTemplate(universe)
            val introText = narrativeEngine.generateIntro(universe, character, introTemplate)
            val initialMessages = listOf(ChatMessage(isFromGameMaster = true, text = introText))
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

    fun onOpenCharacterSheet() = _uiState.update { it.copy(showCharacterSheetDialog = true) }
    fun onCloseCharacterSheet() = _uiState.update { it.copy(showCharacterSheetDialog = false) }
    fun onOpenItemSelectionDialog() = _uiState.update { it.copy(showItemSelectionDialog = true) }
    fun onCloseItemSelectionDialog() = _uiState.update { it.copy(showItemSelectionDialog = false) }

    fun onUseItem(itemName: String) {
        val state = _uiState.value
        val result = itemHandler.useItem(itemName, state.character)
        val playerMsg = ChatMessage(isFromGameMaster = false, text = "🎒 Utilisation d'objet : $itemName")
        val gmMsg = ChatMessage(isFromGameMaster = true, text = result.messageText)
        val updatedMessages = state.messages + playerMsg + gmMsg

        _uiState.update { it.copy(character = result.updatedCharacter, messages = updatedMessages, showItemSelectionDialog = false) }

        viewModelScope.launch {
            saveGameUseCase(result.updatedCharacter, state.selectedUniverse, updatedMessages)
        }

        if (result.isAutoSuccess && result.autoSuccessAttribute != null && state.turnCount > 0 && state.currentEvent != null) {
            val autoSkill = Skill.getByAttribute(result.autoSuccessAttribute).firstOrNull() ?: Skill.COMBAT
            val autoOption = ActionOption(id = 4, label = "Utiliser un objet : $itemName", skill = autoSkill, isItemAction = true, itemName = itemName)
            _uiState.update { it.copy(selectedOption = autoOption) }
            onExecuteActionClicked()
        }
    }

    fun onSaveGameClicked() {
        val state = _uiState.value
        viewModelScope.launch {
            saveGameUseCase(state.character, state.selectedUniverse, state.messages)
            _uiState.update { it.copy(hasSavedGame = true, saveNotificationText = "Partie sauvegardée dans la base SQLite !") }
            delay(2500)
            _uiState.update { it.copy(saveNotificationText = null) }
        }
    }

    fun onExecuteActionClicked() {
        val state = _uiState.value
        val option = state.selectedOption ?: return

        viewModelScope.launch {
            val newState = turnHandler.processTurnExecution(state, option)
            _uiState.value = newState
            saveGameUseCase(newState.character, newState.selectedUniverse, newState.messages)
            _uiState.update { it.copy(hasSavedGame = true) }
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
