package com.example.littlerpg.data.repository

import com.example.littlerpg.domain.model.ClimaxEvent
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.ScenarioConclusion
import com.example.littlerpg.domain.model.ScenarioOrientation
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.StandardEvent
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.domain.repository.ScenarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implémentation du ScenarioRepository combinant le catalogue d'épreuves
 * et garantissant la sélection filtrée par EventTag avec fallback global.
 */
class ScenarioRepositoryImpl : ScenarioRepository {

    private val standardEventCatalog = listOf(
        StandardEvent("evt_1", "Embuscade sanglante", "Des mercenaires sortent de l'ombre !", EventTag.COMBAT, Skill.COMBAT),
        StandardEvent("evt_2", "Porte verrouillée", "Un lourd vantail en fer bloque le passage.", EventTag.OBSTACLE, Skill.BREAK),
        StandardEvent("evt_3", "Marchand mystérieux", "Un voyageur propose une énigmatique transaction.", EventTag.NPC, Skill.PERSUASION),
        StandardEvent("evt_4", "Fosse à piques", "Le sol se dérobe sous vos pas !", EventTag.TRAP, Skill.EXTREME_JUMP),
        StandardEvent("evt_5", "Runes d'Avarice", "Des symboles étincellent en séquence sur le mur.", EventTag.RIDDLE, Skill.LOCK_TRAP),
        StandardEvent("evt_6", "Sentinelle automate", "Un gardien mécanique recharge ses canons.", EventTag.COMBAT, Skill.SHOOT),
        StandardEvent("evt_7", "Éboulement", "Des rochers menacent d'obstruer la sortie.", EventTag.OBSTACLE, Skill.LIFT_PUSH),
        StandardEvent("evt_8", "Informateur masqué", "Une voix murmure des indices depuis l'obscurité.", EventTag.NPC, Skill.PERSUASION)
    )

    private val climaxEventCatalog = mapOf(
        ScenarioOrientation.BOUNTY_HUNT to ClimaxEvent("climax_1", "Confrontation Finale", "Le chef de la guilde criminelle se dresse face à vous.", "Capitaine Malakor", Skill.COMBAT),
        ScenarioOrientation.TREASURE_HUNT to ClimaxEvent("climax_2", "Le Gardien du Sanctuaire", "Un colosse de pierre s'éveille pour protéger le trésor.", "Golem d'Obsidienne", Skill.MAGIC),
        ScenarioOrientation.MYSTERY to ClimaxEvent("climax_3", "Révélation du Culte", "L'archimage corrompu invoque l'ombre séculaire.", "Malzar l'Ancien", Skill.LOCK_TRAP),
        ScenarioOrientation.NPC_ENCOUNTER to ClimaxEvent("climax_4", "Duel de Destinée", "Votre rival ultime réclame le duel final.", "Valerius le Renié", Skill.SHOOT)
    )

    private val conclusionCatalog = mapOf(
        ScenarioOrientation.BOUNTY_HUNT to ScenarioConclusion(
            "c_bounty",
            ScenarioOrientation.BOUNTY_HUNT,
            "Le contrat est honoré avec bravoure ! La prime vous revient de droit.",
            "La cible s'échappe dans la nuit. La quête se solde par un échec."
        ),
        ScenarioOrientation.TREASURE_HUNT to ScenarioConclusion(
            "c_treasure",
            ScenarioOrientation.TREASURE_HUNT,
            "Le reliquaire oublié s'ouvre ! Vous devenez le détenteur du trésor légendaire.",
            "Le piège ancestral scelle le tombeau. Le trésor reste perdu à jamais."
        ),
        ScenarioOrientation.MYSTERY to ScenarioConclusion(
            "c_mystery",
            ScenarioOrientation.MYSTERY,
            "La vérité est révélée ! Le mystère sombre qui pesait sur le royaume est enfin levé.",
            "L'illusion vous submerge. La vérité reste enfouie sous le sceau du secret."
        ),
        ScenarioOrientation.NPC_ENCOUNTER to ScenarioConclusion(
            "c_npc",
            ScenarioOrientation.NPC_ENCOUNTER,
            "Votre alliance s'avère indéfectible ! Ensemble, vous avez triomphé des épreuves.",
            "La discorde l'emporte. L'allié s'efface dans les ombres du doute."
        )
    )

    override suspend fun getStandardEventByTag(universe: UniverseType, tag: EventTag): StandardEvent = withContext(Dispatchers.Default) {
        val matchingEvents = standardEventCatalog.filter { it.tag == tag }
        if (matchingEvents.isNotEmpty()) {
            matchingEvents.random()
        } else {
            getRandomStandardEvent(universe)
        }
    }

    override suspend fun getRandomStandardEvent(universe: UniverseType): StandardEvent = withContext(Dispatchers.Default) {
        standardEventCatalog.random()
    }

    override suspend fun getClimaxEvent(universe: UniverseType, orientation: ScenarioOrientation): ClimaxEvent = withContext(Dispatchers.Default) {
        climaxEventCatalog[orientation] ?: ClimaxEvent(
            "climax_default",
            "Épreuve Ultime",
            "L'adversaire final se dresse au centre de la pièce.",
            "Ombre Suprême",
            Skill.COMBAT
        )
    }

    override suspend fun getLockedConclusion(orientation: ScenarioOrientation): ScenarioConclusion = withContext(Dispatchers.Default) {
        conclusionCatalog[orientation] ?: ScenarioConclusion(
            "c_default",
            orientation,
            "Vous avez triomphé avec honneur !",
            "La pénombre engloutit votre aventure..."
        )
    }
}
