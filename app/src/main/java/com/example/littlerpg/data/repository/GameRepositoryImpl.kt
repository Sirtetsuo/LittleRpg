package com.example.littlerpg.data.repository

import com.example.littlerpg.data.db.GameDatabaseDataSource
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.GameRepository
import com.example.littlerpg.domain.repository.SavedGameData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepositoryImpl(
    private val dataSource: GameDatabaseDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GameRepository {

    override suspend fun saveGame(
        character: Character,
        universe: UniverseType,
        messages: List<ChatMessage>
    ) = withContext(ioDispatcher) {
        dataSource.saveGameSession(character, universe, messages)
    }

    override suspend fun loadGame(): SavedGameData? = withContext(ioDispatcher) {
        dataSource.loadActiveGameSession()
    }

    override suspend fun hasSavedGame(): Boolean = withContext(ioDispatcher) {
        dataSource.hasSavedSession()
    }

    override suspend fun clearSavedGame() = withContext(ioDispatcher) {
        dataSource.clearAllData()
    }
}
