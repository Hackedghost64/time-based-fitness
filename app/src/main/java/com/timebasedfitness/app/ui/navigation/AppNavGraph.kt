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

object Screen {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val RoutineDetail = "routine/{category}"
    const val Settings = "settings"

    fun routineDetail(category: Category) = "routine/${category.name}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
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

        composable(Screen.Settings) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBackToHome = {
                    navController.popBackStack()
                }
            )
        }
    }
}
