package com.vidalogica.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de colores de alto contraste en escala de grises
private val darkBackground = Color(0xFF1A1A1A)
private val buttonColor = Color(0xFF2C2C2C)
private val outlineColor = Color(0xFF444444)
private val textColor = Color.White

@Composable
fun MainScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToInformation: () -> Unit,
    onNavigateToLevels: () -> Unit,
    onNavigateToGame: () -> Unit,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(modifier = Modifier.weight(1f))

            StyledButton(text = "Jugar", icon = Icons.Filled.VideogameAsset, onClick = onNavigateToGame)
            Spacer(modifier = Modifier.height(16.dp))
            StyledButton(text = "Niveles", icon = Icons.Filled.List, onClick = onNavigateToLevels)
            Spacer(modifier = Modifier.height(16.dp))
            StyledButton(text = "Calculadora", icon = Icons.Filled.Calculate, onClick = onNavigateToCalculator)
            Spacer(modifier = Modifier.height(16.dp))
            StyledButton(text = "Información", icon = Icons.Filled.Info, onClick = onNavigateToInformation)
            Spacer(modifier = Modifier.height(16.dp))
            StyledButton(text = "Salir", icon = Icons.AutoMirrored.Filled.ExitToApp, onClick = onExitApp)
        }
    }
}

@Composable
private fun StyledButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = textColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
        border = BorderStroke(1.dp, outlineColor)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
