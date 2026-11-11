package com.example.littlerpg.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETTE MATERIAL 3 - AMBIANCE JDR / GRIMOIRE TÉNÉBREUX
// ============================================================================

// --- ARRIÈRE-PLANS & SURFACES (SOMBRES & CUIR ENCRÉ) ---
val RpgBackground = Color(0xFF141316)       // Fond principal d'écran (noir charbon tamisé)
val RpgOnBackground = Color(0xFFE8E3D9)     // Texte principal sur fond (parchemin clair)

val RpgSurface = Color(0xFF1E1C22)          // Surface des cartes, fiches de personnage et dialogues
val RpgOnSurface = Color(0xFFE2DDD3)        // Texte principal sur cartes et fiches

val RpgSurfaceVariant = Color(0xFF2B2832)   // Conteneurs secondaires, zones de texte d'input
val RpgOnSurfaceVariant = Color(0xFFC5BFB4)  // Texte secondaire, étiquettes et instructions douces

// --- BORDURES & SÉPARATEURS ---
val RpgOutline = Color(0xFF5A5462)          // Bordures de cartes, encadrements de fiches
val RpgOutlineVariant = Color(0xFF3E3946)   // Lignes de séparation secondaires

// --- ACTIONS & DÉS : OR VIEILLI / BRONZE ANTIQUE (PRIMARY) ---
val RpgPrimary = Color(0xFFCBB78C)          // Boutons d'action principaux, lancer de dés, bordures dorées
val RpgOnPrimary = Color(0xFF262010)        // Texte sombre à haut contraste sur bouton d'action principal
val RpgPrimaryContainer = Color(0xFF3D3422) // Fond de conteneurs dorés, cartes d'en-tête
val RpgOnPrimaryContainer = Color(0xFFEDE2C8) // Texte clair sur conteneur doré

// --- ATTRIBUTS & MAGIE : BLEU ACIER DÉLAVÉ (SECONDARY) ---
val RpgSecondary = Color(0xFF90A4AE)        // Puces d'attributs, compétences, caractéristiques
val RpgOnSecondary = Color(0xFF1B272C)      // Texte sur boutons ou badges secondaires
val RpgSecondaryContainer = Color(0xFF2D393E) // Fond de puces d'attributs sélectionnées
val RpgOnSecondaryContainer = Color(0xFFD4E1E6) // Texte sur conteneurs bleu acier

// --- INVENTAIRE & AGILITÉ : VERT SAUGE DOUX (TERTIARY) ---
val RpgTertiary = Color(0xFFA5B899)         // Statut positif, bonus d'agilité, objets
val RpgOnTertiary = Color(0xFF202A1A)       // Texte sur éléments vert sauge
val RpgTertiaryContainer = Color(0xFF333E2C)  // Fond de conteneurs ou badges d'inventaire
val RpgOnTertiaryContainer = Color(0xFFDEE9D5) // Texte clair sur conteneurs vert sauge

// --- ÉCHECS & DÉGÂTS : LIE-DE-VIN PÂLE / ROUGE ASSOURDI (ERROR) ---
val RpgError = Color(0xFFE08282)            // Échecs critiques, perte de PV, alertes
val RpgOnError = Color(0xFF3B1212)          // Texte sur zones d'alerte
val RpgErrorContainer = Color(0xFF522222)   // Fond de conteneur d'échec ou d'avertissement
val RpgOnErrorContainer = Color(0xFFF7CECE)  // Texte sur conteneurs d'erreur
