package com.example.littlerpg.data.db

import android.content.ContentValues
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.IntroTemplate
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.SavedGameData

class GameDatabaseDataSource(private val dbHelper: GameDbHelper) {

    private val eventDataSource = GameEventDataSource(dbHelper)

    fun saveGameSession(character: Character, universe: UniverseType, messages: List<ChatMessage>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(GameDbHelper.TABLE_CHAT, null, null)
            db.delete(GameDbHelper.TABLE_SESSION, null, null)

            val sessionValues = ContentValues().apply {
                put(GameDbHelper.COL_CHAR_NAME, character.name)
                put(GameDbHelper.COL_CHAR_NICKNAME, character.nickname)
                put(GameDbHelper.COL_CHAR_AGE, character.age)
                put(GameDbHelper.COL_CHAR_STR, character.strength)
                put(GameDbHelper.COL_CHAR_INT, character.intelligence)
                put(GameDbHelper.COL_CHAR_AGI, character.agility)
                put(GameDbHelper.COL_CHAR_HP, character.hp)
                put(GameDbHelper.COL_CHAR_MAX_HP, character.maxHp)
                put(GameDbHelper.COL_CHAR_LEVEL, character.level)
                put(GameDbHelper.COL_CHAR_INVENTORY, character.inventory.joinToString(";"))
                put(GameDbHelper.COL_CHAR_GOLD, character.gold)
                put(GameDbHelper.COL_UNIVERSE, universe.name)
                put(GameDbHelper.COL_UPDATED_AT, System.currentTimeMillis())
            }

            val sessionId = db.insert(GameDbHelper.TABLE_SESSION, null, sessionValues)

            for (msg in messages) {
                val chatValues = ContentValues().apply {
                    put(GameDbHelper.COL_CHAT_ID, msg.id)
                    put(GameDbHelper.COL_SESSION_ID, sessionId)
                    put(GameDbHelper.COL_IS_GM, if (msg.isFromGameMaster) 1 else 0)
                    put(GameDbHelper.COL_TEXT, msg.text)
                    put(GameDbHelper.COL_TIMESTAMP, msg.timestamp)
                }
                db.insert(GameDbHelper.TABLE_CHAT, null, chatValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun loadActiveGameSession(): SavedGameData? {
        val db = dbHelper.readableDatabase
        val cursorSession = db.query(
            GameDbHelper.TABLE_SESSION,
            null,
            null,
            null,
            null,
            null,
            "${GameDbHelper.COL_UPDATED_AT} DESC",
            "1"
        )

        cursorSession.use { cursor ->
            if (!cursor.moveToFirst()) return null

            val sessionId = cursor.getLong(cursor.getColumnIndexOrThrow(GameDbHelper.COL_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_NAME))
            val nickname = cursor.getString(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_NICKNAME))
            val age = cursor.getInt(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_AGE))
            val str = cursor.getInt(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_STR))
            val intVal = cursor.getInt(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_INT))
            val agi = cursor.getInt(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_AGI))
            val hp = cursor.getInt(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_HP))
            val maxHp = cursor.getInt(cursor.getColumnIndexOrThrow(GameDbHelper.COL_CHAR_MAX_HP))
            val levelIdx = cursor.getColumnIndex(GameDbHelper.COL_CHAR_LEVEL)
            val level = if (levelIdx != -1) cursor.getInt(levelIdx) else 1
            val inventoryIdx = cursor.getColumnIndex(GameDbHelper.COL_CHAR_INVENTORY)
            val inventoryStr = if (inventoryIdx != -1) cursor.getString(inventoryIdx) ?: "" else ""
            val inventoryList = if (inventoryStr.isNotBlank()) inventoryStr.split(";") else listOf("Épée rouillée")

            val goldIdx = cursor.getColumnIndex(GameDbHelper.COL_CHAR_GOLD)
            val gold = if (goldIdx != -1) cursor.getInt(goldIdx) else 100

            val universeStr = cursor.getString(cursor.getColumnIndexOrThrow(GameDbHelper.COL_UNIVERSE))
            val universe = try {
                UniverseType.valueOf(universeStr)
            } catch (e: Exception) {
                UniverseType.FANTASTIC
            }

            val character = Character(
                name = name,
                nickname = nickname,
                age = age,
                strength = str,
                intelligence = intVal,
                agility = agi,
                hp = hp,
                maxHp = maxHp,
                level = level,
                inventory = inventoryList,
                gold = gold
            )

            val messages = mutableListOf<ChatMessage>()
            val cursorChat = db.query(
                GameDbHelper.TABLE_CHAT,
                null,
                "${GameDbHelper.COL_SESSION_ID} = ?",
                arrayOf(sessionId.toString()),
                null,
                null,
                "${GameDbHelper.COL_TIMESTAMP} ASC"
            )

            cursorChat.use { cChat ->
                while (cChat.moveToNext()) {
                    val id = cChat.getString(cChat.getColumnIndexOrThrow(GameDbHelper.COL_CHAT_ID))
                    val isGm = cChat.getInt(cChat.getColumnIndexOrThrow(GameDbHelper.COL_IS_GM)) == 1
                    val text = cChat.getString(cChat.getColumnIndexOrThrow(GameDbHelper.COL_TEXT))
                    val timestamp = cChat.getLong(cChat.getColumnIndexOrThrow(GameDbHelper.COL_TIMESTAMP))

                    messages.add(
                        ChatMessage(
                            id = id,
                            isFromGameMaster = isGm,
                            text = text,
                            timestamp = timestamp
                        )
                    )
                }
            }

            return SavedGameData(
                character = character,
                universe = universe,
                messages = messages
            )
        }
    }

    fun hasSavedSession(): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${GameDbHelper.TABLE_SESSION}", null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) > 0
            }
        }
        return false
    }

    fun clearAllData() {
        val db = dbHelper.writableDatabase
        db.delete(GameDbHelper.TABLE_CHAT, null, null)
        db.delete(GameDbHelper.TABLE_SESSION, null, null)
    }

    fun getRandomIntroTemplate(universe: UniverseType): IntroTemplate {
        return eventDataSource.getRandomIntroTemplate(universe)
    }

    fun getAllIntroTemplates(universe: UniverseType? = null): List<IntroTemplate> {
        return eventDataSource.getAllIntroTemplates(universe)
    }

    fun getStandardEventByTag(universe: UniverseType, tag: EventTag): StandardEvent? {
        return eventDataSource.getStandardEventByTag(universe, tag)
    }

    fun getRandomStandardEvent(universe: UniverseType): StandardEvent? {
        return eventDataSource.getRandomStandardEvent(universe)
    }
}
