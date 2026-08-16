package com.timebasedfitness.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.model.CategorySelection
import java.time.format.DateTimeFormatter

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(selected: Set<Category>, onToggle: (Category) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
    ) {
        Category.entries.forEach { category ->
            val selectedCategory = category in selected
            val accent = CategoryTheme.getAccentColor(category)
            Surface(
                modifier = Modifier.clickable { onToggle(category) },
                shape = CircleShape,
                color = if (selectedCategory) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (selectedCategory) accent else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(category.displayName, Modifier.padding(horizontal = AppSpacing.spaceMd, vertical = AppSpacing.spaceSm), style = MaterialTheme.typography.labelSmall, color = if (selectedCategory) accent else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun TimeWindowsRow(
    selections: List<CategorySelection>,
    onStartTimeClick: (Category) -> Unit,
    onEndTimeClick: (Category) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    selections.filter { it.isEnabled }.forEach { selection ->
        val accent = CategoryTheme.getAccentColor(selection.category)
        Surface(Modifier.fillMaxWidth().padding(vertical = AppSpacing.spaceSm), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))) {
            Row(Modifier.fillMaxWidth().padding(AppSpacing.spaceMd), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(selection.category.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selection.startTime.format(formatter), color = accent, modifier = Modifier.clickable { onStartTimeClick(selection.category) })
                    Spacer(Modifier.width(AppSpacing.spaceSm))
                    Text("– ${selection.endTime.format(formatter)}", color = accent, modifier = Modifier.clickable { onEndTimeClick(selection.category) })
                }
            }
        }
    }
}
