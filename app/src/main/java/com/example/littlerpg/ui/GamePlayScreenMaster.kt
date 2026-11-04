package com.example.littlerpg.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.model.MasterTurn
import com.example.littlerpg.model.Universe
import com.example.littlerpg.ui.components.MasterConsoleSection
import com.example.littlerpg.ui.components.MasterHeaderBar
import com.example.littlerpg.ui.components.MasterNarrativeLog
import com.example.littlerpg.ui.dialog.MasterPartySheetDialog
import com.example.littlerpg.ui.dialog.MasterSaveExportDialog
import com.example.littlerpg.ui.dialog.RollDiceMasterDialog
import com.example.littlerpg.ui.theme.LittleRpgTheme
import com.example.littlerpg.util.exportMasterAdventureToGoogleDocs
import kotlinx.coroutines.delay

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
            delay(2000L)
            showSaveNotification = false
        }
    }

    // --- POPUPS ---
    if (showSaveConfirmDialog) {
        MasterSaveExportDialog(
            onDismiss = { showSaveConfirmDialog = false },
            onSaveGame = {
                onSaveGame()
                showSaveNotification = true
            },
            onExportGoogleDocs = {
                context.exportMasterAdventureToGoogleDocs(universe, masterHistory)
            }
        )
    }

    if (showPartySheetDialog) {
        MasterPartySheetDialog(
            universe = universe,
            onDismiss = { showPartySheetDialog = false }
        )
    }

    if (showDiceDialog) {
        RollDiceMasterDialog(
            onDismiss = { showDiceDialog = false },
            onConfirmRoll = { diceResultMessage ->
                showDiceDialog = false
                onSubmitDmAction(diceResultMessage)
            }
        )
    }

    // --- LAYOUT PRINCIPAL ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MasterHeaderBar(
            universe = universe,
            onOpenPartySheet = { showPartySheetDialog = true },
            onOpenSaveDialog = { showSaveConfirmDialog = true }
        )

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

        MasterNarrativeLog(
            masterHistory = masterHistory,
            isLoading = isLoading,
            scrollState = scrollState,
            modifier = Modifier.weight(0.55f)
        )

        MasterConsoleSection(
            inputText = dmInputText,
            onInputTextChange = { dmInputText = it },
            isLoading = isLoading,
            onOpenDiceDialog = { showDiceDialog = true },
            onSubmitAction = {
                if (dmInputText.isNotBlank()) {
                    onSubmitDmAction(dmInputText)
                    dmInputText = ""
                }
            },
            modifier = Modifier.weight(0.35f)
        )
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
