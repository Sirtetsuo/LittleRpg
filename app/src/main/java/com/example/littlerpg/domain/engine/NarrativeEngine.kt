package com.example.littlerpg.domain.engine

import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.ActionResolution
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.UniverseType

class NarrativeEngine {

    fun generateIntro(universe: UniverseType, character: Character): String {
        val name = character.nickname.ifBlank { character.name }
        return when (universe) {
            UniverseType.FANTASTIC -> {
                "Bienvenue, brave $name. Vous vous tenez devant la lourde porte verrouillée d'une ancienne cité oubliée. " +
                        "La brume rampe sur les pavés humides, et un souffle glacé résonne derrière le bois renforcé. " +
                        "Vous avez ${character.hp} PV. Choisissez votre action !"
            }
            UniverseType.CONTEMPORARY -> {
                "Pluie battante sur les vitres. $name, la cible vient d'entrer dans un entrepôt hautement sécurisé. " +
                        "La porte blindée d'accès est fermée et un lecteur de badge clignote en rouge. " +
                        "Santé : ${character.hp} PV. Quelle est votre décision ?"
            }
            UniverseType.FUTURISTIC -> {
                "Secteur 7, Néo-Métropole. Le sas de sécurité de la tour корпоратив bloque votre progression. " +
                        "Des étincelles fusent de la console et des drones de patrouille approchent. " +
                        "Intégrité : ${character.hp} PV. Sélectionnez votre protocole d'action !"
            }
        }
    }

    fun generateActionOptions(
        universe: UniverseType,
        turnCount: Int = 0,
        character: Character? = null
    ): List<ActionOption> {
        val setIndex = turnCount % 3
        val inventory = character?.inventory?.ifEmpty { null } ?: listOf("Une épée rouillée", "Une gourde", "Une potion de soin")
        val selectedItem = inventory[turnCount % inventory.size]
        val itemOption = ActionOption(
            id = 4,
            label = "Utiliser un objet : $selectedItem",
            skill = Skill.MAGIC,
            isItemAction = true,
            itemName = selectedItem
        )

        val baseOptions = when (universe) {
            UniverseType.FANTASTIC -> when (setIndex) {
                0 -> listOf(
                    ActionOption(1, "Défoncer la porte blindée", Skill.BREAK),
                    ActionOption(2, "Crocheter la serrure antique", Skill.LOCK_TRAP),
                    ActionOption(3, "Forcer les gonds en fer", Skill.LIFT_PUSH)
                )
                1 -> listOf(
                    ActionOption(1, "Engager le garde au corps-à-corps", Skill.COMBAT),
                    ActionOption(2, "Tirer une flèche de précision", Skill.SHOOT),
                    ActionOption(3, "Persuader les sentinelles de laisser passer", Skill.PERSUASION)
                )
                else -> listOf(
                    ActionOption(1, "Sauter au-dessus du gouffre", Skill.EXTREME_JUMP),
                    ActionOption(2, "Lancer une boule de feu mystique", Skill.MAGIC),
                    ActionOption(3, "Désamorcer le piège à runes", Skill.LOCK_TRAP)
                )
            }
            UniverseType.CONTEMPORARY -> when (setIndex) {
                0 -> listOf(
                    ActionOption(1, "Neutraliser l'agent de sécurité", Skill.COMBAT),
                    ActionOption(2, "Pirater le boîtier de serrure", Skill.LOCK_TRAP),
                    ActionOption(3, "Négocier fermement avec la cible", Skill.PERSUASION)
                )
                1 -> listOf(
                    ActionOption(1, "Tirer un coup de semonce", Skill.SHOOT),
                    ActionOption(2, "Forcer la grille d'aération", Skill.LIFT_PUSH),
                    ActionOption(3, "Trouver une faille logique", Skill.MAGIC)
                )
                else -> listOf(
                    ActionOption(1, "Sauter par-dessus la barrière", Skill.EXTREME_JUMP),
                    ActionOption(2, "Intimider le garde de nuit", Skill.PERSUASION),
                    ActionOption(3, "Crocheter la porte du sous-sol", Skill.LOCK_TRAP)
                )
            }
            UniverseType.FUTURISTIC -> when (setIndex) {
                0 -> listOf(
                    ActionOption(1, "Tirer au blaster plasma", Skill.SHOOT),
                    ActionOption(2, "Piratage d'accès cybernétique", Skill.LOCK_TRAP),
                    ActionOption(3, "Forcer le sas de décompression", Skill.LIFT_PUSH)
                )
                1 -> listOf(
                    ActionOption(1, "Attaquer la patrouille cyborg", Skill.COMBAT),
                    ActionOption(2, "Franchir la faille gravitationnelle", Skill.EXTREME_JUMP),
                    ActionOption(3, "Escalader la structure de néons", Skill.CLIMB)
                )
                else -> listOf(
                    ActionOption(1, "Casser le champ de force", Skill.BREAK),
                    ActionOption(2, "Sort de surcharge énergétique", Skill.MAGIC),
                    ActionOption(3, "Désactiver les tourelles laser", Skill.LOCK_TRAP)
                )
            }
        }

        return baseOptions + itemOption
    }

