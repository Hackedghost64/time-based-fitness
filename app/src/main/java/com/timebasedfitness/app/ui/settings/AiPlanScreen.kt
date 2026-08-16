package com.timebasedfitness.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timebasedfitness.app.data.content.AiPlanRequest
import com.timebasedfitness.app.data.content.AiPromptBuilder
import com.timebasedfitness.app.ui.theme.AppSpacing

@Composable
fun AiPlanScreen(
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    var provider by remember { mutableStateOf("ChatGPT") }
    var goal by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var limitations by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppSpacing.marginPage),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Create with AI", style = MaterialTheme.typography.headlineMedium)
        Text("Answer a few questions, then paste the generated JSON back into the app.", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(provider, { provider = it }, Modifier.fillMaxWidth(), label = { Text("AI provider") }, singleLine = true)
        OutlinedTextField(goal, { goal = it }, Modifier.fillMaxWidth(), label = { Text("Main goal") })
        OutlinedTextField(experience, { experience = it }, Modifier.fillMaxWidth(), label = { Text("Experience level") })
        OutlinedTextField(equipment, { equipment = it }, Modifier.fillMaxWidth(), label = { Text("Equipment available") })
        OutlinedTextField(availability, { availability = it }, Modifier.fillMaxWidth(), label = { Text("Available time and days") })
        OutlinedTextField(limitations, { limitations = it }, Modifier.fillMaxWidth(), label = { Text("Limitations or injuries") })
        OutlinedTextField(preferences, { preferences = it }, Modifier.fillMaxWidth(), label = { Text("Preferences") })
        Button(
            onClick = {
                prompt = AiPromptBuilder.build(AiPlanRequest(provider, goal, experience, equipment, availability, limitations, preferences))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Generate prompt") }
        prompt?.let { generated ->
            Text("Copy this prompt into $provider:", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(generated, {}, Modifier.fillMaxWidth(), readOnly = true, minLines = 8)
            OutlinedButton(onClick = { onCopy(generated) }, modifier = Modifier.fillMaxWidth()) { Text("Copy prompt") }
            TextButton(onClick = { onShare(generated) }, modifier = Modifier.fillMaxWidth()) { Text("Share prompt") }
        }
    }
}
