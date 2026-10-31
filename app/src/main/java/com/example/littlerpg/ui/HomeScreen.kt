package com.example.littlerpg.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.R
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun HomeScreen(
    hasSave: Boolean,
    onContinue: () -> Unit,
    onNewSoloGame: () -> Unit,
    onNewMasterGame: () -> Unit
) {
    val context = LocalContext.current
    var isGoogleConnected by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Little RPG",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            OutlinedButton(
                onClick = onContinue,
                enabled = hasSave,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, if (hasSave) Color.Black else Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Continuer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onNewSoloGame,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Nouvelle partie solo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onNewMasterGame,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "👑 Nouvelle partie Master",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    isGoogleConnected = !isGoogleConnected
                    val msg = if (isGoogleConnected) "Connecté à Google Drive ☁️ (Synchronisation activée)" else "Déconnecté de Google Drive"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isGoogleConnected) Color(0xFF2E7D32) else Color(0xFF4285F4)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isGoogleConnected) Color(0xFF2E7D32) else Color(0xFF4285F4)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = if (isGoogleConnected) "☁️ Connecté à Google Drive" else "🌐 Connexion Google Drive",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // RPG Party illustration using dnd.jpg
        Image(
            painter = painterResource(id = R.drawable.dnd),
            contentDescription = "RPG Adventurers Party",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LittleRpgTheme {
        HomeScreen(
            hasSave = true,
            onContinue = {},
            onNewSoloGame = {},
            onNewMasterGame = {}
        )
    }
}
