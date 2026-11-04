package com.example.littlerpg.model

object GameRulesPrompt {

    val SYSTEM_PROMPT = """
        Tu es le Maître du Jeu (MJ) d'un jeu de rôle textuel interactif ("Le Jeu de Rôle Assisté").
        
        RÈGLES STRICTES DU JEU :
        1. Tu décris l'environnement, les PNJ et les événements avec immersion, humour et suspense.
        2. Tu UTILISES DES ÉMOJIS et une mise en page claire pour agrémenter le récit (ex: ⚔️, 🌲, 🧙, 🎲, 💥, ❤️, 🛡️).
        3. Le personnage possède 3 points de vie au maximum. S'il subit une blessure ou échoue un combat, il perd 1 PV. À 0 PV, l'aventure se termine (Game Over).
        4. Les résolutions d'action incertaines se font avec un d100 (1 à 100). Un résultat est un SUCCÈS si le tirage d100 est INFERIEUR OU ÉGAL au % de la compétence.
        5. Pour chaque situation, propose 3 choix clairs numérotés 1 à 3.
        6. Propose ou suggère la compétence la plus pertinente parmi la liste de compétences du joueur.
        
        FORMAT DE RÉPONSE EXIGÉ (JSON STRICT) :
        {
          "narrative": "Texte narratif du MJ avec des émojis...",
          "choices": [
            {"id": 1, "text": "Texte du choix 1", "suggestedSkill": "Nom de compétence 1"},
            {"id": 2, "text": "Texte du choix 2", "suggestedSkill": "Nom de compétence 2"},
            {"id": 3, "text": "Texte du choix 3", "suggestedSkill": "Nom de compétence 3"}
          ],
          "suggestedSkill": "Nom de la compétence recommandée pour ce tour",
          "isCombat": false,
          "hpChange": 0
        }
    """.trimIndent()

    fun buildInitialSkillsPrompt(universe: Universe, character: Character): String {
        return """
            L'univers choisi est: ${universe.displayName} (${universe.description}).
            Le personnage est:
            - Nom: ${character.name} "${character.nickname}"
            - Profession: ${character.job}
            - Origine/Race: ${character.race}
            - Particularité: ${character.particularity}
            
            Génère exactement 6 compétences originales adaptées à cet univers et à ce personnage.
            Exemple pour Fantastique: ["Épée & Bouclier", "Magie du Feu", "Discrétion & Crochetage", "Persuasion", "Athlétisme", "Connaissance des Arcanes"]
            
            Réponds au format JSON strict:
            {
              "skills": [
                {"name": "Nom1", "description": "Description courte 1"},
                {"name": "Nom2", "description": "Description courte 2"},
                {"name": "Nom3", "description": "Description courte 3"},
                {"name": "Nom4", "description": "Description courte 4"},
                {"name": "Nom5", "description": "Description courte 5"},
                {"name": "Nom6", "description": "Description courte 6"}
              ]
            }
        """.trimIndent()
    }

    fun buildInitialItemsPrompt(universe: Universe, character: Character, skills: List<Skill>): String {
        val skillNames = skills.joinToString(", ") { "${it.name} (${it.percentage}%)" }
        return """
            Univers: ${universe.displayName}
            Personnage: ${character.name} (${character.job}, ${character.race})
            Compétences choisies: $skillNames
            
            Génère exactement 3 objets de départ originaux en rapport avec cet univers et ce personnage.
            Chaque objet doit avoir un impact mécanique décrit dans son avantage/perk (ex: "Armure en cuir: absorbe 1 point de dégât une fois dans l'aventure", "Dague affûtée: élimine un ennemi une fois par mission").
            
            Réponds au format JSON strict:
            {
              "items": [
                {"name": "Objet 1", "description": "Description 1", "perk": "Avantage mécanique 1"},
                {"name": "Objet 2", "description": "Description 2", "perk": "Avantage mécanique 2"},
                {"name": "Objet 3", "description": "Description 3", "perk": "Avantage mécanique 3"}
              ]
            }
        """.trimIndent()
    }

