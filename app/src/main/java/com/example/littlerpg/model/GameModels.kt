package com.example.littlerpg.model

import androidx.annotation.DrawableRes
import com.example.littlerpg.R

enum class Universe(
    val displayName: String,
    val description: String,
    val emoji: String,
    @get:DrawableRes val imageRes: Int
) {
    FANTASTIQUE("Fantastique", "Magie, dragons, elfes et donjons obscurs", "🧙‍♂️", R.drawable.donjon),
    SPACE_OPERA("Space Opéra", "Vaisseaux spatiaux, galaxies lointaines et aliens", "🚀", R.drawable.space),
    STEAMPUNK("SteamPunk", "Machines à vapeur, engrenages, dirigeables et magie cuivrée", "⚙️", R.drawable.steampunk),
    CONTEMPORAIN("Contemporain", "Monde moderne, enquêtes nocturnes et mystères urbains", "🏙️", R.drawable.contemporain)
}

enum class SkillTier(val label: String, val percentage: Int, val emoji: String) {
    MAITRE("Maître", 70, "🥇"),
    EXPERT("Expert", 60, "🥈"),
    ANNEXE("Annexe", 50, "🥉"),
    BASE("Base", 40, "⚪")
}

data class Skill(
    val name: String,
    val description: String = "",
    var percentage: Int = 40,
    var tier: SkillTier = SkillTier.BASE
)

data class Item(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val perk: String = "",
    var isUsed: Boolean = false
)

data class Character(
    val name: String = "",
    val nickname: String = "",
    val job: String = "",
    val race: String = "",
    val age: String = "",
    val gender: String = "M",
    val particularity: String = "",
    val skills: List<Skill> = emptyList(),
    val inventory: List<Item> = emptyList(),
    var hp: Int = 3,
    val maxHp: Int = 3,
    var jokers: Int = 2
) {
    val isAlive: Boolean get() = hp > 0
}

data class ChoiceOption(
    val id: Int,
    val text: String,
    val suggestedSkill: String? = null
)

data class Dice100Result(
    val rollValue: Int,
    val skillName: String,
    val targetPercentage: Int,
    val isSuccess: Boolean,
    val usedJoker: Boolean = false,
    val summary: String = ""
)

data class PendingJokerAction(
    val actionText: String,
    val diceResult: Dice100Result
)

data class GameTurn(
    val turnId: Int = 1,
    val narrative: String,
    val choices: List<ChoiceOption> = emptyList(),
    val suggestedSkill: String? = null,
    val diceResult: Dice100Result? = null,
    val isCombat: Boolean = false,
    val hpChanged: Int = 0,
    val isError: Boolean = false
)

data class SaveState(
    val character: Character,
    val universe: Universe,
    val storyHistory: List<GameTurn>,
    val currentTurn: GameTurn?,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AppScreen {
    HOME,
    UNIVERSE_SELECTION,
    CHARACTER_CREATION,
    SKILL_SELECTION,
    ITEM_SELECTION,
    GAME_PLAY,
    GAME_OVER
}
