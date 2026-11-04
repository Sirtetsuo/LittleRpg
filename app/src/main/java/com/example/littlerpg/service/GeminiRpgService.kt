package com.example.littlerpg.service

import android.util.Log
import com.example.littlerpg.model.Character
import com.example.littlerpg.model.ChoiceOption
import com.example.littlerpg.model.Dice100Result
import com.example.littlerpg.model.GameRulesPrompt
import com.example.littlerpg.model.GameTurn
import com.example.littlerpg.model.Item
import com.example.littlerpg.model.MasterCharacter
import com.example.littlerpg.model.MasterTurn
import com.example.littlerpg.model.Skill
import com.example.littlerpg.model.SkillTier
import com.example.littlerpg.model.Universe
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiRpgService {

    private suspend fun safeGenerateContent(prompt: String): String {
        val models = listOf("gemini-3.5-flash-lite", "gemini-3.8-flash")
        var lastException: Exception? = null

        for (modelName in models) {
            val model = Firebase.ai.generativeModel(
                modelName = modelName,
                systemInstruction = content { text(GameRulesPrompt.SYSTEM_PROMPT) }
            )

            var currentDelay = 1000L
            for (attempt in 1..3) {
                try {
                    val response = model.generateContent(prompt)
                    return response.text ?: throw IllegalStateException("Empty response from $modelName")
                } catch (e: Exception) {
                    lastException = e
                    Log.w("GeminiRpgService", "Model $modelName Attempt $attempt/3 failed: ${e.message}")
                    if (attempt == 3) break
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay *= 2
                }
            }
        }
        throw lastException ?: IllegalStateException("All models failed")
    }

    private suspend fun safeGenerateMasterContent(universe: Universe, prompt: String): String {
        val models = listOf("gemini-3.5-flash-lite", "gemini-3.8-flash")
        var lastException: Exception? = null

        for (modelName in models) {
            val model = Firebase.ai.generativeModel(
                modelName = modelName,
                systemInstruction = content { text(GameRulesPrompt.getMasterSystemPrompt(universe)) }
            )

            var currentDelay = 1000L
            for (attempt in 1..3) {
                try {
                    val response = model.generateContent(prompt)
                    return response.text ?: throw IllegalStateException("Empty response from $modelName")
                } catch (e: Exception) {
                    lastException = e
                    Log.w("GeminiRpgService", "Model $modelName Attempt $attempt/3 failed: ${e.message}")
                    if (attempt == 3) break
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay *= 2
                }
            }
        }
        throw lastException ?: IllegalStateException("All models failed")
    }

    suspend fun generateInitialSkills(universe: Universe, character: Character): List<Skill> {
        return withContext(Dispatchers.IO) {
            val prompt = GameRulesPrompt.buildInitialSkillsPrompt(universe, character)
            Log.d("GeminiRpgService", "generateInitialSkills Prompt:\n$prompt")
            try {
                val text = safeGenerateContent(prompt)
                Log.d("GeminiRpgService", "generateInitialSkills Raw Response:\n$text")
                parseSkillsJson(text)
            } catch (e: Exception) {
                Log.e("GeminiRpgService", "Gemini API Error during generateInitialSkills: ${e.message}", e)
                getFallbackSkills(universe)
            }
        }
    }

    suspend fun generateInitialItems(universe: Universe, character: Character, skills: List<Skill>): List<Item> {
        return withContext(Dispatchers.IO) {
            val prompt = GameRulesPrompt.buildInitialItemsPrompt(universe, character, skills)
            Log.d("GeminiRpgService", "generateInitialItems Prompt:\n$prompt")
            try {
                val text = safeGenerateContent(prompt)
                Log.d("GeminiRpgService", "generateInitialItems Raw Response:\n$text")
                parseItemsJson(text)
            } catch (e: Exception) {
                Log.e("GeminiRpgService", "Gemini API Error during generateInitialItems: ${e.message}", e)
                getFallbackItems(universe)
            }
        }
    }

    suspend fun generateStoryTurn(
        universe: Universe,
        character: Character,
        history: List<GameTurn>,
        actionText: String,
        diceResult: Dice100Result?
    ): GameTurn {
        return withContext(Dispatchers.IO) {
            val prompt = GameRulesPrompt.buildStoryTurnPrompt(universe, character, history, actionText, diceResult)
            Log.d("GeminiRpgService", "generateStoryTurn Prompt:\n$prompt")
            val currentTurnId = (history.maxOfOrNull { it.turnId } ?: 0) + 1
            try {
                val text = safeGenerateContent(prompt)
                Log.d("GeminiRpgService", "generateStoryTurn Raw Response:\n$text")
                parseStoryTurnJson(text, currentTurnId, diceResult)
            } catch (e: Exception) {
                Log.e("GeminiRpgService", "Gemini API Error during generateStoryTurn: ${e.message}", e)
                getFallbackStoryTurn(currentTurnId)
            }
        }
    }

    suspend fun generateMasterTurn(
        universe: Universe,
        history: List<MasterTurn>,
        dmActionText: String
    ): MasterTurn {
        return withContext(Dispatchers.IO) {
            val prompt = GameRulesPrompt.buildMasterTurnPrompt(universe, history, dmActionText)
            Log.d("GeminiRpgService", "generateMasterTurn Prompt:\n$prompt")
            val currentTurnId = (history.maxOfOrNull { it.turnId } ?: 0) + 1
            try {
                val text = safeGenerateMasterContent(universe, prompt)
                Log.d("GeminiRpgService", "generateMasterTurn Raw Response:\n$text")
                parseMasterTurnJson(text, currentTurnId, dmActionText)
            } catch (e: Exception) {
                Log.e("GeminiRpgService", "Gemini API Error during generateMasterTurn: ${e.message}", e)
                getFallbackMasterTurn(currentTurnId, dmActionText)
            }
        }
    }

    private fun parseSkillsJson(text: String): List<Skill> {
        return try {
            val cleanJson = extractJsonContent(text)
            val json = JSONObject(cleanJson)
            val arr = json.getJSONArray("skills")
            val result = mutableListOf<Skill>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(
                    Skill(
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        percentage = 40,
                        tier = SkillTier.BASE
                    )
                )
            }
            if (result.size >= 6) result else getFallbackSkills(Universe.FANTASTIQUE)
        } catch (_: Exception) {
            Log.w("GeminiRpgService", "Failed to parse skills JSON, using fallback.")
            getFallbackSkills(Universe.FANTASTIQUE)
        }
    }

    private fun parseItemsJson(text: String): List<Item> {
        return try {
            val cleanJson = extractJsonContent(text)
            val json = JSONObject(cleanJson)
            val arr = json.getJSONArray("items")
            val result = mutableListOf<Item>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(
                    Item(
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        perk = obj.optString("perk", "")
                    )
                )
            }
            if (result.size >= 3) result else getFallbackItems(Universe.FANTASTIQUE)
        } catch (_: Exception) {
            Log.w("GeminiRpgService", "Failed to parse items JSON, using fallback.")
            getFallbackItems(Universe.FANTASTIQUE)
        }
    }

    private fun parseStoryTurnJson(text: String, turnId: Int, diceResult: Dice100Result?): GameTurn {
        return try {
            val cleanJson = extractJsonContent(text)
            val json = JSONObject(cleanJson)

            val narrative = json.optString("narrative", text)
            val suggestedSkill = json.optString("suggestedSkill").takeIf { !it.isNullOrEmpty() }
            val isCombat = json.optBoolean("isCombat", false)
            val hpChange = json.optInt("hpChange", 0)

            val choicesList = mutableListOf<ChoiceOption>()
            if (json.has("choices")) {
                val arr = json.getJSONArray("choices")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    choicesList.add(
                        ChoiceOption(
                            id = obj.optInt("id", i + 1),
                            text = obj.getString("text"),
                            suggestedSkill = obj.optString("suggestedSkill").takeIf { !it.isNullOrEmpty() }
                        )
                    )
                }
            }

            GameTurn(
                turnId = turnId,
                narrative = narrative,
                choices = choicesList,
                suggestedSkill = suggestedSkill,
                diceResult = diceResult,
                isCombat = isCombat,
                hpChanged = hpChange
            )
        } catch (e: Exception) {
            Log.w("GeminiRpgService", "Failed to parse StoryTurn JSON, using raw text narrative.")
            GameTurn(
                turnId = turnId,
                narrative = text.trim(),
                choices = listOf(
                    ChoiceOption(1, "Continuer l'aventure", null),
                    ChoiceOption(2, "Observer les alentours", null),
                    ChoiceOption(3, "Avancer prudemment", null)
                ),
                diceResult = diceResult
            )
        }
    }

    private fun parseMasterTurnJson(text: String, turnId: Int, dmPrompt: String): MasterTurn {
        return try {
            val cleanJson = extractJsonContent(text)
            val json = JSONObject(cleanJson)

            val narrative = json.optString("narrative", text)
            val partyList = mutableListOf<MasterCharacter>()

            if (json.has("partyStatus")) {
                val arr = json.getJSONArray("partyStatus")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    partyList.add(
                        MasterCharacter(
                            name = obj.optString("name", ""),
                            role = obj.optString("role", ""),
                            status = obj.optString("status", ""),
                            avatarEmoji = obj.optString("avatarEmoji", "🎭")
                        )
                    )
                }
            }

            MasterTurn(
                turnId = turnId,
                dmPrompt = dmPrompt,
                aiNarrative = narrative,
                partyStatus = partyList
            )
        } catch (e: Exception) {
            Log.w("GeminiRpgService", "Failed to parse MasterTurn JSON, using raw text narrative.")
            MasterTurn(
                turnId = turnId,
                dmPrompt = dmPrompt,
                aiNarrative = text.trim(),
                partyStatus = emptyList()
            )
        }
    }

    private fun extractJsonContent(text: String): String {
        val trimmed = text.trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1)
        }
        return trimmed
    }

    private fun getFallbackSkills(universe: Universe): List<Skill> {
        return when (universe) {
            Universe.FANTASTIQUE -> listOf(
                Skill("Combat à l'Épée ⚔️", "Maîtrise des armes blanches et du corps-à-corps"),
                Skill("Magie & Sortilèges 🔮", "Incantation de sorts profanes ou divins"),
                Skill("Discrétion & Crochetage 🗡️", "Se déplacer en silence et ouvrir les verrous"),
                Skill("Persuasion & Intimidation 🗣️", "Convaincre ou effrayer les PNJ"),
                Skill("Athlétisme & Escalade 🏃‍♂️", "Franchir les obstacles et sauter les ravins"),
                Skill("Connaissance des Arcanes 📜", "Identifier les glyphes, objets magiques et légendes")
            )
            Universe.SPACE_OPERA -> listOf(
                Skill("Tir au Blaster 🔫", "Précision avec les armes à énergie"),
                Skill("Pilotage de Vaisseau 🚀", "Maniement des chasseurs et navires stellaires"),
                Skill("Pirates du Réseau (Hacking) 💻", "Infiltration des réseaux cybernétiques"),
                Skill("Négociation Galactique 🤝", "Diplomatie et troc avec les espèces aliens"),
                Skill("Mécanique & Bricolage ⚙️", "Réparation des moteurs hyperpropulseurs"),
                Skill("Médecine Spatiale 💉", "Soigner les blessures et analyser les toxines")
            )
            Universe.STEAMPUNK -> listOf(
                Skill("Tir au Mousquet 💥", "Armes à feu à vapeur et mécanismes à rouage"),
                Skill("Ingénierie & Rouages ⚙️", "Création et piratage d'automates à vapeur"),
                Skill("Pilotage de Dirigeable 🎈", "Navigation aérienne et acrobaties célestes"),
                Skill("Éloquence Bourgeoise 🎩", "Manières distinguées et rhétorique élégante"),
                Skill("Bagarre de Taverne 👊", "Combat de rue à coups de poing et canne"),
                Skill("Alchimie & Explosifs 🧪", "Préparation de potions et bombes fumigènes")
            )
            Universe.CONTEMPORAIN -> listOf(
                Skill("Conduite Sportive 🚗", "Maniement rapide des véhicules urbains"),
                Skill("Enquête & Déduction 🔍", "Repérer les indices cachés et analyser les scènes"),
                Skill("Tir au Pistolet 🎯", "Utilisation d'armes de poing modernes"),
                Skill("Premiers Secours 🏥", "Soigner les traumatismes et stabiliser les blessés"),
                Skill("Piratage Informatique 🖥️", "Extraire les données et débloquer les accès"),
                Skill("Art des Arts Martiaux 🥋", "Combat au corps-à-corps et esquive")
            )
        }
    }

    private fun getFallbackItems(universe: Universe): List<Item> {
        return when (universe) {
            Universe.FANTASTIQUE -> listOf(
                Item(name = "🛡️ Cotte de Mailles Légère", description = "Armure forgée par les nains.", perk = "Absorbe 1 point de dégât une fois dans l'aventure."),
                Item(name = "⚔️ Épée Runique Gravée", description = "Une lame étincelante d'énergie.", perk = "Élimine automatiquement un ennemi une fois par aventure."),
                Item(name = "🧪 Potion de Soin Mystique", description = "Une fiole au liquide rougeoyant.", perk = "Restaure 1 point de vie immédiatement.")
            )
            Universe.SPACE_OPERA -> listOf(
                Item(name = "🛡️ Bouclier à Plasma Personnel", description = "Générateur de champ déflecteur.", perk = "Absorbe 1 attaque ennemie une fois par mission."),
                Item(name = "🔫 Blaster Lourd Modifié", description = "Ajusté pour une surcharge de tir.", perk = "Neutralise une escouade ou gros ennemi une fois par aventure."),
                Item(name = "💉 Injecteur de Nanobots", description = "Stimulant médical d'urgence.", perk = "Restaure 1 point de vie en plein combat.")
            )
            Universe.STEAMPUNK -> listOf(
                Item(name = "🛡️ Cuirasse de Cuivre Renforcée", description = "Plastron étanche à la vapeur.", perk = "Protège contre 1 blessure une fois dans la quête."),
                Item(name = "⚙️ Pistolet à Vapeur à Pression", description = "Tir explosif à courte portée.", perk = "Détruit un obstacle ou ennemi une fois par mission."),
                Item(name = "🎈 Grappin Automatique à Rouages", description = "Propulsé par cartouche de gaz.", perk = "Permet d'esquiver un danger mortel ou fuir.")
            )
            Universe.CONTEMPORAIN -> listOf(
                Item(name = "🛡️ Gilet Pare-Balles K-12", description = "Protection balistique renforcée.", perk = "Absorbe 1 tir ennemi sans subir de blessure."),
                Item(name = "🎯 Taser Haute Tension", description = "Neutralise à distance.", perk = "Met un adversaire hors de combat instantanément."),
                Item(name = "🩹 Trousse de Secours Tactique", description = "Matériel de suture et antiseptique.", perk = "Restaure 1 point de vie.")
            )
        }
    }

    private fun getFallbackStoryTurn(turnId: Int): GameTurn {
        return GameTurn(
            turnId = turnId,
            narrative = "⚠️ Problème de connexion avec l'IA. Veuillez réimprimer ou réessayer votre action.",
            choices = listOf(
                ChoiceOption(1, "🔄 Réessayer de continuer l'aventure", null),
                ChoiceOption(2, "🔍 Observer attentivement les alentours", null),
                ChoiceOption(3, "🚶‍♂️ Avancer prudemment", null)
            ),
            isError = true
        )
    }

    private fun getFallbackMasterTurn(turnId: Int, dmPrompt: String): MasterTurn {
        return MasterTurn(
            turnId = turnId,
            dmPrompt = dmPrompt,
            aiNarrative = "⚠️ Problème de connexion avec l'IA. Les aventuriers attendent vos consignes.",
            isError = true
        )
    }
}
