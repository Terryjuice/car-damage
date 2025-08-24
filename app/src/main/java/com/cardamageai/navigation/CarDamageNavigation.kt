package com.cardamageai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardamageai.feature.camera.CameraScreen
import com.cardamageai.feature.damageanalysis.DamageAnalysisScreen
import com.cardamageai.feature.damageanalysis.HistoryScreen

@Composable
fun CarDamageNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "camera"
    ) {
        composable("camera") {
            CameraScreen(
                onImageCaptured = { imageUri ->
                    navController.navigate("damage_analysis/$imageUri")
                },
                onHistoryClick = {
                    navController.navigate("history")
                }
            )
        }
        
        composable("damage_analysis/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            DamageAnalysisScreen(
                imageUri = imageUri,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}