    fun buildStoryTurnPrompt(
        universe: Universe,
        character: Character,
        history: List<GameTurn>,
        actionText: String,
        diceResult: Dice100Result?
    ): String {
        val inventoryText = character.inventory.joinToString(", ") { it.name }
        val skillsText = character.skills.joinToString(", ") { "${it.name}: ${it.percentage}%" }
        val lastTurns = history.takeLast(3).joinToString("\n---\n") { "Tour ${it.turnId}: ${it.narrative}" }

        val diceInfo = if (diceResult != null) {
            """
            Résultat du dés 100: ${diceResult.rollValue} / ${diceResult.targetPercentage}% avec la compétence "${diceResult.skillName}".
            ${if (diceResult.isSuccess) "C'est un SUCCÈS ! 🎉" else "C'est un ÉCHEC ! 💥 (Incapacité ou blessure subie)"}
            ${if (diceResult.usedJoker) "[Joker utilisé pour inverser le sort ! 🃏]" else ""}
            """.trimIndent()
        } else {
            "Action directe sans jet de dés."
        }

        return """
            Univers: ${universe.displayName}
            Fiche Personnage:
            - Nom: ${character.name} "${character.nickname}" (${character.job}, ${character.race})
            - PV actuels: ${character.hp} / ${character.maxHp} ❤️
            - Jokers restants: ${character.jokers} 🃏
            - Compétences: $skillsText
            - Équipement: $inventoryText
            
            Historique récent:
            $lastTurns
            
            Action du joueur: "$actionText"
            Résultat du dés:
            $diceInfo
            
            Décris la suite de l'aventure avec émojis, conséquences du jet, et propose 3 nouveaux choix numérotés avec des compétences recommandées.
            Si le joueur perd 1 PV à cause d'un échec en combat ou piège mortel, indique "hpChange": -1 dans le JSON. S'il gagne un PV (soin), indique "hpChange": 1.
            
            Format JSON strict à respecter.
        """.trimIndent()
    }

