package com.example.littlerpg.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.model.Universe
import com.example.littlerpg.model.getMasterPartyInfo

@Composable
fun MasterPartySheetDialog(
    universe: Universe?,
    onDismiss: () -> Unit
) {
    val partyInfo = universe.getMasterPartyInfo()

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
                        Text(partyInfo.leoSkills, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                // Cathy
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("⚔️ Cathy (Joueur Action)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                        Text("• Points de Vie : ❤️ ❤️ ❤️ (3/3 PV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(partyInfo.cathySkills, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                // Sam
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🎭 Sam (Joueur Social)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                        Text("• Points de Vie : ❤️ ❤️ ❤️ (3/3 PV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(partyInfo.samSkills, fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("🎒 Objets attribuables dans cet univers :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(partyInfo.items, fontSize = 11.sp, color = Color.DarkGray)
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
