package com.example.littlerpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.littlerpg.data.db.GameDatabaseDataSource
import com.example.littlerpg.data.db.GameDbHelper
import com.example.littlerpg.data.repository.GameRepositoryImpl
import com.example.littlerpg.data.repository.ScenarioRepositoryImpl
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.RuleEngine
import com.example.littlerpg.domain.usecase.ClearSavedGameUseCase
import com.example.littlerpg.domain.usecase.HasSavedGameUseCase
import com.example.littlerpg.domain.usecase.LoadGameUseCase
import com.example.littlerpg.domain.usecase.ResolveActionUseCase
import com.example.littlerpg.domain.usecase.SaveGameUseCase
import com.example.littlerpg.presentation.ui.AppNavigation
import com.example.littlerpg.presentation.viewmodel.GameViewModel
import com.example.littlerpg.ui.theme.LittleRpgTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbHelper = GameDbHelper(applicationContext)
        val dataSource = GameDatabaseDataSource(dbHelper)
        val repository = GameRepositoryImpl(dataSource)
        val scenarioRepository = ScenarioRepositoryImpl(dataSource)

        val ruleEngine = RuleEngine()
        val narrativeEngine = NarrativeEngine()

        val resolveActionUseCase = ResolveActionUseCase(ruleEngine, narrativeEngine)
        val saveGameUseCase = SaveGameUseCase(repository)
        val loadGameUseCase = LoadGameUseCase(repository)
        val hasSavedGameUseCase = HasSavedGameUseCase(repository)
        val clearSavedGameUseCase = ClearSavedGameUseCase(repository)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(
                    resolveActionUseCase = resolveActionUseCase,
                    narrativeEngine = narrativeEngine,
                    saveGameUseCase = saveGameUseCase,
                    loadGameUseCase = loadGameUseCase,
                    hasSavedGameUseCase = hasSavedGameUseCase,
                    clearSavedGameUseCase = clearSavedGameUseCase,
                    scenarioRepository = scenarioRepository
                ) as T
            }
        }

        viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        setContent {
            LittleRpgTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    AppNavigation(
                        uiState = uiState,
                        onNewGameClicked = viewModel::onNewGameClicked,
                        onContinueClicked = viewModel::onContinueGameClicked,
                        onUniverseSelected = viewModel::onSelectUniverse,
                        onCharacterCreated = viewModel::onCreateCharacter,
                        onOptionSelect = viewModel::onOptionSelect,
                        onExecuteActionClicked = viewModel::onExecuteActionClicked,
                        onSaveGameClicked = viewModel::onSaveGameClicked,
                        onOpenCharacterSheet = viewModel::onOpenCharacterSheet,
                        onCloseCharacterSheet = viewModel::onCloseCharacterSheet,
                        onOpenItemSelectionDialog = viewModel::onOpenItemSelectionDialog,
                        onCloseItemSelectionDialog = viewModel::onCloseItemSelectionDialog,
                        onUseItem = viewModel::onUseItem,
                        onRestartAfterGameOver = viewModel::onRestartAfterGameOver,
                        onBackToMenuClicked = viewModel::onBackToMenu
                    )
                }
            }
        }
    }
}
