package com.example.littlerpg.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GameDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createSessionTable = """
            CREATE TABLE $TABLE_SESSION (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CHAR_NAME TEXT NOT NULL,
                $COL_CHAR_NICKNAME TEXT NOT NULL,
                $COL_CHAR_AGE INTEGER NOT NULL,
                $COL_CHAR_STR INTEGER NOT NULL,
                $COL_CHAR_INT INTEGER NOT NULL,
                $COL_CHAR_AGI INTEGER NOT NULL,
                $COL_CHAR_HP INTEGER NOT NULL,
                $COL_CHAR_MAX_HP INTEGER NOT NULL,
                $COL_CHAR_LEVEL INTEGER NOT NULL DEFAULT 1,
                $COL_CHAR_INVENTORY TEXT NOT NULL DEFAULT '',
                $COL_CHAR_GOLD INTEGER NOT NULL DEFAULT 100,
                $COL_UNIVERSE TEXT NOT NULL,
                $COL_UPDATED_AT INTEGER NOT NULL
            );
        """.trimIndent()

        val createChatTable = """
            CREATE TABLE $TABLE_CHAT (
                $COL_CHAT_ID TEXT PRIMARY KEY,
                $COL_SESSION_ID INTEGER NOT NULL,
                $COL_IS_GM INTEGER NOT NULL,
                $COL_TEXT TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                FOREIGN KEY($COL_SESSION_ID) REFERENCES $TABLE_SESSION($COL_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        val createIntroTable = """
            CREATE TABLE $TABLE_INTRO (
                $COL_INTRO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INTRO_ARTICLE TEXT NOT NULL,
                $COL_INTRO_PLACE TEXT NOT NULL,
                $COL_INTRO_CONTEXT TEXT NOT NULL,
                $COL_INTRO_UNIVERSE TEXT NOT NULL
            );
        """.trimIndent()

        val createEventTable = """
            CREATE TABLE $TABLE_STANDARD_EVENT (
                $COL_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EVENT_TITLE TEXT NOT NULL,
                $COL_EVENT_DESC TEXT NOT NULL,
                $COL_EVENT_TAG TEXT NOT NULL,
                $COL_EVENT_UNIVERSE TEXT NOT NULL,
                $COL_EVENT_ACT1_TEXT TEXT NOT NULL,
                $COL_EVENT_ACT1_SKILL TEXT NOT NULL,
                $COL_EVENT_ACT2_TEXT TEXT NOT NULL,
                $COL_EVENT_ACT2_SKILL TEXT NOT NULL,
                $COL_EVENT_ACT3_TEXT TEXT NOT NULL,
                $COL_EVENT_ACT3_SKILL TEXT NOT NULL,
                $COL_EVENT_IS_MERCHANT INTEGER NOT NULL DEFAULT 0,
                $COL_EVENT_CORRECT_INDEX INTEGER NOT NULL DEFAULT 1
            );
        """.trimIndent()

        db.execSQL(createSessionTable)
        db.execSQL(createChatTable)
        db.execSQL(createIntroTable)
        db.execSQL(createEventTable)

        insertDefaultIntroTemplates(db)
        insertDefaultStandardEvents(db)
    }

    private fun insertDefaultIntroTemplates(db: SQLiteDatabase) {
        val templates = DefaultIntroData.getIntroTemplates()
        for ((data, universe) in templates) {
            val values = ContentValues().apply {
                put(COL_INTRO_ARTICLE, data.first)
                put(COL_INTRO_PLACE, data.second)
                put(COL_INTRO_CONTEXT, data.third)
                put(COL_INTRO_UNIVERSE, universe.name)
            }
            db.insert(TABLE_INTRO, null, values)
        }
    }

    private fun insertDefaultStandardEvents(db: SQLiteDatabase) {
        val events = DefaultEventsData.getEvents()
        for (e in events) {
            val values = ContentValues().apply {
                put(COL_EVENT_TITLE, e.title)
                put(COL_EVENT_DESC, e.description)
                put(COL_EVENT_TAG, e.tag.name)
                put(COL_EVENT_UNIVERSE, e.universe.name)
                put(COL_EVENT_ACT1_TEXT, e.action1Text)
                put(COL_EVENT_ACT1_SKILL, e.action1Skill.name)
                put(COL_EVENT_ACT2_TEXT, e.action2Text)
                put(COL_EVENT_ACT2_SKILL, e.action2Skill.name)
                put(COL_EVENT_ACT3_TEXT, e.action3Text)
                put(COL_EVENT_ACT3_SKILL, e.action3Skill.name)
                put(COL_EVENT_IS_MERCHANT, if (e.isMerchant) 1 else 0)
                put(COL_EVENT_CORRECT_INDEX, e.correctActionIndex)
            }
            db.insert(TABLE_STANDARD_EVENT, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STANDARD_EVENT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INTRO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "littlerpg.db"
        const val DATABASE_VERSION = 11

        const val TABLE_SESSION = "game_session"
        const val COL_ID = "_id"
        const val COL_CHAR_NAME = "char_name"
        const val COL_CHAR_NICKNAME = "char_nickname"
        const val COL_CHAR_AGE = "char_age"
        const val COL_CHAR_STR = "char_strength"
        const val COL_CHAR_INT = "char_intelligence"
        const val COL_CHAR_AGI = "char_agility"
        const val COL_CHAR_HP = "char_hp"
        const val COL_CHAR_MAX_HP = "char_max_hp"
        const val COL_CHAR_LEVEL = "char_level"
        val COL_CHAR_INVENTORY = "char_inventory"
        const val COL_CHAR_GOLD = "char_gold"
        const val COL_UNIVERSE = "universe_type"
        const val COL_UPDATED_AT = "updated_at"

        const val TABLE_CHAT = "chat_history"
        const val COL_CHAT_ID = "chat_id"
        const val COL_SESSION_ID = "session_id"
        const val COL_IS_GM = "is_from_gm"
        const val COL_TEXT = "message_text"
        const val COL_TIMESTAMP = "timestamp"

        const val TABLE_INTRO = "intro_template"
        const val COL_INTRO_ID = "_id"
        const val COL_INTRO_ARTICLE = "article"
        const val COL_INTRO_PLACE = "place"
        const val COL_INTRO_CONTEXT = "context_text"
        const val COL_INTRO_UNIVERSE = "universe_type"

        const val TABLE_STANDARD_EVENT = "standard_event"
        const val COL_EVENT_ID = "_id"
        const val COL_EVENT_TITLE = "title"
        const val COL_EVENT_DESC = "description"
        const val COL_EVENT_TAG = "tag"
        const val COL_EVENT_UNIVERSE = "universe_type"
        const val COL_EVENT_ACT1_TEXT = "action1_text"
        const val COL_EVENT_ACT1_SKILL = "action1_skill"
        const val COL_EVENT_ACT2_TEXT = "action2_text"
        const val COL_EVENT_ACT2_SKILL = "action2_skill"
        const val COL_EVENT_ACT3_TEXT = "action3_text"
        const val COL_EVENT_ACT3_SKILL = "action3_skill"
        const val COL_EVENT_IS_MERCHANT = "is_merchant"
        const val COL_EVENT_CORRECT_INDEX = "correct_index"
    }
}
