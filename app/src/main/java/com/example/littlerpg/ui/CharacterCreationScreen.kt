package com.example.littlerpg.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun CharacterCreationScreen(
    onSaveCharacter: (
        name: String,
        nickname: String,
        job: String,
        race: String,
        age: String,
        gender: String,
        particularity: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("M") }
    var particularity by remember { mutableStateOf("Aucun") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val particularitiesList = listOf(
        "Aucun",
        "Phobie des insectes 🕷️",
        "Boiteux (déplacement réduit) 🦵",
        "Tic nerveux incontrôlable 😬",
        "Supersticieux à l'extrême 🔮",
        "Amnésique partiel 🧠",
        "Paranoïaque 👁️",
        "Gourmand compulsif 🍰",
        "Myope sans lunettes 👓",
        "Tic de langage  🗣️"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Création du Personnage",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Nom
        InputFieldRow(label = "Nom :", value = name, onValueChange = { name = it })

        // Surnom
        InputFieldRow(label = "Surnom :", value = nickname, onValueChange = { nickname = it })

        // Métier
        InputFieldRow(label = "Métier :", value = job, onValueChange = { job = it })

        // Race / Origine
        InputFieldRow(label = "Race/origine :", value = race, onValueChange = { race = it })

        // Age
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "age :",
                fontSize = 16.sp,
                modifier = Modifier.width(120.dp)
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp),
                singleLine = true
            )
        }

        // Genre: M / F buttons matching mockup 1
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "genre :",
                fontSize = 16.sp,
                modifier = Modifier.width(120.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GenderButton(label = "M", selected = gender == "M") { gender = "M" }
                GenderButton(label = "F", selected = gender == "F") { gender = "F" }
            }
        }

        // Handicap/tic dropdown matching mockup 1
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Handicap/tic :",
                fontSize = 16.sp,
                modifier = Modifier.width(120.dp)
            )
            Box {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFFB0B0B0), RoundedCornerShape(4.dp))
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = particularity.ifEmpty { "Liste déroulante de choix" },
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    particularitiesList.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                particularity = option
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Valider Button
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onSaveCharacter(name, nickname, job, race, age, gender, particularity)
                }
            },
            enabled = name.isNotBlank() && job.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB0B0B0),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Text(
                text = "Valider",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun InputFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            modifier = Modifier.width(120.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
private fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(50.dp)
            .height(50.dp)
            .background(
                if (selected) Color(0xFF808080) else Color(0xFFB0B0B0),
                RoundedCornerShape(4.dp)
            )
            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterCreationScreenPreview() {
    LittleRpgTheme {
        CharacterCreationScreen(
            onSaveCharacter = { _, _, _, _, _, _, _ -> }
        )
    }
}
