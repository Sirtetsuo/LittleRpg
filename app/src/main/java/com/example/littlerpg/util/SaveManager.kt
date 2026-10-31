package com.example.littlerpg.util

import android.content.Context
import android.content.SharedPreferences
import com.example.littlerpg.model.Character
import com.example.littlerpg.model.ChoiceOption
import com.example.littlerpg.model.Dice100Result
import com.example.littlerpg.model.GameMode
import com.example.littlerpg.model.GameTurn
import com.example.littlerpg.model.Item
import com.example.littlerpg.model.MasterCharacter
import com.example.littlerpg.model.MasterTurn
import com.example.littlerpg.model.SaveState
import com.example.littlerpg.model.Skill
import com.example.littlerpg.model.SkillTier
import com.example.littlerpg.model.Universe
import org.json.JSONArray
import org.json.JSONObject

object SaveManager {

    private const val PREFS_NAME = "little_rpg_save_prefs"
    private const val KEY_SAVE_DATA = "save_state_json"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasSavedGame(context: Context): Boolean {
        val json = getPrefs(context).getString(KEY_SAVE_DATA, null)
        return !json.isNullOrEmpty()
    }

    fun clearSavedGame(context: Context) {
        getPrefs(context).edit().remove(KEY_SAVE_DATA).apply()
    }

