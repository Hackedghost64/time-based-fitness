package com.timebasedfitness.app.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.timebasedfitness.app.MainActivity
import com.timebasedfitness.app.ui.theme.BackgroundWarm
import com.timebasedfitness.app.ui.theme.TextOnSurface
import com.timebasedfitness.app.ui.theme.TextOnSurfaceVariant

class NowRoutineWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetSnapshot.compute(context)
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().background(BackgroundWarm).padding(16.dp).clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(snapshot.first, style = TextStyle(color = ColorProvider(TextOnSurface), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                Text(snapshot.second, style = TextStyle(color = ColorProvider(TextOnSurfaceVariant), fontSize = 13.sp))
            }
        }
    }
}

class NowRoutineWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NowRoutineWidget()
}
