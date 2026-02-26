package com.vidalogica.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val darkBackground = Color(0xFF1A1A1A)
private val componentColor = Color(0xFF2C2C2C)
private val outlineColor = Color(0xFF444444)
private val textColor = Color.White
private val headerColor = Color(0xFF3C3C3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropositionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PropositionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Borrar expresión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que quieres limpiar toda la fórmula?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onClearAll()
                    showDeleteDialog = false
                }) { Text("Limpiar Todo", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = textColor) }
            },
            containerColor = componentColor,
            titleContentColor = textColor,
            textContentColor = textColor.copy(alpha = 0.8f)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime > 500L) {
                            lastClickTime = currentTime
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Inicio", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.statement,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fórmula") },
                    placeholder = { Text("Ej: (p ^ q) -> r", color = textColor.copy(alpha = 0.4f)) },
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = componentColor,
                        unfocusedContainerColor = componentColor,
                        focusedBorderColor = outlineColor,
                        unfocusedBorderColor = outlineColor,
                        focusedLabelColor = textColor.copy(alpha = 0.7f),
                        unfocusedLabelColor = textColor.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (uiState.result.isNotEmpty()) {
                    Text(
                        text = uiState.result,
                        color = Color(0xFFE57373),
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    uiState.truthTable?.let { table ->
                        TruthTableUI(table = table)
                    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Escribe una expresión y presiona EVALUAR", color = textColor.copy(alpha = 0.3f))
                    }
                }
            }

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

                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Limpiar Todo", tint = textColor)
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.evaluateStatement() },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = Color.Black)
                    ) {
                        Text("EVALUAR", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
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
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TruthTableUI(table: TruthTable, modifier: Modifier = Modifier) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(hScroll)
            .verticalScroll(vScroll)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .background(componentColor, RoundedCornerShape(8.dp))
        ) {
            Row(
                Modifier
                    .background(headerColor, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                table.header.forEach { headerText ->
                    Text(
                        text = headerText,
                        modifier = Modifier.width(130.dp).padding(8.dp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = textColor,
                        maxLines = 2
                    )
                }
            }
            table.rows.forEach { rowData ->
                Row(
                    Modifier.border(width = 0.5.dp, color = outlineColor.copy(alpha = 0.3f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowData.forEach { value ->
                        Text(
                            text = if (value) "V" else "F",
                            modifier = Modifier.width(130.dp).padding(12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (value) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    }
                }
            }
        }
    }
}
