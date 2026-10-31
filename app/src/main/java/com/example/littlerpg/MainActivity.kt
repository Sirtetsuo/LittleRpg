package com.example.littlerpg

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.littlerpg.model.AppScreen
import com.example.littlerpg.model.GameMode
import com.example.littlerpg.ui.CharacterCreationScreen
import com.example.littlerpg.ui.GameOverScreen
import com.example.littlerpg.ui.GamePlayScreen
import com.example.littlerpg.ui.GamePlayScreenMaster
import com.example.littlerpg.ui.HomeScreen
import com.example.littlerpg.ui.ItemSelectionStep
import com.example.littlerpg.ui.SkillSelectionStep
import com.example.littlerpg.ui.UniverseSelectionScreen
import com.example.littlerpg.ui.theme.LittleRpgTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LittleRpgTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LittleRpgApp()
                }
            }
        }
    }
}

@Composable
fun LittleRpgApp(
    viewModel: RpgViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedUniverse by viewModel.selectedUniverse.collectAsState()
    val hasSave by viewModel.hasSave.collectAsState()
    val character by viewModel.character.collectAsState()
    val availableSkills by viewModel.availableSkills.collectAsState()
    val availableItems by viewModel.availableItems.collectAsState()
    val currentTurn by viewModel.currentTurn.collectAsState()
    val storyHistory by viewModel.storyHistory.collectAsState()
    val masterHistory by viewModel.masterHistory.collectAsState()
    val lastDiceResult by viewModel.lastDiceResult.collectAsState()
    val pendingJokerAction by viewModel.pendingJokerAction.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkSavedGame(context)
    }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            AppScreen.HOME -> {
                HomeScreen(
                    hasSave = hasSave,
                    onContinue = {
                        val success = viewModel.loadGame(context)
                        if (!success) {
                            Toast.makeText(context, "Impossible de charger la sauvegarde", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onNewSoloGame = {
                        viewModel.startNewGame(GameMode.SOLO)
                    },
                    onNewMasterGame = {
                        viewModel.startNewGame(GameMode.MASTER)
                    }
                )
            }

            AppScreen.UNIVERSE_SELECTION -> {
                UniverseSelectionScreen(
                    onSelectUniverse = { universe ->
                        viewModel.selectUniverse(universe, context)
                    }
                )
            }

            AppScreen.CHARACTER_CREATION -> {
                CharacterCreationScreen(
                    onSaveCharacter = { name, nickname, job, race, age, gender, particularity ->
                        viewModel.saveCharacterInfo(
                            name = name,
                            nickname = nickname,
                            job = job,
                            race = race,
                            age = age,
                            gender = gender,
                            particularity = particularity
                        )
                    }
                )
            }

            AppScreen.SKILL_SELECTION -> {
                SkillSelectionStep(
                    skills = availableSkills,
                    isLoading = isLoading,
                    universe = selectedUniverse,
                    onConfirmSkills = { maitre, expert, annexe ->
                        viewModel.assignSkillTiers(maitre, expert, annexe)
                    }
                )
            }

            AppScreen.ITEM_SELECTION -> {
                ItemSelectionStep(
                    items = availableItems,
                    isLoading = isLoading,
                    universe = selectedUniverse,
                    onSelectItem = { item ->
                        viewModel.selectItemAndStartAdventure(item, context)
                    }
                )
            }

            AppScreen.GAME_PLAY -> {
                GamePlayScreen(
                    character = character,
                    storyHistory = storyHistory,
                    currentTurn = currentTurn,
                    lastDiceResult = lastDiceResult,
                    pendingJokerAction = pendingJokerAction,
                    isLoading = isLoading,
                    universe = selectedUniverse,
                    onSelectChoice = { choice ->
                        viewModel.selectChoice(choice, context)
                    },
                    onSubmitCustomAction = { actionText, diceResult ->
                        viewModel.processActionIntent(actionText, diceResult, context)
                    },
                    onRollDice = { skillName ->
                        viewModel.rollDice(skillName)
                    },
                    onResolveJoker = { useJoker ->
                        viewModel.resolveJoker(useJoker, context)
                    },
                    onSaveGame = {
                        viewModel.saveGame(context)
                        Toast.makeText(context, "Partie sauvegardée 💾", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            AppScreen.GAME_PLAY_MASTER -> {
                GamePlayScreenMaster(
                    universe = selectedUniverse,
                    masterHistory = masterHistory,
                    isLoading = isLoading,
                    onSubmitDmAction = { dmActionText ->
                        viewModel.submitDmAction(dmActionText, context)
                    },
                    onSaveGame = {
                        viewModel.saveGame(context)
                    }
                )
            }

            AppScreen.GAME_OVER -> {
                GameOverScreen(
                    onLoadGame = {
                        val success = viewModel.loadGame(context)
                        if (!success) {
                            Toast.makeText(context, "Aucune sauvegarde valide trouvée", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onNewGame = {
                        viewModel.startNewGame(GameMode.SOLO)
                    }
                )
            }
        }
    }
}
