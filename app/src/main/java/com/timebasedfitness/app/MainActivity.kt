package com.timebasedfitness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.data.prefs.OnboardingPrefsRepository
import com.timebasedfitness.app.ui.navigation.AppNavGraph
import com.timebasedfitness.app.ui.navigation.Screen
import com.timebasedfitness.app.ui.theme.TimeBasedFitnessTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsRepository: OnboardingPrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeBasedFitnessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasOnboardedState by prefsRepository.hasOnboarded.collectAsState(initial = null)

                    when (val onboarded = hasOnboardedState) {
                        null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        else -> {
                            val startDestination = if (onboarded) Screen.Home else Screen.Onboarding
                            val navController = rememberNavController()
                            // Validate EXTRA_CATEGORY against the enum before forwarding to nav.
                            // A malformed extra (from an external Intent) would otherwise pass an
                            // arbitrary string into the navigation graph.
                            val initialCategory = intent.getStringExtra("category")?.let { raw ->
                                runCatching { Category.valueOf(raw) }.getOrNull()
                            }
                            AppNavGraph(
                                navController = navController,
                                startDestination = startDestination,
                                initialCategory = initialCategory?.name
                            )
                        }
                    }
                }
            }
        }
    }
}
