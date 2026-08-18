package com.timebasedfitness.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.timebasedfitness.app.MainActivity
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.repository.CompletionRepository
import com.timebasedfitness.app.data.repository.RoutineRepository
import android.content.Intent
import com.timebasedfitness.app.ui.theme.AccentEveningIndigo
import com.timebasedfitness.app.ui.theme.AccentMealsTerracotta
import com.timebasedfitness.app.ui.theme.AccentMorningAmber
import com.timebasedfitness.app.ui.theme.AccentWorkoutTeal
import com.timebasedfitness.app.ui.theme.OnPrimary
import com.timebasedfitness.app.ui.theme.SurfaceContainerHigh
import com.timebasedfitness.app.ui.theme.SurfaceContainerLowest
import com.timebasedfitness.app.ui.theme.TextOnSurface
import com.timebasedfitness.app.ui.theme.TextOnSurfaceVariant
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/**
 * Home-screen widget with three states (Active / Next / Idle). Mirrors the in-app
 * home card with category color, icon chip, next-up countdown, and a streak chip
 * when a streak is active. Pulled data via Hilt entry points so the widget stays
 * in sync with the app without relying on a refresh tick.
 */
class NowRoutineWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun completionRepository(): CompletionRepository
        fun routineRepository(): RoutineRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val bundle: Pair<WidgetState, Int> = runCatching {
            val entry = EarlyEntryPoints.get(context.applicationContext, WidgetEntryPoint::class.java)
            val completionRepo = entry.completionRepository()
            val routineRepo = entry.routineRepository()

            val streak = completionRepo.currentStreak.first()
            val base = WidgetSnapshot.compute(context)
            val enriched: WidgetState = when (base) {
                is WidgetState.Active -> {
                    val routine = routineRepo.observe(base.category).first()
                    base.copy(
                        title = routine?.title ?: base.category.displayName,
                        total = routine?.steps?.size ?: 0
                    )
                }
                is WidgetState.Next -> {
                    val routine = routineRepo.observe(base.category).first()
                    base.copy(title = routine?.title ?: base.category.displayName)
                }
                WidgetState.Idle -> base
            }
            enriched to streak
        }.getOrElse { WidgetState.Idle to 0 }

        provideContent {
            GlanceTheme {
                WidgetRoot(context = context, snapshot = bundle.first, streak = bundle.second)
            }
        }
    }
}

class NowRoutineWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NowRoutineWidget()
}

// --- Local tokens. Glance widgets can't read MaterialTheme; keep these in sync
// with ui/theme/Color.kt so the visual stays consistent with the in-app look.
private val SurfaceCard = ColorProvider(SurfaceContainerLowest)
private val OnSurface = ColorProvider(TextOnSurface)
private val OnSurfaceVariant = ColorProvider(TextOnSurfaceVariant)
private val OnAccent = ColorProvider(OnPrimary)
private val TrackBackground = ColorProvider(SurfaceContainerHigh)

private fun accentProvider(category: Category): ColorProvider = ColorProvider(
    when (category) {
        Category.MORNING -> AccentMorningAmber
        Category.MEALS -> AccentMealsTerracotta
        Category.WORKOUT -> AccentWorkoutTeal
        Category.EVENING -> AccentEveningIndigo
    }
)

private fun categoryGlyph(category: Category): String = when (category) {
    Category.MORNING -> "M"
    Category.MEALS -> "L"
    Category.WORKOUT -> "W"
    Category.EVENING -> "E"
}

@androidx.compose.runtime.Composable
private fun WidgetRoot(context: Context, snapshot: WidgetState, streak: Int) {
    val accent = when (snapshot) {
        is WidgetState.Active -> accentProvider(snapshot.category)
        is WidgetState.Next -> accentProvider(snapshot.category)
        WidgetState.Idle -> ColorProvider(AccentWorkoutTeal)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(SurfaceCard)
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        when (snapshot) {
            is WidgetState.Active -> ActiveBody(snapshot, accent)
            is WidgetState.Next -> NextBody(snapshot, streak, accent)
            WidgetState.Idle -> IdleBody()
        }
    }
}

@androidx.compose.runtime.Composable
private fun Header(accent: ColorProvider, label: String, trailing: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = trailing,
            style = TextStyle(color = OnSurfaceVariant, fontSize = 11.sp)
        )
    }
}

@androidx.compose.runtime.Composable
private fun ActiveBody(state: WidgetState.Active, accent: ColorProvider) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(accent = accent, label = "ACTIVE", trailing = state.category.displayName.uppercase())
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = state.title,
            style = TextStyle(color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.height(10.dp))
        ProgressText(completed = state.completed, total = state.total, accent = accent)
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = "${state.completed}/${state.total} done · ${state.minutesRemaining} min left",
            style = TextStyle(color = OnSurfaceVariant, fontSize = 12.sp)
        )
    }
}

@androidx.compose.runtime.Composable
private fun NextBody(state: WidgetState.Next, streak: Int, accent: ColorProvider) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(
            accent = accent,
            label = "NEXT",
            trailing = state.startsAt.format(WidgetSnapshot.timeFormatter)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = state.title,
            style = TextStyle(color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.height(10.dp))
        Text(
            text = "Starts ${state.startsAt.format(WidgetSnapshot.timeFormatter)}",
            style = TextStyle(color = OnSurfaceVariant, fontSize = 12.sp)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        if (streak > 0) {
            StreakLabel(streak = streak, accent = accent)
        } else {
            Text(
                text = "Tap to open",
                style = TextStyle(color = OnSurfaceVariant, fontSize = 12.sp)
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun IdleBody() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "Nothing scheduled",
            style = TextStyle(color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = "Open the app to set a routine",
            style = TextStyle(color = OnSurfaceVariant, fontSize = 12.sp)
        )
    }
}

@androidx.compose.runtime.Composable
private fun ProgressText(completed: Int, total: Int, accent: ColorProvider) {
    val pct = if (total > 0) (completed * 100 / total).coerceIn(0, 100) else 0
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track + accent fill within the same Row. Glance doesn't expose ratio-based
        // widths; we approximate by overlaying an accent stripe on a track.
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(6.dp)
                .background(TrackBackground)
                .cornerRadius(3.dp)
        ) {}
        Text(
            text = "  $pct%",
            style = TextStyle(color = OnSurface, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@androidx.compose.runtime.Composable
private fun StreakLabel(streak: Int, accent: ColorProvider) {
    Row(
        modifier = GlanceModifier
            .background(accent)
            .cornerRadius(12.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", style = TextStyle(fontSize = 12.sp))
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = "$streak day streak",
            style = TextStyle(color = OnAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@androidx.compose.runtime.Composable
private fun CategoryGlyph(category: Category, accent: ColorProvider) {
    Box(
        modifier = GlanceModifier
            .size(28.dp)
            .background(accent)
            .cornerRadius(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = categoryGlyph(category),
            style = TextStyle(color = OnAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}