    fun generateNarration(
        universe: UniverseType,
        character: Character,
        option: ActionOption,
        resolution: ActionResolution,
        nextTurnCount: Int = 0
    ): String {
        val name = character.nickname.ifBlank { character.name }
        val skillName = resolution.skill.displayName
        val statName = resolution.skill.attribute.displayName

        val header = if (option.isItemAction) {
            "🎒 UTILISATION D'OBJET ! [${option.label}]\n(Objet '${option.itemName}' utilisé avec succès) ➔ L'épreuve est validée !"
        } else if (resolution.isSuccess) {
            "🎲 RÉUSSITE ! [Action : ${option.label}] ($skillName - $statName)\n(Jet: ${resolution.diceRoll} <= Cible: ${resolution.targetValue})"
        } else {
            "🎲 ÉCHEC ! [Action : ${option.label}] ($skillName - $statName)\n(Jet: ${resolution.diceRoll} > Cible: ${resolution.targetValue}) ➔ -1 PV (${resolution.remainingHp}/${character.maxHp} PV restants)"
        }

        val body = if (resolution.isSuccess) {
            val successOutcome = if (option.isItemAction) {
                "$name utilise avec succès '${option.itemName}' pour surmonter l'épreuve !"
            } else {
                getSuccessText(universe, name, option.label, resolution.skill)
            }
            val nextChallenge = getNextChallengeDescription(universe, nextTurnCount)
            "$successOutcome\n\n📍 ÉPREUVE SUIVANTE :\n$nextChallenge"
        } else {
            val failureOutcome = getFailureText(universe, name, option.label, resolution.skill)
            "$failureOutcome\n\n⚠️ L'épreuve est toujours active ! Retentez votre chance ou adaptez votre stratégie."
        }

        val gameOverWarning = if (resolution.remainingHp <= 0) {
            "\n\n☠️ VOS POINTS DE VIE SONT ÉPUISÉS ! C'EST LE GAME OVER !"
        } else ""

        return "$header\n\n$body$gameOverWarning"
    }

    private fun getNextChallengeDescription(universe: UniverseType, turnCount: Int): String {
        val setIndex = turnCount % 3
        return when (universe) {
            UniverseType.FANTASTIC -> when (setIndex) {
                1 -> "L'obstacle cède dans un fracas métallique ! La voie s'ouvre sur une vaste salle d'armes. Deux gardes squelettes en armure rouillée se dressent face à vous, leurs armes levées !"
                2 -> "Vous vous enfoncez plus profondément dans les ténèbres jusqu'à un gouffre béant traversé par un pont de cordes usé. Une herse en bois et en fer bloque l'accès opposé."
                else -> "Devant vous se dresse le sanctuaire sacré du donjon. Un coffre antique scellé par des runes magiques étincelle au centre d'une pièce mystique."
            }
            UniverseType.CONTEMPORARY -> when (setIndex) {
                1 -> "Le périmètre est franchi ! Vous vous glissez dans l'entrepôt sombre. Soudain, un agent de sécurité armé apparaît au bout de la passerelle et braque sa torche vers vous !"
                2 -> "Vous atteignez le secteur haute sécurité du sous-sol. La grille d'aération principale et une porte de secours d'urgence barrent l'accès au serveur."
                else -> "Vous pénétrez dans le bureau de direction. Un coffre-fort biométrique et une vitre blindée renforcée protègent les documents secrets."
            }
            UniverseType.FUTURISTIC -> when (setIndex) {
                1 -> "Le sas pneumatique s'ouvre ! Une patrouille de cyborgs de combat entre en distorsion et verrouille ses canaux d'armement plasma sur votre position !"
                2 -> "Vous arrivez au cœur de la tour корпоратив. Une faille gravitationnelle sépare les passerelles et une structure de néons surplombe le noyau."
                else -> "La baie de contrôle principale s'illumine. Des tourelles laser automatisées et un champ de force impénétrable bloquent la console centrale."
            }
        }
    }

    private fun getSuccessText(universe: UniverseType, name: String, actionLabel: String, skill: Skill): String {
        return when (universe) {
            UniverseType.FANTASTIC -> "Exécution parfaite ! $name réussit '$actionLabel' grâce à sa maîtrise en ${skill.displayName}."
            UniverseType.CONTEMPORARY -> "Succès tactique ! Grâce à sa compétence en ${skill.displayName}, $name accomplit '$actionLabel' sans hésitation."
            UniverseType.FUTURISTIC -> "Protocole validé ! $name réussit '$actionLabel' avec une efficacité parfaite via ${skill.displayName}."
        }
    }

    private fun getFailureText(universe: UniverseType, name: String, actionLabel: String, skill: Skill): String {
        return when (universe) {
            UniverseType.FANTASTIC -> "Échec lors de la tentative de ${skill.displayName}. $name rate '$actionLabel' et encaisse un coup dur (-1 PV)."
            UniverseType.CONTEMPORARY -> "Fausse manœuvre ! L'action '$actionLabel' avec ${skill.displayName} échoue face à la résistance (-1 PV)."
            UniverseType.FUTURISTIC -> "Erreur système lors de '${skill.displayName}'. L'opération '$actionLabel' échoue et cause des dégâts (-1 PV)."
        }
    }
}
