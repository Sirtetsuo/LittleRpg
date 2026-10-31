package com.example.littlerpg

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.littlerpg.model.AppScreen
import com.example.littlerpg.model.Character
import com.example.littlerpg.model.ChoiceOption
import com.example.littlerpg.model.Dice100Result
import com.example.littlerpg.model.GameMode
import com.example.littlerpg.model.GameTurn
import com.example.littlerpg.model.Item
import com.example.littlerpg.model.MasterTurn
import com.example.littlerpg.model.SaveState
import com.example.littlerpg.model.Skill
import com.example.littlerpg.model.SkillTier
import com.example.littlerpg.model.Universe
import com.example.littlerpg.service.GeminiRpgService
import com.example.littlerpg.util.SaveManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class RpgViewModel(
    private val rpgService: GeminiRpgService = GeminiRpgService()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.SOLO)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _selectedUniverse = MutableStateFlow<Universe?>(null)
    val selectedUniverse: StateFlow<Universe?> = _selectedUniverse.asStateFlow()

    private val _character = MutableStateFlow(Character())
    val character: StateFlow<Character> = _character.asStateFlow()

    private val _availableSkills = MutableStateFlow<List<Skill>>(emptyList())
    val availableSkills: StateFlow<List<Skill>> = _availableSkills.asStateFlow()

    private val _availableItems = MutableStateFlow<List<Item>>(emptyList())
    val availableItems: StateFlow<List<Item>> = _availableItems.asStateFlow()

    private val _storyHistory = MutableStateFlow<List<GameTurn>>(emptyList())
    val storyHistory: StateFlow<List<GameTurn>> = _storyHistory.asStateFlow()

    private val _masterHistory = MutableStateFlow<List<MasterTurn>>(emptyList())
    val masterHistory: StateFlow<List<MasterTurn>> = _masterHistory.asStateFlow()

    private val _currentTurn = MutableStateFlow<GameTurn?>(null)
    val currentTurn: StateFlow<GameTurn?> = _currentTurn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastDiceResult = MutableStateFlow<Dice100Result?>(null)
    val lastDiceResult: StateFlow<Dice100Result?> = _lastDiceResult.asStateFlow()

    private val _pendingJokerAction = MutableStateFlow<com.example.littlerpg.model.PendingJokerAction?>(null)
    val pendingJokerAction: StateFlow<com.example.littlerpg.model.PendingJokerAction?> = _pendingJokerAction.asStateFlow()

    private val _hasSave = MutableStateFlow(false)
    val hasSave: StateFlow<Boolean> = _hasSave.asStateFlow()

    fun checkSavedGame(context: Context) {
        _hasSave.value = SaveManager.hasSavedGame(context)
    }

    fun startNewGame(mode: GameMode = GameMode.SOLO) {
        _gameMode.value = mode
        _selectedUniverse.value = null
        _character.value = Character()
        _availableSkills.value = emptyList()
        _availableItems.value = emptyList()
        _storyHistory.value = emptyList()
        _masterHistory.value = emptyList()
        _currentTurn.value = null
        _lastDiceResult.value = null
        _currentScreen.value = AppScreen.UNIVERSE_SELECTION
    }

    fun selectUniverse(universe: Universe, context: Context? = null) {
        _selectedUniverse.value = universe
        if (_gameMode.value == GameMode.MASTER) {
            _currentScreen.value = AppScreen.GAME_PLAY_MASTER
            startMasterAdventure(universe, context)
        } else {
            _currentScreen.value = AppScreen.CHARACTER_CREATION
        }
    }

    private fun startMasterAdventure(universe: Universe, context: Context?) {
        _isLoading.value = true
        _masterHistory.value = emptyList()
        viewModelScope.launch {
            val initialPrompt = "Le Maître du Jeu (MJ) ouvre la séance. Présentez vos personnages et préparez-vous !"
            val turn = rpgService.generateMasterTurn(universe, emptyList(), initialPrompt)
            _masterHistory.value = listOf(turn)
            _isLoading.value = false

            if (context != null) {
                autoSaveGame(context)
            }
        }
    }

    fun submitDmAction(dmActionText: String, context: Context) {
        val universe = _selectedUniverse.value ?: Universe.FANTASTIQUE
        _isLoading.value = true

        viewModelScope.launch {
            val turn = rpgService.generateMasterTurn(universe, _masterHistory.value, dmActionText)
            _masterHistory.value = _masterHistory.value + turn
            _isLoading.value = false
            autoSaveGame(context)
        }
    }

    fun saveCharacterInfo(
        name: String,
        nickname: String,
        job: String,
        race: String,
        age: String,
        gender: String,
        particularity: String
    ) {
        _character.value = _character.value.copy(
            name = name,
            nickname = nickname,
            job = job,
            race = race,
            age = age,
            gender = gender,
            particularity = particularity
        )

        val universe = _selectedUniverse.value ?: Universe.FANTASTIQUE
        _isLoading.value = true
        viewModelScope.launch {
            val skills = rpgService.generateInitialSkills(universe, _character.value)
            _availableSkills.value = skills
            _isLoading.value = false
            _currentScreen.value = AppScreen.SKILL_SELECTION
        }
    }

    fun assignSkillTiers(maitre: Skill, expert: Skill, annexe: Skill) {
        val updatedSkills = _availableSkills.value.map { skill ->
            when (skill.name) {
                maitre.name -> skill.copy(percentage = 70, tier = SkillTier.MAITRE)
                expert.name -> skill.copy(percentage = 60, tier = SkillTier.EXPERT)
                annexe.name -> skill.copy(percentage = 50, tier = SkillTier.ANNEXE)
                else -> skill.copy(percentage = 40, tier = SkillTier.BASE)
            }
        }

        _character.value = _character.value.copy(skills = updatedSkills)

        val universe = _selectedUniverse.value ?: Universe.FANTASTIQUE
        _isLoading.value = true
        viewModelScope.launch {
            val items = rpgService.generateInitialItems(universe, _character.value, updatedSkills)
            _availableItems.value = items
            _isLoading.value = false
            _currentScreen.value = AppScreen.ITEM_SELECTION
        }
    }

    fun selectItemAndStartAdventure(item: Item, context: Context) {
        _character.value = _character.value.copy(inventory = listOf(item))

        val universe = _selectedUniverse.value ?: Universe.FANTASTIQUE
        _isLoading.value = true
        viewModelScope.launch {
            val initialAction = "Le personnage commence son aventure dans l'univers ${universe.displayName}."
            val turn = rpgService.generateStoryTurn(universe, _character.value, emptyList(), initialAction, null)
            _storyHistory.value = listOf(turn)
            _currentTurn.value = turn
            _isLoading.value = false
            _currentScreen.value = AppScreen.GAME_PLAY

            autoSaveGame(context)
        }
    }

    private fun findSkillByName(skillName: String): Skill? {
        val cleanInput = skillName.replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim().lowercase()
        return _character.value.skills.find { s ->
            val cleanSkill = s.name.replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim().lowercase()
            cleanSkill == cleanInput || cleanSkill.contains(cleanInput) || cleanInput.contains(cleanSkill)
        } ?: _character.value.skills.firstOrNull()
    }

    fun rollDice(skillName: String): Dice100Result {
        val matchedSkill = findSkillByName(skillName)
        val target = matchedSkill?.percentage ?: 40
        val actualSkillName = matchedSkill?.name ?: skillName
        val roll = Random.nextInt(1, 101)
        val isSuccess = roll <= target

        val summary = if (isSuccess) {
            "🎲 Jet d100: $roll <= $target% ($actualSkillName) ➔ 🎉 SUCCÈS !"
        } else {
            "🎲 Jet d100: $roll > $target% ($actualSkillName) ➔ 💥 ÉCHEC !"
        }

        val result = Dice100Result(
            rollValue = roll,
            skillName = actualSkillName,
            targetPercentage = target,
            isSuccess = isSuccess,
            summary = summary
        )
        _lastDiceResult.value = result
        return result
    }

    fun processActionIntent(actionText: String, diceResult: Dice100Result?, context: Context) {
        if ((diceResult != null && !diceResult.isSuccess) && _character.value.jokers > 0) {
            _pendingJokerAction.value = com.example.littlerpg.model.PendingJokerAction(actionText, diceResult)
        } else {
            submitPlayerAction(actionText, diceResult, context)
        }
    }

    fun submitPlayerAction(actionText: String, diceResult: Dice100Result?, context: Context) {
        val universe = _selectedUniverse.value ?: Universe.FANTASTIQUE
        _isLoading.value = true

        viewModelScope.launch {
            val turn = rpgService.generateStoryTurn(
                universe = universe,
                character = _character.value,
                history = _storyHistory.value,
                actionText = actionText,
                diceResult = diceResult
            )

            val updatedHistory = _storyHistory.value + turn
            _storyHistory.value = updatedHistory
            _currentTurn.value = turn

            if (turn.hpChanged != 0) {
                val newHp = (_character.value.hp + turn.hpChanged).coerceIn(0, _character.value.maxHp)
                _character.value = _character.value.copy(hp = newHp)
            }

            _isLoading.value = false

            if (!_character.value.isAlive) {
                _currentScreen.value = AppScreen.GAME_OVER
            } else {
                autoSaveGame(context)
            }
        }
    }

    fun selectChoice(choice: ChoiceOption, context: Context) {
        val suggestedSkill = choice.suggestedSkill ?: _character.value.skills.firstOrNull()?.name ?: "Compétence"
        val diceResult = rollDice(suggestedSkill)
        processActionIntent(choice.text, diceResult, context)
    }

    fun resolveJoker(useJoker: Boolean, context: Context) {
        val pending = _pendingJokerAction.value ?: return
        _pendingJokerAction.value = null

        if (useJoker) {
            val newJokers = _character.value.jokers - 1
            _character.value = _character.value.copy(jokers = newJokers)
            val jokerDice = pending.diceResult.copy(
                isSuccess = true,
                usedJoker = true,
                summary = "🃏 Joker utilisé ! Succès automatique appliqué !"
            )
            _lastDiceResult.value = jokerDice
            submitPlayerAction(pending.actionText, jokerDice, context)
        } else {
            submitPlayerAction(pending.actionText, pending.diceResult, context)
        }
    }

    fun loadGame(context: Context): Boolean {
        val save = SaveManager.loadGame(context) ?: return false
        _character.value = save.character
        _selectedUniverse.value = save.universe
        _storyHistory.value = save.storyHistory
        _masterHistory.value = save.masterHistory
        _gameMode.value = save.gameMode
        _currentTurn.value = save.currentTurn ?: save.storyHistory.lastOrNull()
        
        if (save.gameMode == GameMode.MASTER) {
            _currentScreen.value = AppScreen.GAME_PLAY_MASTER
        } else {
            _currentScreen.value = AppScreen.GAME_PLAY
        }
        return true
    }

    fun saveGame(context: Context): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            autoSaveGame(context)
        }
        return true
    }

    private fun autoSaveGame(context: Context) {
        val universe = _selectedUniverse.value ?: return
        val saveState = SaveState(
            character = _character.value,
            universe = universe,
            storyHistory = _storyHistory.value,
            currentTurn = _currentTurn.value,
            gameMode = _gameMode.value,
            masterHistory = _masterHistory.value
        )
        SaveManager.saveGame(context, saveState)
        _hasSave.value = true
    }
}
