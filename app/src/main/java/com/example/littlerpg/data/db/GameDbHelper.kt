package com.example.littlerpg.data.db

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

        db.execSQL(createSessionTable)
        db.execSQL(createChatTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "littlerpg.db"
        const val DATABASE_VERSION = 2

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
        const val COL_CHAR_INVENTORY = "char_inventory"
        const val COL_UNIVERSE = "universe_type"
        const val COL_UPDATED_AT = "updated_at"

        const val TABLE_CHAT = "chat_history"
        const val COL_CHAT_ID = "chat_id"
        const val COL_SESSION_ID = "session_id"
        const val COL_IS_GM = "is_from_gm"
        const val COL_TEXT = "message_text"
        const val COL_TIMESTAMP = "timestamp"
    }
}
