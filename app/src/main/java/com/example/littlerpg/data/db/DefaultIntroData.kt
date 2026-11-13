package com.example.littlerpg.data.db

import com.example.littlerpg.domain.model.UniverseType

object DefaultIntroData {

    fun getIntroTemplates(): List<Pair<Triple<String, String, String>, UniverseType>> {
        return listOf(
            // FANTASTIC (7)
            Triple("une", "taverne bruyante", "où les aventuriers racontent leurs exploits autour de choppes de chouchen") to UniverseType.FANTASTIC,
            Triple("une", "guilde des aventuriers", "dont le tableau des quêtes regorge d'avis de recherche et de contrats urgents") to UniverseType.FANTASTIC,
            Triple("un", "entrepôt abandonné", "reconverti en marché noir où circulent d'étranges reliques") to UniverseType.FANTASTIC,
            Triple("un", "village de montagne", "baigné par une brume mystérieuse et d'étranges murmures nocturnes") to UniverseType.FANTASTIC,
            Triple("une", "cité fortifiée", "dont les gardes en armure surveillent nerveusement les portes") to UniverseType.FANTASTIC,
            Triple("une", "tour de magie en ruine", "où l'air crépite encore d'énergie arcanique instable") to UniverseType.FANTASTIC,
            Triple("un", "sanctuaire sacré oublié", "gardé par de majestueuses statues de pierre vieilles de plusieurs siècles") to UniverseType.FANTASTIC,

            // CONTEMPORARY (7)
            Triple("un", "entrepôt portuaire", "rempli de conteneurs suspects sous la pluie battante") to UniverseType.CONTEMPORARY,
            Triple("une", "guilde de détectives privés", "où les affaires classées s'accumulent sur les bureaux encombrés") to UniverseType.CONTEMPORARY,
            Triple("une", "taverne clandestine", "située dans le sous-sol d'un vieux club de jazz") to UniverseType.CONTEMPORARY,
            Triple("un", "village côcier", "frappé par la tempête où les insulaires chuchotent sur les derniers naufrages") to UniverseType.CONTEMPORARY,
            Triple("une", "métropole bruyante", "broyée par la pluie et le vacarme incessant de la circulation") to UniverseType.CONTEMPORARY,
            Triple("une", "station de métro désaffectée", "éclairée par le néon vacillant d'un vieux poste de garde") to UniverseType.CONTEMPORARY,
            Triple("un", "manoir abandonné", "dont les couloirs grinçants recellent de bien sombres secrets") to UniverseType.CONTEMPORARY,

            // FUTURISTIC (6)
            Triple("un", "entrepôt orbital", "abritant des cargaisons de minerais rares et instables") to UniverseType.FUTURISTIC,
            Triple("une", "station spatiale franche", "où se croisent cyber-mercenaires et contrebandiers de la galaxie") to UniverseType.FUTURISTIC,
            Triple("un", "village colonial martien", "protégé par un dôme atmosphérique menaçant de céder") to UniverseType.FUTURISTIC,
            Triple("une", "guilde de chasseurs de primes holographique", "diffusant des contrats en temps réel sur écran géant") to UniverseType.FUTURISTIC,
            Triple("une", "mégalopole sous dôme", "dominée par les tours immenses des méga-corporations") to UniverseType.FUTURISTIC,
            Triple("un", "laboratoire de cybernétique déserté", "où des drones en veille émettent des signaux d'alerte") to UniverseType.FUTURISTIC
        )
    }
}
