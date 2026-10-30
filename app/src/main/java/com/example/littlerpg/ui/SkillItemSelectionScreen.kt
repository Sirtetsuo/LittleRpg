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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.model.Item
import com.example.littlerpg.model.Skill
import com.example.littlerpg.model.Universe
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun SkillSelectionStep(
    skills: List<Skill>,
    isLoading: Boolean,
    onConfirmSkills: (maitre: Skill, expert: Skill, annexe: Skill) -> Unit,
    universe: Universe? = null
) {
    var selectedMaitre by remember { mutableStateOf<Skill?>(null) }
    var selectedExpert by remember { mutableStateOf<Skill?>(null) }
    var selectedAnnexe by remember { mutableStateOf<Skill?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Attribution des Compétences",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choisissez parmi les 6 compétences générées par le MJ :\n" +
                    "🥇 Maître (70%) | 🥈 Expert (60%) | 🥉 Annexe (50%)\nLes 3 autres resteront à 40%.",
            fontSize = 13.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = universe?.imageRes ?: com.example.littlerpg.R.drawable.dnd),
                    contentDescription = "Chargement...",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(180.dp)
                        .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("L'IA génère vos compétences...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            skills.forEach { skill ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
                    border = BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = skill.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (skill.description.isNotEmpty()) {
                            Text(
                                text = skill.description,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedMaitre?.name == skill.name,
                                    onClick = {
                                        selectedMaitre = skill
                                        if (selectedExpert?.name == skill.name) selectedExpert = null
                                        if (selectedAnnexe?.name == skill.name) selectedAnnexe = null
                                    }
                                )
                                Text("🥇 70%", fontSize = 12.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedExpert?.name == skill.name,
                                    onClick = {
                                        selectedExpert = skill
                                        if (selectedMaitre?.name == skill.name) selectedMaitre = null
                                        if (selectedAnnexe?.name == skill.name) selectedAnnexe = null
                                    }
                                )
                                Text("🥈 60%", fontSize = 12.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedAnnexe?.name == skill.name,
                                    onClick = {
                                        selectedAnnexe = skill
                                        if (selectedMaitre?.name == skill.name) selectedMaitre = null
                                        if (selectedExpert?.name == skill.name) selectedExpert = null
                                    }
                                )
                                Text("🥉 50%", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val isValid = selectedMaitre != null && selectedExpert != null && selectedAnnexe != null

            Button(
                onClick = {
                    if (isValid) {
                        onConfirmSkills(selectedMaitre!!, selectedExpert!!, selectedAnnexe!!)
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Valider les compétences", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ItemSelectionStep(
    items: List<Item>,
    isLoading: Boolean,
    onSelectItem: (Item) -> Unit,
    universe: Universe? = null
) {
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Inventaire de Départ",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choisissez 1 objet de départ proposé par le MJ :\n📌 Vous disposez aussi de 2 Jokers (🃏 🃏) pour la mission.",
            fontSize = 13.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = universe?.imageRes ?: com.example.littlerpg.R.drawable.dnd),
                    contentDescription = "Chargement...",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(180.dp)
                        .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("L'IA génère votre équipement...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            items.forEach { item ->
                val isSelected = selectedItem?.id == item.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { selectedItem = item }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        if (item.perk.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✨ Avantage: ${item.perk}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedItem?.let { onSelectItem(it) }
                },
                enabled = selectedItem != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Lancer l'Aventure ! 🚀", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkillSelectionStepPreview() {
    LittleRpgTheme {
        SkillSelectionStep(
            skills = listOf(
                Skill("Combat à l'Épée ⚔️", "Maîtrise des armes blanches et du corps-à-corps"),
                Skill("Magie & Sortilèges 🔮", "Incantation de sorts profanes ou divins"),
                Skill("Discrétion & Crochetage 🗡️", "Se déplacer en silence et ouvrir les verrous"),
                Skill("Persuasion & Intimidation 🗣️", "Convaincre ou effrayer les PNJ"),
                Skill("Athlétisme & Escalade 🏃‍♂️", "Franchir les obstacles et sauter les ravins"),
                Skill("Connaissance des Arcanes 📜", "Identifier les glyphes, objets magiques et légendes")
            ),
            isLoading = false,
            onConfirmSkills = { _, _, _ -> },
            universe = Universe.FANTASTIQUE
        )
    }
}
