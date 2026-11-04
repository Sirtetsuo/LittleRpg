package com.example.littlerpg.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.util.executeD100Roll

@Composable
fun RollDiceMasterDialog(
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
                    val rollResult = executeD100Roll(selectedPlayer, skillName, targetThreshold)
                    onConfirmRoll(rollResult.toChatMessage())
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
