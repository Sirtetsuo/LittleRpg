package com.example.littlerpg.presentation.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.domain.model.UniverseType
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun CharacterCreationScreen(
    selectedUniverse: UniverseType,
    onCharacterCreated: (name: String, nickname: String, age: Int, str: Int, intVal: Int, agi: Int) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("25") }

    var strBonus by remember { mutableIntStateOf(0) }
    var intBonus by remember { mutableIntStateOf(0) }
    var agiBonus by remember { mutableIntStateOf(0) }

    val remainingPoints = TOTAL_POOL - (strBonus + intBonus + agiBonus)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            OutlinedButton(
                onClick = onBackClicked,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← Retour", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Création Personnage",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Univers : ${selectedUniverse.displayName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du personnage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Surnom / Titre (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { char -> char.isDigit() } },
                    label = { Text("Âge") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (remainingPoints > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Points restants : $remainingPoints / $TOTAL_POOL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (remainingPoints < TOTAL_POOL) {
                    TextButton(
                        onClick = {
                            strBonus = 0
                            intBonus = 0
                            agiBonus = 0
                        }
                    ) {
                        Text("Réinitialiser", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Attributs (Base 7 + Répartition)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        AttributeStepper(
            label = "Force",
            bonusValue = strBonus,
            canIncrement = remainingPoints > 0,
            onIncrement = { strBonus++ },
            onDecrement = { strBonus-- }
        )

        AttributeStepper(
            label = "Intelligence",
            bonusValue = intBonus,
            canIncrement = remainingPoints > 0,
            onIncrement = { intBonus++ },
            onDecrement = { intBonus-- }
        )

        AttributeStepper(
            label = "Agilité",
            bonusValue = agiBonus,
            canIncrement = remainingPoints > 0,
            onIncrement = { agiBonus++ },
            onDecrement = { agiBonus-- }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val age = ageText.toIntOrNull() ?: 25
                onCharacterCreated(
                    name.ifBlank { "L'Aventurier" },
                    nickname,
                    age,
                    BASE_STAT + strBonus,
                    BASE_STAT + intBonus,
                    BASE_STAT + agiBonus
                )
            },
            enabled = remainingPoints == 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (remainingPoints > 0) "Attribuez tous les points ($remainingPoints restants)" else "Commencer l'Aventure",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterCreationScreenPreview() {
    LittleRpgTheme {
        CharacterCreationScreen(
            selectedUniverse = UniverseType.FANTASTIC,
            onCharacterCreated = { _, _, _, _, _, _ -> },
            onBackClicked = {}
        )
    }
}
