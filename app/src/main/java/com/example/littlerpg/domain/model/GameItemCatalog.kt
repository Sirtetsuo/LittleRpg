package com.example.littlerpg.domain.model

/**
 * Catalogue complet des 42 objets calibrés du jeu répartis sur les 3 univers.
 */
object GameItemCatalog {

    // 1. Univers Fantastique (id: 1..14)
    val FANTASTIC_ITEMS = listOf(
        GameItem(1, "Potion de soin mineure", "+1d6 PV", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(2, "Élixir de vitalité", "+10 PV", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(3, "Ration de voyage", "+4 PV", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(4, "Philtre de force d'ogre", "+3 FOR (1 tour)", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(5, "Parchemin de clairvoyance", "+3 INT (1 tour)", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(6, "Huile de célérité", "+3 AGI (1 tour)", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(7, "Bélier portable", "AUTO_FOR", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(8, "Grimoire de traduction", "AUTO_INT", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(9, "Passe-partout en mithril", "AUTO_AGI", isConsumable = true, universe = UniverseType.FANTASTIC),
        GameItem(10, "Épée longue", "+2 FOR", isConsumable = false, universe = UniverseType.FANTASTIC),
        GameItem(11, "Bâton d'érudit", "+2 INT", isConsumable = false, universe = UniverseType.FANTASTIC),
        GameItem(12, "Bottes elfiques", "+2 AGI", isConsumable = false, universe = UniverseType.FANTASTIC),
        GameItem(13, "Anneau de régénération", "+1 PV", isConsumable = false, universe = UniverseType.FANTASTIC),
        GameItem(14, "Amulette d'équilibre", "+1 FOR, +1 INT, +1 AGI", isConsumable = false, universe = UniverseType.FANTASTIC)
    )

    // 2. Univers Contemporain (id: 101..114)
    val CONTEMPORARY_ITEMS = listOf(
        GameItem(101, "Pansement compressif", "+1d6 PV", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(102, "Trousse de secours", "+10 PV", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(103, "Barre énergétique", "+4 PV", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(104, "Seringue d'adrénaline", "+3 FOR (1 tour)", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(105, "Nootropique de synthèse", "+3 INT (1 tour)", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(106, "Boisson isotonique", "+3 AGI (1 tour)", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(107, "Pied-de-biche en acier", "AUTO_FOR", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(108, "Clé USB de décryptage", "AUTO_INT", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(109, "Kit de crochetage pro", "AUTO_AGI", isConsumable = true, universe = UniverseType.CONTEMPORARY),
        GameItem(110, "Poing américain", "+2 FOR", isConsumable = false, universe = UniverseType.CONTEMPORARY),
        GameItem(111, "Smartphone tactique", "+2 INT", isConsumable = false, universe = UniverseType.CONTEMPORARY),
        GameItem(112, "Chaussures d'intervention", "+2 AGI", isConsumable = false, universe = UniverseType.CONTEMPORARY),
        GameItem(113, "Gilet pare-balles", "+2 PV", isConsumable = false, universe = UniverseType.CONTEMPORARY),
        GameItem(114, "Montre connectée", "+1 FOR, +1 INT, +1 AGI", isConsumable = false, universe = UniverseType.CONTEMPORARY)
    )

    // 3. Univers Futuriste (id: 201..214)
    val FUTURISTIC_ITEMS = listOf(
        GameItem(201, "Gel cicatrisant", "+1d6 PV", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(202, "Nano-injecteur médical", "+10 PV", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(203, "Capsule de nutriments", "+4 PV", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(204, "Stimulant musculaire", "+3 FOR (1 tour)", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(205, "Puce neurale temporaire", "+3 INT (1 tour)", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(206, "Accélérateur synaptique", "+3 AGI (1 tour)", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(207, "Charge à plasma", "AUTO_FOR", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(208, "IA de piratage jetable", "AUTO_INT", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(209, "Grappin magnétique", "AUTO_AGI", isConsumable = true, universe = UniverseType.FUTURISTIC),
        GameItem(210, "Gantelet énergétique", "+2 FOR", isConsumable = false, universe = UniverseType.FUTURISTIC),
        GameItem(211, "Implant oculaire", "+2 INT", isConsumable = false, universe = UniverseType.FUTURISTIC),
        GameItem(212, "Propulseurs de saut", "+2 AGI", isConsumable = false, universe = UniverseType.FUTURISTIC),
        GameItem(213, "Bouclier déflecteur", "+2 PV", isConsumable = false, universe = UniverseType.FUTURISTIC),
        GameItem(214, "Exosquelette léger", "+1 FOR, +1 INT, +1 AGI", isConsumable = false, universe = UniverseType.FUTURISTIC)
    )

    val ALL_ITEMS: List<GameItem> = FANTASTIC_ITEMS + CONTEMPORARY_ITEMS + FUTURISTIC_ITEMS

    fun getItemByName(name: String): GameItem? {
        val trimmed = name.trim()
        return ALL_ITEMS.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
            ?: ALL_ITEMS.firstOrNull { trimmed.contains(it.name, ignoreCase = true) || it.name.contains(trimmed, ignoreCase = true) }
    }

    fun getItemsForUniverse(universe: UniverseType): List<GameItem> {
        return when (universe) {
            UniverseType.FANTASTIC -> FANTASTIC_ITEMS
            UniverseType.CONTEMPORARY -> CONTEMPORARY_ITEMS
            UniverseType.FUTURISTIC -> FUTURISTIC_ITEMS
        }
    }
}
