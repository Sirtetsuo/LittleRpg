package com.example.littlerpg.data.db

import android.database.Cursor
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.IntroTemplate
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType

class GameEventDataSource(private val dbHelper: GameDbHelper) {

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

    private fun parseStandardEventFromCursor(c: Cursor): StandardEvent {
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
