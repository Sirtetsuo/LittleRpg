package com.example.littlerpg.presentation.viewmodel

import com.example.littlerpg.domain.engine.GameVictoryHandler
import com.example.littlerpg.domain.engine.NarrativeEngine
import com.example.littlerpg.domain.engine.NextEventTagStrategy
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.ActionResolution
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.GameItem
import com.example.littlerpg.domain.model.GameItemCatalog
import com.example.littlerpg.domain.model.ItemEffect
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.ScenarioRepository
import com.example.littlerpg.domain.usecase.ResolveActionUseCase

sealed interface MerchantResult {
    data class InsufficientGold(val gmMsgText: String) : MerchantResult
    data class Success(
        val updatedCharacter: Character,
        val drawnTag: EventTag?,
        val nextEvent: StandardEvent?,
        val gmMsgText: String,
        val nextOptions: List<ActionOption>
    ) : MerchantResult
}

data class StandardActionResult(
    val updatedCharacter: Character,
    val drawnTag: EventTag?,
    val nextEvent: StandardEvent?,
    val gmMsgText: String,
    val nextOptions: List<ActionOption>,
    val resolution: ActionResolution,
    val isDead: Boolean
)

class GameActionHandler(
    private val resolveActionUseCase: ResolveActionUseCase,
    private val narrativeEngine: NarrativeEngine,
    private val scenarioRepository: ScenarioRepository,
    private val tagStrategy: NextEventTagStrategy,
    private val victoryHandler: GameVictoryHandler
) {

    suspend fun handleMerchantPurchase(
        boughtItemName: String,
        character: Character,
        universe: UniverseType,
        targetTurn: Int,
        totalEvents: Int,
        currentTag: EventTag?,
        selectedOrientation: ScenarioOrientation?
    ): MerchantResult {
        val itemToBuy = GameItem.fromName(boughtItemName)
        val itemCost = itemToBuy.cost

        if (character.gold < itemCost) {
            val gmMsgText = "❌ PIÈCES D'OR INSUFFISANTES !\n" +
                    "L'objet '${itemToBuy.name}' coûte $itemCost PO, mais vous ne possédez que ${character.gold} PO.\n" +
                    "L'achat est refusé !"
            return MerchantResult.InsufficientGold(gmMsgText)
        }

        val newGold = character.gold - itemCost
        val cleanName = itemToBuy.name
        val updatedInventory = character.inventory + cleanName
        val updatedChar = character.copy(inventory = updatedInventory, gold = newGold)

        val nextOptionsForTurn: List<ActionOption>
        var drawnTag: EventTag? = currentTag
        var nextEvt: StandardEvent? = null

        val gmMsgText = if (targetTurn <= totalEvents) {
            val nextTag = tagStrategy.determineNextTag(
                previousTag = currentTag,
                currentStep = targetTurn,
                totalSteps = totalEvents,
                orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
            )
            nextEvt = scenarioRepository.getStandardEventByTag(universe, nextTag)
            drawnTag = nextEvt.tag
            nextOptionsForTurn = nextEvt.toActionOptions(updatedChar.inventory.firstOrNull())
            "🛍️ OBJET ACQUIS !\n" +
                    "Vous obtenez l'objet '$cleanName' (${itemToBuy.effectDescription}) pour $itemCost PO ! (Solde restant : $newGold PO).\n\n" +
                    "📍 ÉPREUVE $targetTurn/$totalEvents — ${nextEvt.title} :\n${nextEvt.description}"
        } else if (targetTurn == totalEvents + 1) {
            val orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
            val climax = scenarioRepository.getClimaxEvent(universe, orientation)
            nextOptionsForTurn = narrativeEngine.generateActionOptions(universe, targetTurn, updatedChar)
            "🛍️ OBJET ACQUIS !\n" +
                    "Vous obtenez '$cleanName' (${itemToBuy.effectDescription}) pour $itemCost PO ! (Solde restant : $newGold PO).\n\n" +
                    "🔥 CLIMAX FINAL — ${climax.title} !\n${climax.description}\nAdversaire ultime : ${climax.finalBossName}"
        } else {
            val catalogItems = GameItemCatalog.getItemsForUniverse(universe).shuffled().take(3)
            nextOptionsForTurn = listOf(
                ActionOption(201, "Acheter : ${catalogItems[0].name} (${catalogItems[0].cost} PO)", Skill.MAGIC),
                ActionOption(202, "Acheter : ${catalogItems[1].name} (${catalogItems[1].cost} PO)", Skill.PERSUASION),
                ActionOption(203, "Acheter : ${catalogItems[2].name} (${catalogItems[2].cost} PO)", Skill.LOCK_TRAP),
                ActionOption(99, "🔄 Continuer l'aventure (Niveau ${updatedChar.level})", Skill.PERSUASION)
            )
            "🛍️ OBJET ACQUIS AU MARCHÉ DE NIVEAU !\n" +
                    "Vous obtenez l'objet '$cleanName' (${itemToBuy.effectDescription}) pour $itemCost PO ! (Solde restant : $newGold PO).\n\n" +
                    "Vous pouvez effectuer un autre achat ci-dessous ou cliquer sur 'Continuer l'aventure' pour démarrer la quête de Niveau ${updatedChar.level} !"
        }

        return MerchantResult.Success(
            updatedCharacter = updatedChar,
            drawnTag = drawnTag,
            nextEvent = nextEvt,
            gmMsgText = gmMsgText,
            nextOptions = nextOptionsForTurn
        )
    }

    suspend fun handleStandardAction(
        option: ActionOption,
        character: Character,
        universe: UniverseType,
        targetTurn: Int,
        totalEvents: Int,
        currentTurnCount: Int,
        currentTag: EventTag?,
        currentOptions: List<ActionOption>,
        selectedOrientation: ScenarioOrientation?
    ): StandardActionResult {
        val isClimaxTurn = (currentTurnCount == totalEvents + 1)
        val hpLostOnFailure = if (isClimaxTurn) {
            (character.level / 2).coerceAtLeast(1)
        } else {
            (character.level / 3).coerceAtLeast(1)
        }

        val quadruple = resolveActionUseCase(
            universe = universe,
            character = character,
            option = option,
            currentOptions = currentOptions,
            turnCount = targetTurn,
            hpLostOnFailure = hpLostOnFailure
        )

        val charAfterItemUse = if (option.isItemAction && option.itemName != null) {
            val usedItem = GameItem.fromName(option.itemName)
            var updated = quadruple.updatedCharacter
            val effect = usedItem.parsedEffect
            if (effect is ItemEffect.Heal) {
                val healAmount = if (effect.dice) kotlin.random.Random.nextInt(1, 7) else effect.amount
                val maxHp = updated.getEffectiveMaxHp()
                val newHp = (updated.hp + healAmount).coerceAtMost(maxHp)
                updated = updated.copy(hp = newHp)
            }
            if (usedItem.isConsumable) {
                val newInv = updated.inventory.toMutableList().apply { remove(option.itemName) }
                updated = updated.copy(inventory = newInv)
            }
            updated
        } else {
            quadruple.updatedCharacter
        }

        val isDead = charAfterItemUse.hp <= 0
        val isSuccess = quadruple.resolution.isSuccess
        val newTurnCount = if (isSuccess) targetTurn else currentTurnCount

        var nextOptionsForTurn = currentOptions
        var drawnTag: EventTag? = currentTag
        var finalCharacterState = charAfterItemUse

        val gmMsgText = if (!isSuccess) {
            quadruple.gmMessage.text
        } else if (newTurnCount <= totalEvents) {
            val nextTag = tagStrategy.determineNextTag(
                previousTag = currentTag,
                currentStep = newTurnCount,
                totalSteps = totalEvents,
                orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
            )
            val nextEvent = scenarioRepository.getStandardEventByTag(universe, nextTag)
            drawnTag = nextEvent.tag
            nextOptionsForTurn = nextEvent.toActionOptions(charAfterItemUse.inventory.firstOrNull())
            val skillName = quadruple.resolution.skill.displayName
            val statName = quadruple.resolution.skill.attribute.displayName
            "🎲 RÉUSSITE ! [Action : ${option.label}] ($skillName - $statName)\n" +
                    "(Jet: ${quadruple.resolution.diceRoll} < Cible: ${quadruple.resolution.targetValue})\n\n" +
                    "📍 ÉPREUVE $newTurnCount/$totalEvents — ${nextEvent.title} :\n${nextEvent.description}"
        } else if (newTurnCount == totalEvents + 1) {
            val orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
            val climax = scenarioRepository.getClimaxEvent(universe, orientation)
            nextOptionsForTurn = narrativeEngine.generateActionOptions(universe, newTurnCount, charAfterItemUse)
            val skillName = quadruple.resolution.skill.displayName
            val statName = quadruple.resolution.skill.attribute.displayName
            "🎲 RÉUSSITE ! [Action : ${option.label}] ($skillName - $statName)\n" +
                    "(Jet: ${quadruple.resolution.diceRoll} < Cible: ${quadruple.resolution.targetValue})\n\n" +
                    "🔥 CLIMAX FINAL — ${climax.title} !\n" +
                    "${climax.description}\n" +
                    "Adversaire ultime : ${climax.finalBossName}"
        } else {
            val orientation = selectedOrientation ?: ScenarioOrientation.BOUNTY_HUNT
            val victoryRes = victoryHandler.createVictoryState(charAfterItemUse, orientation)
            finalCharacterState = victoryRes.victoriousCharacter
            nextOptionsForTurn = victoryRes.rewardOptions
            victoryRes.gmMessageText
        }

        return StandardActionResult(
            updatedCharacter = finalCharacterState,
            drawnTag = drawnTag,
            nextEvent = if (isSuccess && newTurnCount <= totalEvents) scenarioRepository.getStandardEventByTag(universe, drawnTag ?: EventTag.COMBAT) else null,
            gmMsgText = gmMsgText,
            nextOptions = if (isDead) emptyList() else if (isSuccess) nextOptionsForTurn else currentOptions,
            resolution = quadruple.resolution,
            isDead = isDead
        )
    }
}
