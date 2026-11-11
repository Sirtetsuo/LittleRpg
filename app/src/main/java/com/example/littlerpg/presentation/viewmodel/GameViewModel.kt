package com.example.littlerpg.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.UniverseType
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
    private val clearSavedGameUseCase: ClearSavedGameUseCase
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
                val initialOptions = narrativeEngine.generateActionOptions(savedData.universe, 0)
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
        val introMessageText = narrativeEngine.generateIntro(universe, character)
        val introMessage = ChatMessage(isFromGameMaster = true, text = introMessageText)
        val initialMessages = listOf(introMessage)
        val initialOptions = narrativeEngine.generateActionOptions(universe, 0)

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

        viewModelScope.launch {
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

        val playerMsg = ChatMessage(isFromGameMaster = false, text = "Action choisie : ${option.label}")
        val nextTurn = state.turnCount + 1

        val quadruple = resolveActionUseCase(
            universe = state.selectedUniverse,
            character = state.character,
            option = option,
            turnCount = nextTurn
        )

        val updatedMessages = state.messages + playerMsg + quadruple.gmMessage
        val isDead = quadruple.updatedCharacter.hp <= 0

        _uiState.update {
            it.copy(
                character = quadruple.updatedCharacter,
                messages = updatedMessages,
                currentOptions = quadruple.nextOptions,
                selectedOption = quadruple.nextOptions.firstOrNull(),
                turnCount = nextTurn,
                lastResolution = quadruple.resolution,
                currentStep = if (isDead) ScreenStep.GameOver else it.currentStep
            )
        }

        viewModelScope.launch {
            saveGameUseCase(quadruple.updatedCharacter, state.selectedUniverse, updatedMessages)
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
