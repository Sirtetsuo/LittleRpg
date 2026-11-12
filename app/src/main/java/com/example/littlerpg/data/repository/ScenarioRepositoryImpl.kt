package com.example.littlerpg.data.repository

import com.example.littlerpg.data.db.GameDatabaseDataSource
import com.example.littlerpg.domain.model.ClimaxEvent
import com.example.littlerpg.domain.model.EventTag
import com.example.littlerpg.domain.model.IntroTemplate
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
class ScenarioRepositoryImpl(
    private val dataSource: GameDatabaseDataSource? = null
) : ScenarioRepository {

    private val introTemplateCatalog = listOf(
        // FANTASTIC (7)
        IntroTemplate(1, "une", "taverne bruyante", "où les aventuriers racontent leurs exploits autour de choppes de chouchen", UniverseType.FANTASTIC),
        IntroTemplate(2, "une", "guilde des aventuriers", "dont le tableau des quêtes regorge d'avis de recherche et de contrats urgents", UniverseType.FANTASTIC),
        IntroTemplate(3, "un", "entrepôt abandonné", "reconverti en marché noir où circulent d'étranges reliques", UniverseType.FANTASTIC),
        IntroTemplate(4, "un", "village de montagne", "baigné par une brume mystérieuse et d'étranges murmures nocturnes", UniverseType.FANTASTIC),
        IntroTemplate(5, "une", "cité fortifiée", "dont les gardes en armure surveillent nerveusement les portes", UniverseType.FANTASTIC),
        IntroTemplate(6, "une", "tour de magie en ruine", "où l'air crépite encore d'énergie arcanique instable", UniverseType.FANTASTIC),
        IntroTemplate(7, "un", "sanctuaire sacré oublié", "gardé par de majestueuses statues de pierre vieilles de plusieurs siècles", UniverseType.FANTASTIC),

        // CONTEMPORARY (7)
        IntroTemplate(8, "un", "entrepôt portuaire", "rempli de conteneurs suspects sous la pluie battante", UniverseType.CONTEMPORARY),
        IntroTemplate(9, "une", "guilde de détectives privés", "où les affaires classées s'accumulent sur les bureaux encombrés", UniverseType.CONTEMPORARY),
        IntroTemplate(10, "une", "taverne clandestine", "située dans le sous-sol d'un vieux club de jazz", UniverseType.CONTEMPORARY),
        IntroTemplate(11, "un", "village côtier", "frappé par la tempête où les insulaires chuchotent sur les derniers naufrages", UniverseType.CONTEMPORARY),
        IntroTemplate(12, "une", "métropole bruyante", "broyée par la pluie et le vacarme incessant de la circulation", UniverseType.CONTEMPORARY),
        IntroTemplate(13, "une", "station de métro désaffectée", "éclairée par le néon vacillant d'un vieux poste de garde", UniverseType.CONTEMPORARY),
        IntroTemplate(14, "un", "manoir abandonné", "dont les couloirs grinçants recèlent de bien sombres secrets", UniverseType.CONTEMPORARY),

        // FUTURISTIC (6)
        IntroTemplate(15, "un", "entrepôt orbital", "abritant des cargaisons de minerais rares et instables", UniverseType.FUTURISTIC),
        IntroTemplate(16, "une", "station spatiale franche", "où se croisent cyber-mercenaires et contrebandiers de la galaxie", UniverseType.FUTURISTIC),
        IntroTemplate(17, "un", "village colonial martien", "protégé par un dôme atmosphérique menaçant de céder", UniverseType.FUTURISTIC),
        IntroTemplate(18, "une", "guilde de chasseurs de primes holographique", "diffusant des contrats en temps réel sur écran géant", UniverseType.FUTURISTIC),
        IntroTemplate(19, "une", "mégalopole sous dôme", "dominée par les tours immenses des méga-corporations", UniverseType.FUTURISTIC),
        IntroTemplate(20, "un", "laboratoire de cybernétique déserté", "où des drones en veille émettent des signaux d'alerte", UniverseType.FUTURISTIC)
    )

    private val standardEventCatalog = listOf(
        StandardEvent("evt_1", "Embuscade sanglante", "Des mercenaires sortent de l'ombre !", EventTag.COMBAT, UniverseType.FANTASTIC, "Charger de face", Skill.COMBAT, "Tirer à distance", Skill.SHOOT, "Négocier la paix", Skill.PERSUASION),
        StandardEvent("evt_2", "Porte verrouillée", "Un lourd vantail en fer bloque le passage.", EventTag.OBSTACLE, UniverseType.FANTASTIC, "Enfoncer la porte", Skill.BREAK, "Crocheter la serrure", Skill.LOCK_TRAP, "Escalader le mur", Skill.CLIMB),
        StandardEvent("evt_3", "Marchand mystérieux", "Un voyageur propose une énigmatique transaction.", EventTag.NPC, UniverseType.FANTASTIC, "Négocier le prix", Skill.PERSUASION, "Déceler le piège", Skill.LOCK_TRAP, "Le menacer", Skill.COMBAT),
        StandardEvent("evt_4", "Fosse à piques", "Le sol se dérobe sous vos pas !", EventTag.TRAP, UniverseType.FANTASTIC, "Sauter par-dessus", Skill.EXTREME_JUMP, "Désamorcer le mécanisme", Skill.LOCK_TRAP, "Utiliser un grappin", Skill.CLIMB),
        StandardEvent("evt_5", "Runes d'Avarice", "Des symboles étincellent en séquence sur le mur.", EventTag.RIDDLE, UniverseType.FANTASTIC, "Déchiffrer la magie", Skill.MAGIC, "Crocheter le panneau", Skill.LOCK_TRAP, "Détruire la stèle", Skill.BREAK)
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

    override suspend fun getRandomIntroTemplate(universe: UniverseType): IntroTemplate = withContext(Dispatchers.IO) {
        dataSource?.getRandomIntroTemplate(universe) ?: introTemplateCatalog.filter { it.universe == universe }.let { list ->
            if (list.isNotEmpty()) list.random() else IntroTemplate(article = "une", place = "taverne", contextText = "vous avez plein d'opportunités", universe = universe)
        }
    }

    override suspend fun getStandardEventByTag(universe: UniverseType, tag: EventTag): StandardEvent = withContext(Dispatchers.IO) {
        dataSource?.getStandardEventByTag(universe, tag) ?: standardEventCatalog.filter { it.tag == tag }.let { list ->
            if (list.isNotEmpty()) list.random() else getRandomStandardEvent(universe)
        }
    }

    override suspend fun getRandomStandardEvent(universe: UniverseType): StandardEvent = withContext(Dispatchers.IO) {
        dataSource?.getRandomStandardEvent(universe) ?: standardEventCatalog.random()
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
