package com.example.littlerpg.domain.repository

import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.UniverseType

data class SavedGameData(
    val character: Character,
    val universe: UniverseType,
    val messages: List<ChatMessage>
)

interface GameRepository {
    suspend fun saveGame(character: Character, universe: UniverseType, messages: List<ChatMessage>)
    suspend fun loadGame(): SavedGameData?
    suspend fun hasSavedGame(): Boolean
    suspend fun clearSavedGame()
}
