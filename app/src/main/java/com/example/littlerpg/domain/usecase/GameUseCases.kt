package com.example.littlerpg.domain.usecase

import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.RuleEngine
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.ActionResolution
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.GameRepository
import com.example.littlerpg.domain.repository.SavedGameData

class ResolveActionUseCase(
    private val ruleEngine: RuleEngine,
    private val narrativeEngine: NarrativeEngine
) {
    operator fun invoke(
        universe: UniverseType,
        character: Character,
        option: ActionOption,
        turnCount: Int = 0
    ): Quadruple {
        val (resolution, updatedCharacter) = ruleEngine.resolve(skill = option.skill, character = character)
        val narrativeText = narrativeEngine.generateNarration(universe, updatedCharacter, option, resolution, turnCount)
        val gmMessage = ChatMessage(isFromGameMaster = true, text = narrativeText)
        val newOptions = narrativeEngine.generateActionOptions(universe, turnCount)

        return Quadruple(resolution, updatedCharacter, gmMessage, newOptions)
    }
}

data class Quadruple(
    val resolution: ActionResolution,
    val updatedCharacter: Character,
    val gmMessage: ChatMessage,
    val nextOptions: List<ActionOption>
)

class SaveGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(character: Character, universe: UniverseType, messages: List<ChatMessage>) {
        repository.saveGame(character, universe, messages)
    }
}

class LoadGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(): SavedGameData? {
        return repository.loadGame()
    }
}

class HasSavedGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.hasSavedGame()
    }
}

class ClearSavedGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke() {
        repository.clearSavedGame()
    }
}
