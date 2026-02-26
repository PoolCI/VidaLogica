package com.vidalogica

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vidalogica.ui.InformationScreen
import com.vidalogica.ui.MainScreen
import com.vidalogica.ui.PropositionsScreen
import com.vidalogica.ui.game.GameScreen
import com.vidalogica.ui.game.GameViewModel
import com.vidalogica.ui.game.LevelsScreen
import com.vidalogica.ui.theme.VidaLogicaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VidaLogicaTheme {
                VidaLogicaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidaLogicaApp() {
    val navController = rememberNavController()
    val activity = (LocalContext.current as? Activity)
    // Instanciamos el GameViewModel aquí para que sea compartido
    val gameViewModel: GameViewModel = viewModel()
    val gameUiState by gameViewModel.uiState.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main") {
                MainScreen(
                    onNavigateToCalculator = { navController.navigate("calculator") },
                    onNavigateToInformation = { navController.navigate("information") },
                    onNavigateToLevels = { navController.navigate("levels") },
                    // Ahora navega al nivel máximo alcanzado
                    onNavigateToGame = { 
                        navController.navigate("game/${gameUiState.maxLevelReached}") 
                    },
                    onExitApp = { activity?.finish() }
                )
            }
            composable("calculator") {
                PropositionsScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("information") {
                InformationScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("levels") {
                LevelsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLevelSelected = { level -> 
                        navController.navigate("game/$level") 
                    }
                )
            }
            composable("game/{level}") { backStackEntry ->
                val level = backStackEntry.arguments?.getString("level")?.toIntOrNull() ?: 1
                GameScreen(
                    level = level,
                    gameViewModel = gameViewModel, // Pasamos el ViewModel compartido
                    onNavigateBack = { 
                        navController.navigate("main") {
                            popUpTo("main") { inclusive = true }
                        } 
                    },
                    onNavigateToLevels = { navController.navigate("levels") },
                    // Nueva función para navegar físicamente al siguiente nivel
                    onNextLevel = { nextLevel ->
                        navController.navigate("game/$nextLevel") {
                            popUpTo("game/$level") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
