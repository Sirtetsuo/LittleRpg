package com.example.littlerpg.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.littlerpg.model.Character
import com.example.littlerpg.model.ChoiceOption
import com.example.littlerpg.model.Dice100Result
import com.example.littlerpg.model.GameTurn
import com.example.littlerpg.model.Item
import com.example.littlerpg.model.Skill
import com.example.littlerpg.model.SkillTier
import com.example.littlerpg.model.Universe
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun GamePlayScreen(
    character: Character,
    storyHistory: List<GameTurn>,
    currentTurn: GameTurn?,
    lastDiceResult: Dice100Result?,
    pendingJokerAction: com.example.littlerpg.model.PendingJokerAction?,
    isLoading: Boolean,
    onSelectChoice: (ChoiceOption) -> Unit,
    onSubmitCustomAction: (actionText: String, diceResult: Dice100Result?) -> Unit,
    onRollDice: (skillName: String) -> Dice100Result,
    onResolveJoker: (useJoker: Boolean) -> Unit,
    onSaveGame: () -> Unit,
    universe: Universe? = null
) {
    var customActionInput by remember { mutableStateOf("") }
    var showCharacterSheetDialog by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(storyHistory.size) {
        if (storyHistory.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(showSaveConfirmation) {
        if (showSaveConfirmation) {
            kotlinx.coroutines.delay(2000)
            showSaveConfirmation = false
        }
    }

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = {
                Text(
                    text = "💾 Confirmation de sauvegarde",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = "Voulez-vous sauvegarder votre partie Solo ?\nAttention : Toute sauvegarde existante (Solo ou Master) sera remplacée.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirmDialog = false
                        onSaveGame()
                        showSaveConfirmation = true
                    }
                ) {
                    Text("Oui, sauvegarder 💾", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- HEADER BAR: Character Stats ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
            border = BorderStroke(1.dp, Color.Gray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${character.name} (${character.job})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "PV: ${"❤️ ".repeat(character.hp)} | Jokers: ${"🃏 ".repeat(character.jokers)}",
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showCharacterSheetDialog = true },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(36.dp).weight(1f)
                    ) {
                        Text("📜 Fiche", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (showSaveConfirmation) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).height(36.dp)) {
                            Text(
                                text = "Sauvegardé 💾",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                showSaveConfirmDialog = true
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(36.dp).weight(1f)
                        ) {
                            Text("💾 Sauver", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- ZONE D'ECHANGE DE L'IA (Story Box - ~1/3 screen height, accumulating history) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFFC0C0C0), RoundedCornerShape(4.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(10.dp)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = universe?.imageRes ?: com.example.littlerpg.R.drawable.dnd),
                        contentDescription = "Chargement...",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(130.dp)
                            .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Le Maître du Jeu réfléchit...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (storyHistory.isEmpty()) {
                        Text(
                            text = currentTurn?.narrative ?: "Bienvenue dans l'aventure...",
                            fontSize = if (currentTurn?.isError == true) 22.sp else 14.sp,
                            fontWeight = if (currentTurn?.isError == true) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (currentTurn?.isError == true) Color.Red else Color.Black,
                            lineHeight = if (currentTurn?.isError == true) 28.sp else 20.sp,
                            textAlign = if (currentTurn?.isError == true) TextAlign.Center else TextAlign.Start,
                            modifier = if (currentTurn?.isError == true) Modifier.fillMaxWidth().padding(vertical = 16.dp) else Modifier
                        )
                    } else {
                        storyHistory.forEachIndexed { index, turn ->
                            if (index > 0) {
                                Text(
                                    text = "────────────────────────",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (index == 0 && universe != null) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = universe.imageRes),
                                    contentDescription = universe.displayName,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = turn.narrative,
                                fontSize = if (turn.isError) 22.sp else 14.sp,
                                fontWeight = if (turn.isError) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (turn.isError) Color.Red else Color.Black,
                                lineHeight = if (turn.isError) 28.sp else 20.sp,
                                textAlign = if (turn.isError) TextAlign.Center else TextAlign.Start,
                                modifier = if (turn.isError) Modifier.fillMaxWidth().padding(vertical = 16.dp) else Modifier
                            )
                        }
                    }
                }
            }
        }

        // --- ZONE DE TEXTE INPUT POUR DONNER UNE ACTION SPECIFIQUE ---
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFC0C0C0), RoundedCornerShape(4.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customActionInput,
                    onValueChange = { customActionInput = it },
                    placeholder = { Text("taper ici votre action", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        if (customActionInput.isNotBlank()) {
                            val defaultSkill = character.skills.firstOrNull()?.name ?: "Combat"
                            val dice = onRollDice(defaultSkill)
                            onSubmitCustomAction(customActionInput, dice)
                            customActionInput = ""
                        }
                    },
                    enabled = customActionInput.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080))
                ) {
                    Text("Envoyer", fontSize = 12.sp)
                }
            }
        }

        // --- 3 CHOIX BUTTONS (Dynamic size, stacked vertically) ---
        val activeTurn = currentTurn ?: storyHistory.lastOrNull()
        val choices = activeTurn?.choices ?: emptyList()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0 until 3) {
                val choice = choices.getOrNull(i)
                val targetPercentage = if (choice != null) {
                    val skillName = choice.suggestedSkill ?: character.skills.firstOrNull()?.name
                    val matchedSkill = if (skillName != null) {
                        val cleanInput = skillName.replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim().lowercase()
                        character.skills.find { s ->
                            val cleanSkill = s.name.replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim().lowercase()
                            cleanSkill == cleanInput || cleanSkill.contains(cleanInput) || cleanInput.contains(cleanSkill)
                        } ?: character.skills.firstOrNull()
                    } else null
                    matchedSkill?.percentage ?: 40
                } else null

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (targetPercentage != null && !isLoading) {
                        Text(
                            text = "$targetPercentage%",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(48.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    ChoiceButton(
                        label = choice?.text ?: "Option ${i + 1}",
                        enabled = choice != null && !isLoading,
                        onClick = { choice?.let { onSelectChoice(it) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- ZONE DE RÉSULTAT ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val diceText = lastDiceResult?.summary ?: "Zone de résultat"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = diceText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (lastDiceResult?.isSuccess == true) Color(0xFF2E7D32) else if (lastDiceResult?.isSuccess == false) Color.Red else Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (pendingJokerAction != null) {
        AlertDialog(
            onDismissRequest = { /* Require explicit button click */ },
            title = {
                Text(
                    text = "Voulez-vous utiliser un joker ?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            },
            text = {
                Text(
                    text = "Votre action a échoué ( Jet : ${pendingJokerAction.diceResult.rollValue} > ${pendingJokerAction.diceResult.targetPercentage}% ).\n" +
                            "Utiliser un joker forcera la réussite de l'action.\n\n" +
                            "Jokers restants : ${character.jokers} 🃏",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { onResolveJoker(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text("🃏 Joker", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onResolveJoker(false) }) {
                    Text("Annuler", color = Color.Black)
                }
            }
        )
    }

    // Character Sheet Dialog
    if (showCharacterSheetDialog) {
        AlertDialog(
            onDismissRequest = { showCharacterSheetDialog = false },
            title = { Text("📜 Fiche de Personnage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Nom: ${character.name} \"${character.nickname}\"", fontWeight = FontWeight.Bold)
                    Text("• Profession: ${character.job} (${character.race})")
                    Text("• Particularité: ${character.particularity}")
                    Text("• PV: ${character.hp} / ${character.maxHp} ❤️")
                    Text("• Jokers: ${character.jokers} 🃏")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Compétences:", fontWeight = FontWeight.Bold)
                    character.skills.forEach { skill ->
                        Text("  ${skill.tier.emoji} ${skill.name}: ${skill.percentage}% (${skill.tier.label})")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Équipement:", fontWeight = FontWeight.Bold)
                    character.inventory.forEach { item ->
                        Text("  🎒 ${item.name}: ${item.perk}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCharacterSheetDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
private fun ChoiceButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .background(if (enabled) Color(0xFFB0B0B0) else Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.Black else Color.Gray,
            textAlign = TextAlign.Start,
            maxLines = Int.MAX_VALUE
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GamePlayScreenPreview() {
    val sampleCharacter = Character(
        name = "Valeros",
        nickname = "Le Paladin",
        job = "Guerrier",
        race = "Humain",
        age = "30",
        gender = "M",
        particularity = "Cicatrice à l'œil gauche",
        skills = listOf(
            Skill("Épée", "Maniement des armes blanches", 70, SkillTier.MAITRE),
            Skill("Athlétisme", "Force physique et endurance", 60, SkillTier.EXPERT),
            Skill("Perception", "Sens aiguisés", 40, SkillTier.BASE)
        ),
        inventory = listOf(
            Item(name = "Épée longue", description = "Une épée en acier trempé", perk = "+10 combat"),
            Item(name = "Bouclier", description = "Un bouclier en bois renforcé", perk = "+5 défense")
        ),
        hp = 3,
        maxHp = 3,
        jokers = 2
    )

    val sampleTurn = GameTurn(
        turnId = 1,
        narrative = "Vous vous tenez devant les portes sombres du donjon. Le vent souffle fraîchement et une odeur d'humidité s'échappe des profondeurs. Que faites-vous ?",
        choices = listOf(
            ChoiceOption(1, "Ouvrir la porte en bois massif pour entrer dans la grande salle obscure"),
            ChoiceOption(2, "Inspecter minutieusement les environs à la recherche d'un mécanisme caché"),
            ChoiceOption(3, "Chuchoter un sort de détection magique pour identifier les pièges")
        )
    )

    val sampleDiceResult = Dice100Result(
        rollValue = 35,
        skillName = "Épée",
        targetPercentage = 70,
        isSuccess = true,
        summary = "Lancer de d100: 35 / 70% (Épée) - SUCCÈS"
    )

    LittleRpgTheme {
        GamePlayScreen(
            character = sampleCharacter,
            storyHistory = listOf(sampleTurn),
            currentTurn = sampleTurn,
            lastDiceResult = sampleDiceResult,
            pendingJokerAction = null,
            isLoading = false,
            onSelectChoice = {},
            onSubmitCustomAction = { _, _ -> },
            onRollDice = { Dice100Result(50, it, 50, true, false, "Test") },
            onResolveJoker = {},
            onSaveGame = {},
            universe = Universe.FANTASTIQUE
        )
    }
}
