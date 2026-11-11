package com.example.littlerpg.presentation.state

import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.ActionResolution
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.UniverseType

sealed interface ScreenStep {
    object Menu : ScreenStep
    object UniverseSelection : ScreenStep
    data class CharacterCreation(val universe: UniverseType) : ScreenStep
    data class MainGame(val universe: UniverseType, val character: Character) : ScreenStep
    object GameOver : ScreenStep
}

data class GameUiState(
    val currentStep: ScreenStep = ScreenStep.Menu,
    val hasSavedGame: Boolean = false,
    val selectedUniverse: UniverseType = UniverseType.FANTASTIC,
    val character: Character = Character("", "", 25, 10, 10, 10, hp = 20, maxHp = 20, level = 1),
    val messages: List<ChatMessage> = emptyList(),
    val currentOptions: List<ActionOption> = emptyList(),
    val selectedOption: ActionOption? = null,
    val turnCount: Int = 0,
    val isRolling: Boolean = false,
    val lastResolution: ActionResolution? = null,
    val saveNotificationText: String? = null,
    val showCharacterSheetDialog: Boolean = false
)
