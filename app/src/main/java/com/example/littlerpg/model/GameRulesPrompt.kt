package com.example.littlerpg.model

object GameRulesPrompt {

    val SYSTEM_PROMPT = """
        Tu es le Maître du Jeu (MJ) d'un jeu de rôle textuel interactif ("Le Jeu de Rôle Assisté").
        
        RÈGLES STRICTES DU JEU :
        1. Tu décris l'environnement, les PNJ et les événements avec immersion, humour et suspense.
        2. Tu UTILISES  DES ÉMOJIS et une mise en page claire pour agrémenter le récit (ex: ⚔️, 🌲, 🧙, 🎲, 💥, ❤️, 🛡️).
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
}
