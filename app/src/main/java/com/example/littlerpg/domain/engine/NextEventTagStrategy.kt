package com.example.littlerpg.domain.engine

import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.ScenarioOrientation
import kotlin.random.Random

/**
 * Contrat d'interface (Strategy Pattern) pour la détermination du tag de l'épreuve suivante.
 */
interface NextEventTagStrategy {
    fun determineNextTag(
        previousTag: EventTag?,
        currentStep: Int,
        totalSteps: Int,
        orientation: ScenarioOrientation
    ): EventTag
}

/**
 * Stratégie d'enchaînement basée sur un système de disette (Starvation System).
 * Plus un tag tarde à être tiré, plus son poids et sa probabilité d'apparition augmentent.
 */
class StarvationNextEventTagStrategy(
    private val baseWeight: Double = 10.0,
    private val starvationStep: Double = 15.0,
    private val randomSupplier: () -> Double = { Random.nextDouble(0.0, 100.0) }
) : NextEventTagStrategy {

    private val starvationCounters = EventTag.entries.associateWith { 0 }.toMutableMap()

    override fun determineNextTag(
        previousTag: EventTag?,
        currentStep: Int,
        totalSteps: Int,
        orientation: ScenarioOrientation
    ): EventTag {
        return rollNextTag()
    }

    fun rollNextTag(): EventTag {
        val distribution = calculatePercentages()
        val roll = randomSupplier()
        var cumulative = 0.0
        var selectedTag = EventTag.entries.first()

        for ((tag, percentage) in distribution) {
            cumulative += percentage
            if (roll < cumulative) {
                selectedTag = tag
                break
            }
        }

        applyTurnOutcome(selectedTag)
        return selectedTag
    }

    private fun applyTurnOutcome(drawnTag: EventTag) {
        for (tag in EventTag.entries) {
            if (tag == drawnTag) {
                starvationCounters[tag] = 0
            } else {
                starvationCounters[tag] = (starvationCounters[tag] ?: 0) + 1
            }
        }
    }

    fun calculatePercentages(): Map<EventTag, Double> {
        val rawWeights = EventTag.entries.associateWith { tag ->
            val drought = starvationCounters[tag] ?: 0
            baseWeight + (starvationStep * drought)
        }

        val totalWeight = rawWeights.values.sum()
        if (totalWeight <= 0.0) {
            val defaultPct = 100.0 / EventTag.entries.size
            return EventTag.entries.associateWith { defaultPct }
        }

        return rawWeights.mapValues { (_, weight) ->
            (weight / totalWeight) * 100.0
        }
    }

    fun getStarvationCounters(): Map<EventTag, Int> = starvationCounters.toMap()

    fun resetCounters() {
        for (tag in EventTag.entries) {
            starvationCounters[tag] = 0
        }
    }
}
