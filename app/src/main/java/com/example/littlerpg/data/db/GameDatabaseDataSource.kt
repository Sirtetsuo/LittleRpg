package com.example.littlerpg.data.db

import android.content.ContentValues
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.IntroTemplate
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.SavedGameData

class GameDatabaseDataSource(private val dbHelper: GameDbHelper) {

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
            val inventoryList = if (inventoryStr.isNotBlank()) inventoryStr.split(";") else listOf("Épée d'aventurier", "Potion de soin", "Clé ancienne")

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
                inventory = inventoryList
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
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GameDbHelper.TABLE_INTRO,
            null,
            "${GameDbHelper.COL_INTRO_UNIVERSE} = ?",
            arrayOf(universe.name),
            null,
            null,
            "RANDOM()",
            "1"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_ID))
                val article = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_ARTICLE))
                val place = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_PLACE))
                val contextText = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_CONTEXT))
                val uniStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_UNIVERSE))
                val uni = try { UniverseType.valueOf(uniStr) } catch (e: Exception) { universe }

                return IntroTemplate(
                    id = id,
                    article = article,
                    place = place,
                    contextText = contextText,
                    universe = uni
                )
            }
        }

        return IntroTemplate(
            article = "une",
            place = "taverne",
            contextText = "vous avez plein d'opportunités",
            universe = universe
        )
    }

    fun getAllIntroTemplates(universe: UniverseType? = null): List<IntroTemplate> {
        val db = dbHelper.readableDatabase
        val selection = if (universe != null) "${GameDbHelper.COL_INTRO_UNIVERSE} = ?" else null
        val selectionArgs = if (universe != null) arrayOf(universe.name) else null

        val cursor = db.query(
            GameDbHelper.TABLE_INTRO,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${GameDbHelper.COL_INTRO_ID} ASC"
        )

        val result = mutableListOf<IntroTemplate>()
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_ID))
                val article = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_ARTICLE))
                val place = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_PLACE))
                val contextText = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_CONTEXT))
                val uniStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_INTRO_UNIVERSE))
                val uni = try { UniverseType.valueOf(uniStr) } catch (e: Exception) { UniverseType.FANTASTIC }

                result.add(
                    IntroTemplate(
                        id = id,
                        article = article,
                        place = place,
                        contextText = contextText,
                        universe = uni
                    )
                )
            }
        }
        return result
    }

    fun getStandardEventByTag(universe: UniverseType, tag: EventTag): StandardEvent? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GameDbHelper.TABLE_STANDARD_EVENT,
            null,
            "${GameDbHelper.COL_EVENT_UNIVERSE} = ? AND ${GameDbHelper.COL_EVENT_TAG} = ?",
            arrayOf(universe.name, tag.name),
            null,
            null,
            "RANDOM()",
            "1"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                return parseStandardEventFromCursor(c)
            }
        }
        return getRandomStandardEvent(universe)
    }

    fun getRandomStandardEvent(universe: UniverseType): StandardEvent? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GameDbHelper.TABLE_STANDARD_EVENT,
            null,
            "${GameDbHelper.COL_EVENT_UNIVERSE} = ?",
            arrayOf(universe.name),
            null,
            null,
            "RANDOM()",
            "1"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                return parseStandardEventFromCursor(c)
            }
        }
        return null
    }

    private fun parseStandardEventFromCursor(c: android.database.Cursor): StandardEvent {
        val id = c.getLong(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ID)).toString()
        val title = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_TITLE))
        val desc = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_DESC))
        val tagStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_TAG))
        val tag = try { EventTag.valueOf(tagStr) } catch (e: Exception) { EventTag.COMBAT }
        val uniStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_UNIVERSE))
        val uni = try { UniverseType.valueOf(uniStr) } catch (e: Exception) { UniverseType.FANTASTIC }

        val act1Text = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ACT1_TEXT))
        val act1SkillStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ACT1_SKILL))
        val act1Skill = try { Skill.valueOf(act1SkillStr) } catch (e: Exception) { Skill.COMBAT }

        val act2Text = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ACT2_TEXT))
        val act2SkillStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ACT2_SKILL))
        val act2Skill = try { Skill.valueOf(act2SkillStr) } catch (e: Exception) { Skill.LOCK_TRAP }

        val act3Text = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ACT3_TEXT))
        val act3SkillStr = c.getString(c.getColumnIndexOrThrow(GameDbHelper.COL_EVENT_ACT3_SKILL))
        val act3Skill = try { Skill.valueOf(act3SkillStr) } catch (e: Exception) { Skill.LIFT_PUSH }

        val merchantIdx = c.getColumnIndex(GameDbHelper.COL_EVENT_IS_MERCHANT)
        val isMerchant = if (merchantIdx != -1) c.getInt(merchantIdx) == 1 else false

        val correctIdx = c.getColumnIndex(GameDbHelper.COL_EVENT_CORRECT_INDEX)
        val correctActionIndex = if (correctIdx != -1) c.getInt(correctIdx) else 1

        return StandardEvent(
            id = id,
            title = title,
            description = desc,
            tag = tag,
            universe = uni,
            action1Text = act1Text,
            action1Skill = act1Skill,
            action2Text = act2Text,
            action2Skill = act2Skill,
            action3Text = act3Text,
            action3Skill = act3Skill,
            isMerchant = isMerchant,
            correctActionIndex = correctActionIndex
        )
    }
}
