package com.example.littlerpg.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
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

        // Zone d'échange avec le MJ
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

        // Empilement vertical des 4 actions
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

        // Bouton Lancer (70% de la largeur de l'écran, centré)
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
