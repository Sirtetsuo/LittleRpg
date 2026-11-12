package com.example.littlerpg.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.presentation.state.GameUiState
import com.example.littlerpg.presentation.state.ScreenStep

@Composable
fun AppNavigation(
    uiState: GameUiState,
    onNewGameClicked: () -> Unit,
    onContinueClicked: () -> Unit,
    onUniverseSelected: (UniverseType) -> Unit,
    onCharacterCreated: (name: String, nickname: String, age: Int, str: Int, intVal: Int, agi: Int) -> Unit,
    onOptionSelect: (ActionOption) -> Unit,
    onExecuteActionClicked: () -> Unit,
    onSaveGameClicked: () -> Unit,
    onOpenCharacterSheet: () -> Unit,
    onCloseCharacterSheet: () -> Unit,
    onOpenItemSelectionDialog: () -> Unit = {},
    onCloseItemSelectionDialog: () -> Unit = {},
    onUseItem: (String) -> Unit = {},
    onRestartAfterGameOver: () -> Unit,
    onBackToMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val marginX = maxWidth * 0.08f
        val marginY = maxHeight * 0.08f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = marginX,
                    end = marginX,
                    top = marginY,
                    bottom = marginY
                )
        ) {
            when (val step = uiState.currentStep) {
                is ScreenStep.Menu -> {
                    MenuScreen(
                        hasSavedGame = uiState.hasSavedGame,
                        onNewGameClicked = onNewGameClicked,
                        onContinueClicked = onContinueClicked
                    )
                }
                is ScreenStep.UniverseSelection -> {
                    UniverseScreen(
                        onUniverseSelected = onUniverseSelected,
                        onBackClicked = onBackToMenuClicked
                    )
                }
                is ScreenStep.CharacterCreation -> {
                    CharacterCreationScreen(
                        selectedUniverse = step.universe,
                        onCharacterCreated = onCharacterCreated,
                        onBackClicked = onNewGameClicked
                    )
                }
                is ScreenStep.MainGame -> {
                    MainGameScreen(
                        universe = step.universe,
                        character = uiState.character,
                        messages = uiState.messages,
                        currentOptions = uiState.currentOptions,
                        selectedOption = uiState.selectedOption,
                        saveNotificationText = uiState.saveNotificationText,
                        showCharacterSheetDialog = uiState.showCharacterSheetDialog,
                        showItemSelectionDialog = uiState.showItemSelectionDialog,
                        onOptionSelect = onOptionSelect,
                        onExecuteActionClicked = onExecuteActionClicked,
                        onSaveGameClicked = onSaveGameClicked,
                        onOpenCharacterSheet = onOpenCharacterSheet,
                        onCloseCharacterSheet = onCloseCharacterSheet,
                        onOpenItemSelectionDialog = onOpenItemSelectionDialog,
                        onCloseItemSelectionDialog = onCloseItemSelectionDialog,
                        onUseItem = onUseItem,
                        onBackToMenuClicked = onBackToMenuClicked
                    )
                }
                is ScreenStep.GameOver -> {
                    GameOverScreen(
                        onRestartNewGame = onRestartAfterGameOver
                    )
                }
            }
        }
    }
}
