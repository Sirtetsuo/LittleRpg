package com.example.littlerpg.model

data class MasterPartyInfo(
    val leoSkills: String,
    val cathySkills: String,
    val samSkills: String,
    val items: String
)

fun Universe?.getMasterPartyInfo(): MasterPartyInfo = when (this) {
    Universe.SPACE_OPERA -> MasterPartyInfo(
        leoSkills = "Maître (70%) : Foudre des doigts (Jedi)\nExpert (60%) : Programmation droïde\nAnnexe (50%) : Piratage\nBase (40%) : Autres compétences",
        cathySkills = "Maître (70%) : Piratage\nExpert (60%) : Connexion faction\nAnnexe (50%) : Déguisement holographique\nBase (40%) : Autres compétences",
        samSkills = "Maître (70%) : Manipulation mentale (Jedi)\nExpert (60%) : Persuasion\nAnnexe (50%) : Étiquette des milieux\nBase (40%) : Autres compétences",
        items = "Sabre laser, Pistolet laser, Multipasse de sécurité, Combo jetpack grappin, Droïde de protocole"
    )
    Universe.FANTASTIQUE -> MasterPartyInfo(
        leoSkills = "Maître (70%) : Lancer une boule de feu\nExpert (60%) : Connaissance d'une population\nAnnexe (50%) : Transformation magique\nBase (40%) : Autres compétences",
        cathySkills = "Maître (70%) : Effraction de serrure\nExpert (60%) : Combat au corps à corps\nAnnexe (50%) : Furtivité\nBase (40%) : Autres compétences",
        samSkills = "Maître (70%) : Persuasion\nExpert (60%) : Connaissance d'une population\nAnnexe (50%) : Transformation magique\nBase (40%) : Autres compétences",
        items = "Ensemble de combat (épée/armure), Bourse de 100 PO, Corde & grappin, Amulette de puissance, Kit de cambriolage"
    )
    Universe.CONTEMPORAIN -> MasterPartyInfo(
        leoSkills = "Maître (70%) : Expert histoire et culture\nExpert (60%) : Expert médical\nAnnexe (50%) : Piratage\nBase (40%) : Autres compétences",
        cathySkills = "Maître (70%) : Piratage\nExpert (60%) : Combat (tir)\nAnnexe (50%) : Connaissance de la rue\nBase (40%) : Autres compétences",
        samSkills = "Maître (70%) : Persuasion\nExpert (60%) : Connaissance de la rue\nAnnexe (50%) : Expert histoire\nBase (40%) : Autres compétences",
        items = "Pistolet, Armure Kevlar, Console de piratage, Jumelles, Kit de soin, Sérum de vérité"
    )
    else -> MasterPartyInfo( // STEAMPUNK
        leoSkills = "Maître (70%) : Bricolage\nExpert (60%) : Invention de gadget\nAnnexe (50%) : Médecine\nBase (40%) : Autres compétences",
        cathySkills = "Maître (70%) : Combat (tir et arme à feu)\nExpert (60%) : Invention de gadget\nAnnexe (50%) : Bricolage\nBase (40%) : Autres compétences",
        samSkills = "Maître (70%) : Persuasion\nExpert (60%) : Marchandage\nAnnexe (50%) : Médecine\nBase (40%) : Autres compétences",
        items = "Boîte à outils, Kit de soin, Corde & grappin, Lance-boulon, Épée vapeur, Bourse de 150 PO"
    )
}
