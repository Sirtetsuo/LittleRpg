package com.example.littlerpg.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littlerpg.R
import com.example.littlerpg.ui.theme.LittleRpgTheme

@Composable
fun GameOverScreen(
    onLoadGame: () -> Unit,
    onNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.gameover),
            contentDescription = "Game Over Logo",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Votre personnage a succombé à ses blessures.\nL'aventure prend fin ici...",
            fontSize = 16.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoadGame,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp)
        ) {
            Text("💾 Charger la dernière sauvegarde", fontSize = 16.sp, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp)
        ) {
            Text("✨ Recommencer une nouvelle partie", fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameOverScreenPreview() {
    LittleRpgTheme {
        GameOverScreen(
            onLoadGame = {},
            onNewGame = {}
        )
    }
}
