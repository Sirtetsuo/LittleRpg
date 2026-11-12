package com.example.littlerpg.domain.repository

import com.example.littlerpg.domain.model.ClimaxEvent
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.IntroTemplate
import com.example.littlerpg.domain.model.ScenarioConclusion
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType

/**
 * Contrat de dépôt (Repository) abstrayant l'accès aux épreuves et conclusions
 * depuis la base de données SQLite locale.
 */
interface ScenarioRepository {
    /**
     * Récupère une épreuve standard correspondant au tag demandé.
     * En cas d'absence d'événement correspondant en base, un fallback vers une épreuve aléatoire est appliqué.
     */
    suspend fun getStandardEventByTag(universe: UniverseType, tag: EventTag): StandardEvent

    /**
     * Tirage aléatoire global d'une épreuve standard de secours.
     */
    suspend fun getRandomStandardEvent(universe: UniverseType): StandardEvent

    /**
     * Récupère un événement de Climax final spécifique depuis la table dédiée.
     */
    suspend fun getClimaxEvent(universe: UniverseType, orientation: ScenarioOrientation): ClimaxEvent

    /**
     * Récupère la conclusion narrative associée à l'orientation choisie en Phase 1.
     */
    suspend fun getLockedConclusion(orientation: ScenarioOrientation): ScenarioConclusion

    /**
     * Récupère un modèle d'introduction tiré dynamiquement de la base de données.
     */
    suspend fun getRandomIntroTemplate(universe: UniverseType): IntroTemplate
}
