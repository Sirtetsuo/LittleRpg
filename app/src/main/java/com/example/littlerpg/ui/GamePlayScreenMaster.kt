package com.example.littlerpg.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.model.MasterTurn
import com.example.littlerpg.model.Universe
import com.example.littlerpg.ui.theme.LittleRpgTheme
import kotlin.random.Random

@Composable
fun GamePlayScreenMaster(
    universe: Universe?,
    masterHistory: List<MasterTurn>,
    isLoading: Boolean,
    onSubmitDmAction: (dmActionText: String) -> Unit,
    onSaveGame: () -> Unit
) {
    val context = LocalContext.current
    var dmInputText by remember { mutableStateOf("") }
    var showSaveNotification by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showPartySheetDialog by remember { mutableStateOf(false) }
    var showDiceDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(masterHistory.size) {
        if (masterHistory.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(showSaveNotification) {
        if (showSaveNotification) {
            kotlinx.coroutines.delay(2000)
            showSaveNotification = false
        }
    }

    // --- POPUP : RECUPERATION ET EXPORT DE L'AVENTURE / SAUVEGARDE ---
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = {
                Text(
                    text = "💾 Sauvegarde & Export",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A237E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choisissez l'action à réaliser pour votre aventure :",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Sauvegarder : enregistre la partie en local dans l'application.\n• Exporter vers Google Docs : crée directement un fichier texte d'aventure pour Google Docs.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            showSaveConfirmDialog = false
                            onSaveGame()
                            showSaveNotification = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💾 Sauvegarder la partie", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showSaveConfirmDialog = false
                            exportAdventureToGoogleDocs(context, universe, masterHistory)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📄 Créer le fichier Google Docs", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // --- POPUP : RESUME DES INFOS JOUEURS (FICHE DE L'EQUIPE) ---
    if (showPartySheetDialog) {
        MasterPartySheetDialog(
            universe = universe,
            onDismiss = { showPartySheetDialog = false }
        )
    }

    // --- POPUP : JET DE DES ---
    if (showDiceDialog) {
        RollDiceMasterDialog(
            onDismiss = { showDiceDialog = false },
            onConfirmRoll = { diceResultMessage ->
                showDiceDialog = false
                onSubmitDmAction(diceResultMessage)
            }
        )
    }

    // MAIN LAYOUT
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- HEADER BAR ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
            border = BorderStroke(1.dp, Color(0xFF3F51B5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${universe?.emoji ?: "🌍"} Univers : ${universe?.displayName ?: "Inconnu"}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showPartySheetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("📜 Fiche", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showSaveConfirmDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("💾 Sauver", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showSaveNotification) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Partie enregistrée avec succès 💾",
                    modifier = Modifier.padding(8.dp),
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- NARRATIVE LOG AREA (50% SCREEN RATIO, SCROLLABLE) ---
        Box(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .background(Color(0xFFFAFAFA))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (masterHistory.isEmpty()) {
                    Text(
                        text = "L'aventure commence ! Décrivez la situation initiale aux 3 joueurs (Léo, Cathy, Sam).",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    masterHistory.forEach { turn ->
                        MasterTurnDisplay(turn = turn)
                    }
                }

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.height(28.dp).width(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Les 3 joueurs réagissent aux consignes du MJ...",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- DM CONSOLE / INPUT PANEL (30% SCREEN RATIO) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF3F51B5)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(0.35f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✍️ Console du Maître du Jeu :",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )

                    OutlinedButton(
                        onClick = { showDiceDialog = true },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF3F51B5)
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("🎲 Lancer les dés (d100)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = dmInputText,
                    onValueChange = { dmInputText = it },
                    placeholder = { Text("Décrivez le décor, les PNJ, les événements ou demandez des actions aux aventuriers...") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (dmInputText.isNotBlank()) {
                            onSubmitDmAction(dmInputText)
                            dmInputText = ""
                        }
                    },
                    enabled = !isLoading && dmInputText.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Envoyer les consignes du MJ 👑", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun MasterPartySheetDialog(
    universe: Universe?,
    onDismiss: () -> Unit
) {
    val (skillsInfoLeo, skillsInfoCathy, skillsInfoSam, itemsInfo) = when (universe) {
        Universe.SPACE_OPERA -> Quadruple(
            "Maître (70%) : Foudre des doigts (Jedi)\nExpert (60%) : Programmation droïde\nAnnexe (50%) : Piratage\nBase (40%) : Autres compétences",
            "Maître (70%) : Piratage\nExpert (60%) : Connexion faction\nAnnexe (50%) : Déguisement holographique\nBase (40%) : Autres compétences",
            "Maître (70%) : Manipulation mentale (Jedi)\nExpert (60%) : Persuasion\nAnnexe (50%) : Étiquette des milieux\nBase (40%) : Autres compétences",
            "Sabre laser, Pistolet laser, Multipasse de sécurité, Combo jetpack grappin, Droïde de protocole"
        )
        Universe.FANTASTIQUE -> Quadruple(
            "Maître (70%) : Lancer une boule de feu\nExpert (60%) : Connaissance d'une population\nAnnexe (50%) : Transformation magique\nBase (40%) : Autres compétences",
            "Maître (70%) : Effraction de serrure\nExpert (60%) : Combat au corps à corps\nAnnexe (50%) : Furtivité\nBase (40%) : Autres compétences",
            "Maître (70%) : Persuasion\nExpert (60%) : Connaissance d'une population\nAnnexe (50%) : Transformation magique\nBase (40%) : Autres compétences",
            "Ensemble de combat (épée/armure), Bourse de 100 PO, Corde & grappin, Amulette de puissance, Kit de cambriolage"
        )
        Universe.CONTEMPORAIN -> Quadruple(
            "Maître (70%) : Expert histoire et culture\nExpert (60%) : Expert médical\nAnnexe (50%) : Piratage\nBase (40%) : Autres compétences",
            "Maître (70%) : Piratage\nExpert (60%) : Combat (tir)\nAnnexe (50%) : Connaissance de la rue\nBase (40%) : Autres compétences",
            "Maître (70%) : Persuasion\nExpert (60%) : Connaissance de la rue\nAnnexe (50%) : Expert histoire\nBase (40%) : Autres compétences",
            "Pistolet, Armure Kevlar, Console de piratage, Jumelles, Kit de soin, Sérum de vérité"
        )
        else -> Quadruple( // STEAMPUNK
            "Maître (70%) : Bricolage\nExpert (60%) : Invention de gadget\nAnnexe (50%) : Médecine\nBase (40%) : Autres compétences",
            "Maître (70%) : Combat (tir et arme à feu)\nExpert (60%) : Invention de gadget\nAnnexe (50%) : Bricolage\nBase (40%) : Autres compétences",
            "Maître (70%) : Persuasion\nExpert (60%) : Marchandage\nAnnexe (50%) : Médecine\nBase (40%) : Autres compétences",
            "Boîte à outils, Kit de soin, Corde & grappin, Lance-boulon, Épée vapeur, Bourse de 150 PO"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📜 Fiches des Joueurs IA & Compétences",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A237E)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Univers actif : ${universe?.emoji ?: "🌍"} ${universe?.displayName ?: ""}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Léo
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🧠 Léo (Joueur Tactique)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4A148C))
                        Text("• Points de Vie : ❤️ ❤️ ❤️ (3/3 PV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(skillsInfoLeo, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                // Cathy
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("⚔️ Cathy (Joueur Action)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                        Text("• Points de Vie : ❤️ ❤️ ❤️ (3/3 PV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(skillsInfoCathy, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                // Sam
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🎭 Sam (Joueur Social)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                        Text("• Points de Vie : ❤️ ❤️ ❤️ (3/3 PV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(skillsInfoSam, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("🎒 Objets attribuables dans cet univers :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(itemsInfo, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                Text(
                    text = "📌 Règles du d100 : Jet <= Seuil % ➔ SUCCÈS (Adversaire KO). Jet > Seuil % ➔ ÉCHEC (-1 PV).",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

private data class Quadruple<A, B, C, D>(val val1: A, val val2: B, val val3: C, val val4: D)

private fun exportAdventureToGoogleDocs(
    context: Context,
    universe: Universe?,
    masterHistory: List<MasterTurn>
) {
    val transcript = buildString {
        appendLine("=== AVENTURE LITTLE RPG - MODE MAÎTRE DU JEU ===")
        appendLine("Univers : ${universe?.displayName ?: "Inconnu"}")
        appendLine("Date : ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        appendLine("===============================================\n")

        if (masterHistory.isEmpty()) {
            appendLine("Aucun historique d'aventure enregistrée.")
        } else {
            masterHistory.forEach { turn ->
                if (turn.dmPrompt.isNotBlank()) {
                    appendLine("👑 MAÎTRE DU JEU :")
                    appendLine(turn.dmPrompt)
                    appendLine()
                }
                appendLine("🎭 JOUEURS IA (Léo, Cathy, Sam) :")
                appendLine(turn.aiNarrative)
                appendLine("\n-----------------------------------------------\n")
            }
        }
    }

    // Direct targeted Intent to Google Docs app
    val docsIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, transcript)
        putExtra(Intent.EXTRA_SUBJECT, "Aventure Little RPG - ${universe?.displayName ?: ""}")
        type = "text/plain"
        `package` = "com.google.android.apps.docs.editors.docs"
    }

    try {
        context.startActivity(docsIntent)
    } catch (_: Exception) {
        // Fallback: Google Drive app target or Google Drive Upload / Docs Chooser
        val driveIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, transcript)
            putExtra(Intent.EXTRA_SUBJECT, "Aventure Little RPG - ${universe?.displayName ?: ""}")
            type = "text/plain"
            `package` = "com.google.android.apps.docs"
        }

        try {
            context.startActivity(driveIntent)
        } catch (_: Exception) {
            // General text intent chooser fallback
            val chooserIntent = Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, transcript)
                    type = "text/plain"
                },
                "Ouvrir dans Google Docs / Drive"
            )
            try {
                context.startActivity(chooserIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Impossible d'ouvrir Google Docs.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun RollDiceMasterDialog(
    onDismiss: () -> Unit,
    onConfirmRoll: (diceResultMessage: String) -> Unit
) {
    var selectedPlayer by remember { mutableStateOf("Léo") }
    var skillName by remember { mutableStateOf("Piratage") }
    var targetThreshold by remember { mutableIntStateOf(60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎲 Jet de dé a 100 faces (d100)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A237E)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sélectionnez le joueur ou le personnage :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("🧠 Léo", "⚔️ Cathy", "🎭 Sam").forEach { p ->
                        FilterChip(
                            selected = selectedPlayer.contains(p.takeLast(4)),
                            onClick = { selectedPlayer = p },
                            label = { Text(p, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = skillName,
                    onValueChange = { skillName = it },
                    label = { Text("Nom de la compétence testée") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Seuil de compétence (% max pour réussir) :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val thresholds = listOf(70 to "Maître (70%)", 60 to "Expert (60%)", 50 to "Annexe (50%)", 40 to "Base (40%)")
                    thresholds.forEach { (valeur, _) ->
                        FilterChip(
                            selected = targetThreshold == valeur,
                            onClick = { targetThreshold = valeur },
                            label = { Text("$valeur%", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rollValue = Random.nextInt(1, 101)
                    val isSuccess = rollValue <= targetThreshold
                    val resultMessage = """
                        🎲 [JET DE DÉ d100] pour $selectedPlayer sur "$skillName"
                        • Seuil de compétence : $targetThreshold%
                        • Résultat du d100 : $rollValue
                        ➔ Résultat : ${if (isSuccess) "🎉 SUCCÈS ! (Action réussie)" else "💥 ÉCHEC ! (Blessure subie ou action ratée)"}
                    """.trimIndent()
                    onConfirmRoll(resultMessage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text("Lancer le d100 🎲", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun MasterTurnDisplay(turn: MasterTurn) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (turn.dmPrompt.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEDE7F6))
                    .border(1.dp, Color(0xFFB388FF), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "👑 Intervention du Maître du Jeu :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF512DA8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = turn.dmPrompt,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "🎭 Réactions des Joueurs (Léo, Cathy, Sam) :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = turn.aiNarrative,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GamePlayScreenMasterPreview() {
    LittleRpgTheme {
        GamePlayScreenMaster(
            universe = Universe.FANTASTIQUE,
            masterHistory = listOf(
                MasterTurn(
                    turnId = 1,
                    dmPrompt = "Vous vous tenez devant une grande porte mystérieuse emmurée de glyphes luminescents.",
                    aiNarrative = "🧠 Léo : \"Examinons les glyphes, cela semble être un code magique ancient.\"\n⚔️ Cathy : \"Pas le temps de lire, je teste mon crochet de serrure !\"\n🎭 Sam : \"Et si on frappait poliment en demandant qui est là ?\"",
                    partyStatus = emptyList()
                )
            ),
            isLoading = false,
            onSubmitDmAction = {},
            onSaveGame = {}
        )
    }
}