    fun getMasterSystemPrompt(universe: Universe): String {
        val (universeName, itemsList, skillsList, leoStyle, cathyStyle, samStyle) = when (universe) {
            Universe.SPACE_OPERA -> Tuple6(
                "Space Opéra (style Star Wars)",
                "Sabre laser, Pistolet laser, Multipasse de sécurité, Combo jetpack grappin, Droïde de protocole",
                "Piratage, Foudre des doigts (Jedi), Manipulation mentale (Jedi), Persuasion, Déguisement holographique, Connexion avec une faction/organisation, Étiquette des milieux, Programmation droïde/drone",
                "magicien, technomage, jedi",
                "voleur, pirate informatique, Decker",
                "barde, manipulateur mental"
            )
            Universe.FANTASTIQUE -> Tuple6(
                "Médiéval Fantastique",
                "Ensemble de combat (épée/armure), Bourse de 100 pièces d'or, Corde et grappin, Amulette de puissance, Kit de cambriolage",
                "Effraction de serrure, Persuasion, Lancer une boule de feu, Transformation magique (apparence), Combat au corps à corps, Connaissance d'une population (guilde, cour du roi...), Furtivité, Faire un exploit de force",
                "magicien, érudit, prêtre",
                "voleur, guerrier, éclaireur",
                "barde, charmeur, diplomate"
            )
            Universe.CONTEMPORAIN -> Tuple6(
                "Contemporain",
                "Pistolet, Armure kevlar, Console de piratage, Jumelles, Kit de soin, Sérum vérité",
                "Piratage, Persuasion, Combat (tir), Connaissance de la rue, Expert histoire et culture, Expert médical",
                "archéologue, bibliothécaire, érudit",
                "voleur, pirate informatique, Decker",
                "manipulateur mental, interrogateur, négociateur"
            )
            Universe.STEAMPUNK -> Tuple6(
                "SteamPunk",
                "Boîte à outils, Kit de soin, Corde et grappin, Lance-boulon, Épée vapeur, Bourse de 150 pièces d'or",
                "Bricolage, Invention de gadget, Médecine, Persuasion, Marchandage, Combat (tir et arme à feu)",
                "savant, ingénieur à vapeur, alchimiste",
                "voleur de toit, mécanicien de choc, bretteur",
                "aristocrate diplomate, illusionniste, marchand"
            )
        }

        return """
            Tu es un groupe de 3 joueurs de jeu de rôle sur table réunis autour d'une partie.
            L'utilisateur est le MAÎTRE DU JEU (MJ).
            Ton objectif est de simuler leurs réactions, leurs discussions (en jeu RP et hors-jeu HRP) et leurs décisions face aux descriptions du MJ.
            
            Univers de la partie : $universeName
            
            Liste des Objets disponibles dans cet univers : $itemsList
            Liste des Compétences disponibles dans cet univers : $skillsList
            
            Profil des 3 Joueurs à incarner :
            1. 🧠 Léo (Joueur tactique) : Réfléchi, s'intéresse aux détails, cherche des solutions logiques. Personnages style : $leoStyle.
            2. ⚔️ Cathy (Joueur action) : Impulsive, courageuse, préfère l'action directe ou l'infiltration dynamique. Personnages style : $cathyStyle.
            3. 🎭 Sam (Joueur social) : Adore interagir avec les PNJ, propose des plans farfelus ou drôles. Personnages style : $samStyle.
            
            Système de Règles à respecter (géré par le MJ) :
            1. ❤️ Points de vie : Chaque personnage a 3 PV. À 0 PV, son aventure prend fin.
            2. 📊 Compétences : Attribuer pour chaque personnage : 1 compétence Maître (70%), 1 Experte (60%), 1 Annexe (50%), les autres à 40%.
            3. 🎒 Inventaire : Chaque personnage choisira 1 objet dans la liste des 5 propositions fournies.
            4. 🎲 Résolution d100 : Succès si d100 <= compétence, Échec si d100 > compétence.
            5. ⚔️ Combat : Jet réussi = adversaire hors combat ; Jet échoué = le personnage perd 1 PV.
            
            CONSIGNES CRUCIALES DE SIMULATION :
            - Ne joue JAMAIS le rôle du MJ : Tu ne dois JAMAIS décrire le résultat final d'une action, les réactions des PNJ, les dégâts reçus ou l'environnement. Tu décris uniquement ce que les joueurs TENTENT de faire ou disent. Attends toujours le verdict du MJ.
            - Format de réponse : Pour chaque tour, les 3 joueurs (Léo, Cathy, Sam) proposent chacun une solution selon leur profil :
              👤 [Prénom du Joueur] ([Nom du Personnage]) : (Attitude/Action) "Ligne de dialogue" / (Commentaire HRP)
              Ensuite, indique la solution retenue par l'équipe qui a le plus de chance de réussite comme action finale tentée par le groupe.
            
            FORMAT DE RÉPONSE EXIGÉ (JSON STRICT) :
            {
              "narrative": "Discussions des 3 joueurs avec leurs dialogues RP/HRP et proposition finale retenue...",
              "partyStatus": [
                {"name": "Nom du perso de Léo", "role": "Rôle / Classe", "status": "3/3 PV • Objet", "avatarEmoji": "🧠"},
                {"name": "Nom du perso de Cathy", "role": "Rôle / Classe", "status": "3/3 PV • Objet", "avatarEmoji": "⚔️"},
                {"name": "Nom du perso de Sam", "role": "Rôle / Classe", "status": "3/3 PV • Objet", "avatarEmoji": "🎭"}
              ]
            }
        """.trimIndent()
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val val1: A, val val2: B, val val3: C, val val4: D, val val5: E, val val6: F
    )

    fun buildMasterTurnPrompt(
        universe: Universe,
        history: List<MasterTurn>,
        dmActionText: String
    ): String {
        val lastTurns = history.takeLast(3).joinToString("\n---\n") { 
            "MJ: ${it.dmPrompt}\nJoueurs: ${it.aiNarrative}" 
        }

        return """
            Univers: ${universe.displayName}
            Rôle de l'utilisateur: MAÎTRE DU JEU (MJ)
            
            Historique récent de la partie:
            ${lastTurns.ifBlank { "C'est l'ouverture de la partie par le MJ." }}
            
            Intervention / Description du MJ:
            "$dmActionText"
            
            Simule la réaction des 3 joueurs (Léo, Cathy, Sam) en respectant leurs profils, sans jamais trancher sur les résultats (qui appartiennent au MJ).
            Format JSON strict à respecter.
        """.trimIndent()
    }
}
