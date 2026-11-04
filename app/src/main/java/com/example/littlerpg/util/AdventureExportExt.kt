package com.example.littlerpg.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.littlerpg.model.MasterTurn
import com.example.littlerpg.model.Universe
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun buildMasterAdventureTranscript(
    universe: Universe?,
    masterHistory: List<MasterTurn>
): String = buildString {
    appendLine("=== AVENTURE LITTLE RPG - MODE MAÎTRE DU JEU ===")
    appendLine("Univers : ${universe?.displayName ?: "Inconnu"}")
    appendLine("Date : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
    appendLine("===============================================\n")

    if (masterHistory.isEmpty()) {
        appendLine("Aucun historique d'aventure enregistrée.")
    } else {
        masterHistory.forEach { turn ->
            if (turn.dmPrompt.isNotBlank()) {
                appendLine("👑 MAÎTRE DU JEU :")
                appendLine(turn.dmPrompt)
                appendLine()
            }
            appendLine("🎭 JOUEURS IA (Léo, Cathy, Sam) :")
            appendLine(turn.aiNarrative)
            appendLine("\n-----------------------------------------------\n")
        }
    }
}

fun Context.exportMasterAdventureToGoogleDocs(
    universe: Universe?,
    masterHistory: List<MasterTurn>
) {
    try {
        val transcript = buildMasterAdventureTranscript(universe, masterHistory)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Aventure_${universe?.displayName ?: "RPG"}_$timestamp.txt"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: cacheDir
        val file = File(storageDir, fileName)
        file.writeText(transcript)

        val fileUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Aventure Little RPG - ${universe?.displayName ?: ""}")
            putExtra(Intent.EXTRA_TEXT, transcript)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Exporter vers Google Drive / Docs")
        startActivity(chooserIntent)
        Toast.makeText(this, "Fichier d'aventure généré 📄", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        Toast.makeText(this, "Erreur lors de la création du fichier.", Toast.LENGTH_SHORT).show()
    }
}
