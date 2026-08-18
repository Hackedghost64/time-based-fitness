package com.timebasedfitness.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.timebasedfitness.app.data.model.Category
import com.timebasedfitness.app.ui.home.HomeScreen
import com.timebasedfitness.app.ui.home.HomeViewModel
import com.timebasedfitness.app.ui.onboarding.OnboardingScreen
import com.timebasedfitness.app.ui.onboarding.OnboardingViewModel
import com.timebasedfitness.app.ui.routine.RoutineDetailScreen
import com.timebasedfitness.app.ui.routine.RoutineDetailViewModel
import com.timebasedfitness.app.ui.settings.SettingsScreen
import com.timebasedfitness.app.ui.settings.SettingsViewModel
import com.timebasedfitness.app.ui.settings.PlanTransferScreen
import com.timebasedfitness.app.ui.settings.AiPlanScreen
import com.timebasedfitness.app.ui.settings.GuideScreen
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect

object Screen {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val RoutineDetail = "routine/{category}"
    const val Settings = "settings"
    const val Progress = "progress"
    const val PlanTransfer = "plan-transfer"
    const val AiPlan = "ai-plan"
    const val Guide = "guide"

    fun routineDetail(category: Category) = "routine/${category.name}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    initialCategory: String? = null
) {
    LaunchedEffect(initialCategory) {
        if (startDestination == Screen.Home && initialCategory != null && runCatching { Category.valueOf(initialCategory) }.isSuccess) {
            navController.navigate("${Screen.RoutineDetail.substringBefore("{")}$initialCategory")
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onOnboardingComplete = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onRoutineClick = { category ->
                    navController.navigate(Screen.routineDetail(category))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings)
                },
                onProgressClick = {
                    navController.navigate(Screen.Progress)
                }
            )
        }

        composable(
            route = Screen.RoutineDetail,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) {
            val viewModel: RoutineDetailViewModel = hiltViewModel()
            RoutineDetailScreen(
                viewModel = viewModel,
                onBackToHome = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Progress) {
            val viewModel: com.timebasedfitness.app.ui.progress.ProgressViewModel = hiltViewModel()
            com.timebasedfitness.app.ui.progress.ProgressScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBackToHome = {
                    navController.popBackStack()
                },
                onPlanTransfer = { navController.navigate(Screen.PlanTransfer) },
                onAiPlan = { navController.navigate(Screen.AiPlan) },
                onGuide = { navController.navigate(Screen.Guide) }
            )
        }

        composable(Screen.PlanTransfer) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val context = LocalContext.current
            PlanTransferScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onShare = { json ->
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "Onset Fitness Plan (JSON)")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }, "Share fitness plan"))
                }
            )
        }

        composable(Screen.AiPlan) {
            val context = LocalContext.current
            val clipboard = LocalClipboardManager.current
            AiPlanScreen(
                onBack = { navController.popBackStack() },
                onCopy = { clipboard.setText(AnnotatedString(it)) },
                onShare = { prompt ->
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, prompt)
                    }, "Share AI prompt"))
                }
            )
        }

        composable(Screen.Guide) {
            GuideScreen(onBack = { navController.popBackStack() })
        }
    }
}