    fun saveGame(context: Context, saveState: SaveState): Boolean {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put("universe", saveState.universe.name)
            jsonObject.put("gameMode", saveState.gameMode.name)
            jsonObject.put("timestamp", saveState.timestamp)
            jsonObject.put("character", characterToJson(saveState.character))

            val historyArray = JSONArray()
            saveState.storyHistory.forEach { historyArray.put(turnToJson(it)) }
            jsonObject.put("storyHistory", historyArray)

            val masterArray = JSONArray()
            saveState.masterHistory.forEach { masterArray.put(masterTurnToJson(it)) }
            jsonObject.put("masterHistory", masterArray)

            saveState.currentTurn?.let {
                jsonObject.put("currentTurn", turnToJson(it))
            }

            getPrefs(context).edit().putString(KEY_SAVE_DATA, jsonObject.toString()).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadGame(context: Context): SaveState? {
        val jsonStr = getPrefs(context).getString(KEY_SAVE_DATA, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val universe = Universe.valueOf(root.getString("universe"))
            val modeStr = root.optString("gameMode", GameMode.SOLO.name)
            val gameMode = try {
                GameMode.valueOf(modeStr)
            } catch (_: Exception) {
                GameMode.SOLO
            }
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())
            val character = jsonToCharacter(root.getJSONObject("character"))

            val historyArray = root.optJSONArray("storyHistory")
            val storyHistory = mutableListOf<GameTurn>()
            if (historyArray != null) {
                for (i in 0 until historyArray.length()) {
                    storyHistory.add(jsonToTurn(historyArray.getJSONObject(i)))
                }
            }

            val masterArray = root.optJSONArray("masterHistory")
            val masterHistory = mutableListOf<MasterTurn>()
            if (masterArray != null) {
                for (i in 0 until masterArray.length()) {
                    masterHistory.add(jsonToMasterTurn(masterArray.getJSONObject(i)))
                }
            }

            val currentTurn = if (root.has("currentTurn")) {
                jsonToTurn(root.getJSONObject("currentTurn"))
            } else null

            SaveState(
                character = character,
                universe = universe,
                storyHistory = storyHistory,
                currentTurn = currentTurn,
                gameMode = gameMode,
                masterHistory = masterHistory,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun characterToJson(c: Character): JSONObject {
        val obj = JSONObject()
        obj.put("name", c.name)
        obj.put("nickname", c.nickname)
        obj.put("job", c.job)
        obj.put("race", c.race)
        obj.put("age", c.age)
        obj.put("gender", c.gender)
        obj.put("particularity", c.particularity)
        obj.put("hp", c.hp)
        obj.put("maxHp", c.maxHp)
        obj.put("jokers", c.jokers)

        val skillsArray = JSONArray()
        c.skills.forEach { s ->
            val sObj = JSONObject()
            sObj.put("name", s.name)
            sObj.put("description", s.description)
            sObj.put("percentage", s.percentage)
            sObj.put("tier", s.tier.name)
            skillsArray.put(sObj)
        }
        obj.put("skills", skillsArray)

        val itemsArray = JSONArray()
        c.inventory.forEach { item ->
            val iObj = JSONObject()
            iObj.put("id", item.id)
            iObj.put("name", item.name)
            iObj.put("description", item.description)
            iObj.put("perk", item.perk)
            iObj.put("isUsed", item.isUsed)
            itemsArray.put(iObj)
        }
        obj.put("inventory", itemsArray)

        return obj
    }

    private fun jsonToCharacter(obj: JSONObject): Character {
        val name = obj.getString("name")
        val nickname = obj.optString("nickname", "")
        val job = obj.optString("job", "")
        val race = obj.optString("race", "")
        val age = obj.optString("age", "")
        val gender = obj.optString("gender", "M")
        val particularity = obj.optString("particularity", "")
        val hp = obj.optInt("hp", 3)
        val maxHp = obj.optInt("maxHp", 3)
        val jokers = obj.optInt("jokers", 2)

        val skillsList = mutableListOf<Skill>()
        if (obj.has("skills")) {
            val sArr = obj.getJSONArray("skills")
            for (i in 0 until sArr.length()) {
                val sObj = sArr.getJSONObject(i)
                val tierStr = sObj.optString("tier", SkillTier.BASE.name)
                val tier = try {
                    SkillTier.valueOf(tierStr)
                } catch (_: Exception) {
                    SkillTier.BASE
                }
                skillsList.add(
                    Skill(
                        name = sObj.getString("name"),
                        description = sObj.optString("description", ""),
                        percentage = sObj.optInt("percentage", 40),
                        tier = tier
                    )
                )
            }
        }

        val itemsList = mutableListOf<Item>()
        if (obj.has("inventory")) {
            val iArr = obj.getJSONArray("inventory")
            for (i in 0 until iArr.length()) {
                val iObj = iArr.getJSONObject(i)
                itemsList.add(
                    Item(
                        id = iObj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = iObj.getString("name"),
                        description = iObj.optString("description", ""),
                        perk = iObj.optString("perk", ""),
                        isUsed = iObj.optBoolean("isUsed", false)
                    )
                )
            }
        }

        return Character(
            name = name,
            nickname = nickname,
            job = job,
            race = race,
            age = age,
            gender = gender,
            particularity = particularity,
            skills = skillsList,
            inventory = itemsList,
            hp = hp,
            maxHp = maxHp,
            jokers = jokers
        )
    }

    private fun turnToJson(turn: GameTurn): JSONObject {
        val obj = JSONObject()
        obj.put("turnId", turn.turnId)
        obj.put("narrative", turn.narrative)
        obj.put("suggestedSkill", turn.suggestedSkill ?: "")
        obj.put("isCombat", turn.isCombat)
        obj.put("hpChanged", turn.hpChanged)

        val choicesArr = JSONArray()
        turn.choices.forEach { c ->
            val cObj = JSONObject()
            cObj.put("id", c.id)
            cObj.put("text", c.text)
            cObj.put("suggestedSkill", c.suggestedSkill ?: "")
            choicesArr.put(cObj)
        }
        obj.put("choices", choicesArr)

        turn.diceResult?.let { d ->
            val dObj = JSONObject()
            dObj.put("rollValue", d.rollValue)
            dObj.put("skillName", d.skillName)
            dObj.put("targetPercentage", d.targetPercentage)
            dObj.put("isSuccess", d.isSuccess)
            dObj.put("usedJoker", d.usedJoker)
            dObj.put("summary", d.summary)
            obj.put("diceResult", dObj)
        }

        return obj
    }

    private fun jsonToTurn(obj: JSONObject): GameTurn {
        val turnId = obj.optInt("turnId", 1)
        val narrative = obj.getString("narrative")
        val suggestedSkill = obj.optString("suggestedSkill").takeIf { it.isNotEmpty() }
        val isCombat = obj.optBoolean("isCombat", false)
        val hpChanged = obj.optInt("hpChanged", 0)

        val choicesList = mutableListOf<ChoiceOption>()
        if (obj.has("choices")) {
            val cArr = obj.getJSONArray("choices")
            for (i in 0 until cArr.length()) {
                val cObj = cArr.getJSONObject(i)
                choicesList.add(
                    ChoiceOption(
                        id = cObj.getInt("id"),
                        text = cObj.getString("text"),
                        suggestedSkill = cObj.optString("suggestedSkill").takeIf { it.isNotEmpty() }
                    )
                )
            }
        }

        val diceResult = if (obj.has("diceResult")) {
            val dObj = obj.getJSONObject("diceResult")
            Dice100Result(
                rollValue = dObj.getInt("rollValue"),
                skillName = dObj.getString("skillName"),
                targetPercentage = dObj.getInt("targetPercentage"),
                isSuccess = dObj.getBoolean("isSuccess"),
                usedJoker = dObj.optBoolean("usedJoker", false),
                summary = dObj.optString("summary", "")
            )
        } else null

        return GameTurn(
            turnId = turnId,
            narrative = narrative,
            choices = choicesList,
            suggestedSkill = suggestedSkill,
            diceResult = diceResult,
            isCombat = isCombat,
            hpChanged = hpChanged
        )
    }

    private fun masterTurnToJson(turn: MasterTurn): JSONObject {
        val obj = JSONObject()
        obj.put("turnId", turn.turnId)
        obj.put("dmPrompt", turn.dmPrompt)
        obj.put("aiNarrative", turn.aiNarrative)
        obj.put("isError", turn.isError)
        val partyArr = JSONArray()
        turn.partyStatus.forEach { member ->
            val mObj = JSONObject()
            mObj.put("name", member.name)
            mObj.put("role", member.role)
            mObj.put("status", member.status)
            mObj.put("avatarEmoji", member.avatarEmoji)
            partyArr.put(mObj)
        }
        obj.put("partyStatus", partyArr)
        return obj
    }

    private fun jsonToMasterTurn(obj: JSONObject): MasterTurn {
        val turnId = obj.optInt("turnId", 1)
        val dmPrompt = obj.optString("dmPrompt", "")
        val aiNarrative = obj.optString("aiNarrative", "")
        val isError = obj.optBoolean("isError", false)
        val partyList = mutableListOf<MasterCharacter>()
        if (obj.has("partyStatus")) {
            val pArr = obj.getJSONArray("partyStatus")
            for (i in 0 until pArr.length()) {
                val pObj = pArr.getJSONObject(i)
                partyList.add(
                    MasterCharacter(
                        name = pObj.optString("name", ""),
                        role = pObj.optString("role", ""),
                        status = pObj.optString("status", ""),
                        avatarEmoji = pObj.optString("avatarEmoji", "🎭")
                    )
                )
            }
        }
        return MasterTurn(
            turnId = turnId,
            dmPrompt = dmPrompt,
            aiNarrative = aiNarrative,
            partyStatus = partyList,
            isError = isError
        )
    }
}
