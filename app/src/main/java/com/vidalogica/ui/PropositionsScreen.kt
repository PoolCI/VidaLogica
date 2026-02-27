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
<<<<<<< Updated upstream
=======
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
>>>>>>> Stashed changes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
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
private val accentColor = Color(0xFF64B5F6)

@Composable
fun PropositionsScreen(
    modifier: Modifier = Modifier,
    viewModel: PropositionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
<<<<<<< Updated upstream
=======
    var showDeleteDialog by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val resultScrollState = rememberScrollState()
>>>>>>> Stashed changes

    Box(modifier = modifier.fillMaxSize().background(darkBackground)) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
<<<<<<< Updated upstream
            item {
=======
            // Área de Entrada y Resultados
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
                Spacer(modifier = Modifier.height(12.dp))

                // Área de Scroll para Procedimiento y Tabla
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(resultScrollState)
                ) {
                    if (uiState.steps.isNotEmpty()) {
                        Text(
                            text = "Procedimiento Paso a Paso",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        uiState.steps.forEach { step ->
                            LogicalStepItem(step = step)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // CORRECCIÓN DEL ERROR DE COMPILACIÓN AQUÍ
                    val table = uiState.truthTable
                    if (table != null) {
                        Text(
                            text = "Tabla de Verdad Completa",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        TruthTableUI(table = table)
                    } else if (uiState.result.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Escribe una expresión y presiona EVALUAR", 
                                color = textColor.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Teclado Fijo en la Base
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("p", "q", "r", "s", "t").forEach { v ->
                            KeyButton(text = v, modifier = Modifier.weight(1f)) { viewModel.onSymbolClick(v) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("¬", "^", "v", "(", ")").forEach { s ->
                            KeyButton(text = s, modifier = Modifier.weight(1f), color = Color.DarkGray) { viewModel.onSymbolClick(s) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KeyButton(text = "->", modifier = Modifier.weight(1.2f), color = Color.DarkGray) { viewModel.onSymbolClick("->") }
                        KeyButton(text = "<->", modifier = Modifier.weight(1.2f), color = Color.DarkGray) { viewModel.onSymbolClick("<->") }
                        
                        Button(
                            onClick = { viewModel.onDeleteClick() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                        ) {
                            Icon(Icons.Filled.Backspace, contentDescription = "Borrar", tint = textColor)
                        }
>>>>>>> Stashed changes

            uiState.truthTable?.let {
                item {
                    TruthTableUI(table = it)
                }
            }
        }
    }
}

@Composable
<<<<<<< Updated upstream
private fun SymbolKeyboard(onSymbolClick: (String) -> Unit) {
    val symbols = listOf("(", ")", "¬", "^", "v", "->", "<->")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
=======
private fun LogicalStepItem(step: LogicalStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = componentColor),
        border = BorderStroke(1.dp, outlineColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Paso ${step.stepNumber}: ${step.type}",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = step.expression,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = outlineColor.copy(alpha = 0.5f))
            Text(
                text = step.description,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun KeyButton(text: String, modifier: Modifier = Modifier, color: Color = componentColor, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = textColor),
        border = BorderStroke(1.dp, outlineColor)
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
    val scrollState = rememberScrollState()
=======
    val hScroll = rememberScrollState()
>>>>>>> Stashed changes

    Row(
        modifier = modifier
<<<<<<< Updated upstream
            .padding(top = 16.dp)
            .horizontalScroll(scrollState)
=======
            .fillMaxWidth()
            .horizontalScroll(hScroll)
>>>>>>> Stashed changes
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
