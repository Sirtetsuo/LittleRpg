package com.example.littlerpg.domain.engine

import com.example.littlerpg.data.repository.ScenarioRepositoryImpl
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.ScenarioState
import com.example.littlerpg.domain.model.UniverseType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScenarioEngineTest {

    private lateinit var repository: ScenarioRepositoryImpl
    private lateinit var tagStrategy: StarvationNextEventTagStrategy
    private lateinit var engine: ScenarioEngine

    @Before
    fun setUp() {
        repository = ScenarioRepositoryImpl()
        tagStrategy = StarvationNextEventTagStrategy()
        engine = ScenarioEngine(repository, tagStrategy)
    }

    @Test
    fun testEventCountCalculationFormula() {
        assertEquals(2, engine.calculateEventCount(1))
        assertEquals(3, engine.calculateEventCount(2))
        assertEquals(3, engine.calculateEventCount(3))
        assertEquals(4, engine.calculateEventCount(4))
        assertEquals(4, engine.calculateEventCount(5))
    }

    @Test
    fun testStarvationTagStrategyWeightsIncrease() {
        val initialPercentages = tagStrategy.calculatePercentages()
        assertEquals(20.0, initialPercentages[EventTag.COMBAT]!!, 0.01)

        val drawnTag = tagStrategy.rollNextTag()
        val updatedPercentages = tagStrategy.calculatePercentages()

        assertEquals(0, tagStrategy.getStarvationCounters()[drawnTag])
        assertTrue(updatedPercentages[drawnTag]!! < 20.0)

        val otherTag = EventTag.entries.first { it != drawnTag }
        assertEquals(1, tagStrategy.getStarvationCounters()[otherTag])
        assertTrue(updatedPercentages[otherTag]!! > 20.0)
    }

    @Test
    fun testFullScenarioLifecycle_VictoryFlow() = runBlocking {
        // Phase 1 : Intro & calcul des épreuves (Level 1 -> 2 épreuves)
        val introState = engine.startScenario(UniverseType.FANTASTIC, playerLevel = 1)
        assertTrue(introState is ScenarioState.Intro)

        // Phase 1 -> Phase 2 : Sélection de l'orientation et verrouillage conclusion
        val midGame1 = engine.selectOrientation(ScenarioOrientation.BOUNTY_HUNT)
        assertTrue(midGame1 is ScenarioState.MidGameEvent)
        val event1 = midGame1 as ScenarioState.MidGameEvent
        assertEquals(1, event1.currentEventIndex)
        assertEquals(2, event1.totalEvents)
        assertNotNull(event1.lockedConclusion)

        // Phase 2 : Deuxième épreuve
        val midGame2 = engine.resolveCurrentEvent(isSuccess = true)
        assertTrue(midGame2 is ScenarioState.MidGameEvent)
        val event2 = midGame2 as ScenarioState.MidGameEvent
        assertEquals(2, event2.currentEventIndex)

        // Phase 2 -> Phase 3 : Climax final
        val climaxState = engine.resolveCurrentEvent(isSuccess = true)
        assertTrue(climaxState is ScenarioState.ClimaxPhase)
        val climax = climaxState as ScenarioState.ClimaxPhase
        assertNotNull(climax.climaxEvent)

        // Phase 3 : Résolution climax -> Dévoilement conclusion verrouillée (Victoire)
        val conclusionState = engine.resolveClimax(isSuccess = true)
        assertTrue(conclusionState is ScenarioState.ConclusionPhase)
        val conclusion = conclusionState as ScenarioState.ConclusionPhase
        assertTrue(conclusion.isVictory)
        assertTrue(conclusion.conclusionText.contains("contrat est honoré"))

        // Phase Terminale : Finished
        val finishedState = engine.finishGame()
        assertTrue(finishedState is ScenarioState.Finished)
        assertTrue((finishedState as ScenarioState.Finished).isVictory)
    }
}
