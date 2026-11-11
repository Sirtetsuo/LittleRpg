package com.example.littlerpg.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.domain.model.ActionOption
import com.example.littlerpg.domain.model.Character
import com.example.littlerpg.domain.model.ChatMessage
import com.example.littlerpg.domain.model.Skill
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun MainGameScreen(
    universe: UniverseType,
    character: Character,
    messages: List<ChatMessage>,
    currentOptions: List<ActionOption>,
    selectedOption: ActionOption?,
    saveNotificationText: String?,
    showCharacterSheetDialog: Boolean = false,
    onOptionSelect: (ActionOption) -> Unit,
    onExecuteActionClicked: () -> Unit,
    onSaveGameClicked: () -> Unit,
    onOpenCharacterSheet: () -> Unit = {},
    onCloseCharacterSheet: () -> Unit = {},
    onBackToMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showCharacterSheetDialog) {
        CharacterSheetDialog(
            character = character,
            universe = universe,
            onDismiss = onCloseCharacterSheet
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // En-tête Personnage & Statut avec Boutons Fiche, Save et Menu
        CharacterHeaderBar(
            character = character,
            universe = universe,
            onOpenSheet = onOpenCharacterSheet,
            onSave = onSaveGameClicked,
            onBack = onBackToMenuClicked
        )

        saveNotificationText?.let { notification ->
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = notification,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 1. Zone d'échange avec le MJ (40% de la hauteur disponible, texte sélectionnable/copiable)
        SelectionContainer(
            modifier = Modifier
                .weight(0.40f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(message = msg)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Empilement vertical des 4 actions (hauteur adaptée au contenu)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            currentOptions.forEach { option ->
                val isSelected = selectedOption?.id == option.id
                Button(
                    onClick = { onOptionSelect(option) },
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isSelected) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = "${option.label} (${option.skill.displayName})",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Bouton Lancer (70% de la largeur de l'écran, centré)
        Button(
            onClick = onExecuteActionClicked,
            enabled = selectedOption != null,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.70f)
                .align(Alignment.CenterHorizontally)
                .height(50.dp)
        ) {
            Text(
                text = "Lancer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CharacterHeaderBar(
    character: Character,
    universe: UniverseType,
    onOpenSheet: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${character.name} ${if (character.nickname.isNotBlank()) "(${character.nickname})" else ""} - ${universe.displayName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onOpenSheet,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Fiche", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Menu", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (character.hp <= 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "❤️ PV : ${character.hp} / ${character.maxHp}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (character.hp <= 5) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "💪 FOR: ${character.strength}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "🧠 INT: ${character.intelligence}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⚡ AGI: ${character.agility}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    val isGm = message.isFromGameMaster
    val alignment = if (isGm) Alignment.Start else Alignment.End
    val cardColor = if (isGm) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isGm) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Text(
            text = if (isGm) "Maître du Jeu" else "Vous",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainGameScreenPreview() {
    val options = listOf(
        ActionOption(1, "Défoncer la porte blindée", Skill.BREAK),
        ActionOption(2, "Crocheter la serrure antique", Skill.LOCK_TRAP),
        ActionOption(3, "Forcer les gonds en fer", Skill.LIFT_PUSH),
        ActionOption(4, "Invoquer un sort de déverrouillage", Skill.MAGIC)
    )

    LittleRpgTheme {
        MainGameScreen(
            universe = UniverseType.FANTASTIC,
            character = Character("Arthur", "Le Brave", 28, 14, 12, 10, hp = 18, maxHp = 20),
            messages = listOf(
                ChatMessage(isFromGameMaster = true, text = "Bienvenue dans le donjon obscur. Que faites-vous ?"),
                ChatMessage(isFromGameMaster = false, text = "Action choisie : Crocheter la serrure antique")
            ),
            currentOptions = options,
            selectedOption = options[1],
            saveNotificationText = "Partie sauvegardée dans SQLite !",
            onOptionSelect = {},
            onExecuteActionClicked = {},
            onSaveGameClicked = {},
            onBackToMenuClicked = {}
        )
    }
}
