package com.example.littlerpg.domain.engine

import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.GameItemCatalog
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.ScenarioRepository

data class VictoryResult(
    val victoriousCharacter: Character,
    val gmMessageText: String,
    val rewardOptions: List<ActionOption>
)

class GameVictoryHandler(private val scenarioRepository: ScenarioRepository) {

    suspend fun createVictoryState(
        charAfterItemUse: Character,
        orientation: ScenarioOrientation
    ): VictoryResult {
        val conclusion = scenarioRepository.getLockedConclusion(orientation)
        val newLevel = charAfterItemUse.level + 1
        val newMaxHpBase = charAfterItemUse.maxHp + 1
        val rewardGold = newLevel * 10 + 80
        val newGold = charAfterItemUse.gold + rewardGold
        val effectiveMaxHp = newMaxHpBase + charAfterItemUse.getPermanentHpBonus()
        val hpHeal = (effectiveMaxHp * 0.25).toInt().coerceAtLeast(1)
        val newHp = (charAfterItemUse.hp + hpHeal).coerceAtMost(effectiveMaxHp)

        val victoriousChar = charAfterItemUse.copy(
            level = newLevel,
            maxHp = newMaxHpBase,
            gold = newGold,
            hp = newHp
        )

        val rewardOptions = listOf(
            ActionOption(101, "💪 Gain de Force (+1 FOR)", Skill.COMBAT),
            ActionOption(102, "🧠 Gain d'Intelligence (+1 INT)", Skill.MAGIC),
            ActionOption(103, "⚡ Gain d'Agilité (+1 AGI)", Skill.SHOOT),
            ActionOption(104, "❤️ Gain de Vitalité (+2 PV)", Skill.LIFT_PUSH)
        )

        val gmMsgText = "🏆 Victoire vous vous sentez plus fort !\n\n" +
                "${conclusion.victoryText}\n\n" +
                "----------------------------------------\n" +
                "⬆️ NIVEAU SUPÉRIEUR : Vous passez Niveau $newLevel ! (PV Max +1)\n" +
                "🪙 TRÉSOR DE QUÊTE : +$rewardGold PO (Solde total : $newGold PO)\n" +
                "❤️ RÉGÉNÉRATION DE PV (+25%) : +$hpHeal PV ($newHp/$effectiveMaxHp PV actuels)\n" +
                "🎒 Tout votre inventaire et votre équipement sont conservés !\n\n" +
                "🎁 RÉCOMPENSE DE NIVEAU : Choisissez votre amélioration parmi les 4 actions ci-dessous :"

        return VictoryResult(
            victoriousCharacter = victoriousChar,
            gmMessageText = gmMsgText,
            rewardOptions = rewardOptions
        )
    }

    fun applyAttributeUpgrade(optionId: Int, character: Character, universe: UniverseType): Triple<Character, String, List<ActionOption>> {
        val updatedChar = when (optionId) {
            101 -> character.copy(strength = character.strength + 1)
            102 -> character.copy(intelligence = character.intelligence + 1)
            103 -> character.copy(agility = character.agility + 1)
            104 -> {
                val newMaxHp = character.maxHp + 2
                val newHp = (character.hp + 2).coerceAtMost(newMaxHp + character.getPermanentHpBonus())
                character.copy(maxHp = newMaxHp, hp = newHp)
            }
            else -> character
        }

        val bonusDetail = when (optionId) {
            101 -> "+1 Force (Nouvelle Force : ${updatedChar.strength})"
            102 -> "+1 Intelligence (Nouvelle Intelligence : ${updatedChar.intelligence})"
            103 -> "+1 Agilité (Nouvelle Agilité : ${updatedChar.agility})"
            104 -> "+2 PV (Nouveaux PV Max : ${updatedChar.getEffectiveMaxHp()})"
            else -> "Bonus d'attribut"
        }

        val catalogItems = GameItemCatalog.getItemsForUniverse(universe).shuffled().take(3)
        val merchantOptions = listOf(
            ActionOption(201, "Acheter : ${catalogItems[0].name} (${catalogItems[0].cost} PO)", Skill.MAGIC),
            ActionOption(202, "Acheter : ${catalogItems[1].name} (${catalogItems[1].cost} PO)", Skill.PERSUASION),
            ActionOption(203, "Acheter : ${catalogItems[2].name} (${catalogItems[2].cost} PO)", Skill.LOCK_TRAP),
            ActionOption(99, "🔄 Continuer l'aventure (Niveau ${updatedChar.level})", Skill.PERSUASION)
        )

        val merchantTitle = when (universe) {
            UniverseType.FANTASTIC -> "🏪 MARCHAND DE PASSAGE DE NIVEAU — L'Étal du Voyageur"
            UniverseType.CONTEMPORARY -> "🏪 MARCHAND DE PASSAGE DE NIVEAU — L'Armurerie Clandestine"
            UniverseType.FUTURISTIC -> "🏪 MARCHAND DE PASSAGE DE NIVEAU — Le Terminal Commercial Cyber"
        }

        val gmMsgText = "⭐ AMÉLIORATION ACQUISE !\n" +
                "Vous obtenez : $bonusDetail !\n\n" +
                "----------------------------------------\n" +
                "$merchantTitle\n" +
                "Un marchand spécial vous accueille suite à vos récents exploits ! Il vous propose ses marchandises (Solde disponible : ${updatedChar.gold} PO) :\n" +
                "1. ${catalogItems[0].name} — ${catalogItems[0].effectDescription} (${catalogItems[0].cost} PO)\n" +
                "2. ${catalogItems[1].name} — ${catalogItems[1].effectDescription} (${catalogItems[1].cost} PO)\n" +
                "3. ${catalogItems[2].name} — ${catalogItems[2].effectDescription} (${catalogItems[2].cost} PO)\n\n" +
                "Choisissez un objet à acheter ou continuez l'aventure !"

        return Triple(updatedChar, gmMsgText, merchantOptions)
    }
}
