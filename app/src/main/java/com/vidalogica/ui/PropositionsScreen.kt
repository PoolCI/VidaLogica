package com.vidalogica.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Paleta de colores de alto contraste en escala de grises
private val darkBackground = Color(0xFF1A1A1A)
private val componentColor = Color(0xFF2C2C2C)
private val outlineColor = Color(0xFF444444)
private val textColor = Color.White
private val headerColor = Color(0xFF3C3C3C)

@Composable
fun PropositionsScreen(
    modifier: Modifier = Modifier,
    viewModel: PropositionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(darkBackground)) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.statement,
                    onValueChange = { viewModel.onStatementChange(it) },
                    label = { Text("Introduce la expresión lógica") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor,
                        focusedBorderColor = outlineColor,
                        unfocusedBorderColor = outlineColor.copy(alpha = 0.5f),
                        focusedContainerColor = componentColor,
                        unfocusedContainerColor = componentColor,
                        focusedLabelColor = textColor.copy(alpha = 0.8f),
                        unfocusedLabelColor = textColor.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                SymbolKeyboard(onSymbolClick = { viewModel.onSymbolClick(it) })
            }

            item {
                Button(
                    onClick = { viewModel.evaluateStatement() },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = componentColor,
                        contentColor = textColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
                    border = BorderStroke(1.dp, outlineColor)
                ) {
                    Text("Evaluar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (uiState.result.isNotEmpty()) {
                item {
                    Text(
                        text = uiState.result,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Red, // Mantenemos el rojo para errores
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            uiState.truthTable?.let {
                item {
                    TruthTableUI(table = it)
                }
            }
        }
    }
}

@Composable
private fun SymbolKeyboard(onSymbolClick: (String) -> Unit) {
    val symbols = listOf("(", ")", "¬", "^", "v", "->", "<->")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        symbols.forEach { symbol ->
            Button(
                onClick = { onSymbolClick(symbol) },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = componentColor,
                    contentColor = textColor
                ),
                border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f))
            ) {
                Text(text = symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TruthTableUI(table: TruthTable, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .padding(top = 16.dp)
            .horizontalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .background(componentColor, RoundedCornerShape(8.dp))
        ) {
            // Header
            Row(
                Modifier
                    .background(headerColor, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                table.header.forEach { headerText ->
                    Text(
                        text = headerText,
                        modifier = Modifier
                            .width(150.dp) // Ancho aumentado para expresiones largas
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = textColor,
                        maxLines = Int.MAX_VALUE // Permite múltiples líneas si es necesario
                    )
                }
            }
            // Rows
            table.rows.forEach { rowData ->
                Row(
                    Modifier.border(width = 0.5.dp, color = outlineColor),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowData.forEach { value ->
                        Text(
                            text = if (value) "V" else "F",
                            modifier = Modifier
                                .width(150.dp) // Mismo ancho para alineación
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}
