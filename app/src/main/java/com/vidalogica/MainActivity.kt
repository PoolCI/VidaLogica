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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vidalogica.ui.InformationScreen
import com.vidalogica.ui.MainScreen
import com.vidalogica.ui.PropositionsScreen
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
                    onExitApp = { activity?.finish() }
                )
            }
            composable("calculator") {
                PropositionsScreen()
            }
            composable("information") {
                InformationScreen()
            }
        }
    }
}
