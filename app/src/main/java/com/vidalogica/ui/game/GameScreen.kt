package com.vidalogica.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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

// --- Style ---
private val darkBackground = Color(0xFF1A1A1A)
private val componentColor = Color(0xFF2C2C2C)
private val outlineColor = Color(0xFF444444)
private val textColor = Color.White
private val headerColor = Color(0xFF3C3C3C)
private val correctColor = Color(0xFF4CAF50)
private val incorrectColor = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLevels: () -> Unit,
    onNextLevel: (Int) -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val uiState by gameViewModel.uiState.collectAsState()
    var lastClickTime by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(level) {
        gameViewModel.loadLevel(level)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Nivel $level", fontWeight = FontWeight.Bold, color = textColor) },
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
                actions = {
                    IconButton(onClick = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime > 500L) {
                            lastClickTime = currentTime
                            onNavigateToLevels()
                        }
                    }) {
                        Icon(Icons.Filled.List, contentDescription = "Niveles", tint = textColor)
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.currentLevel?.let { currentLevel ->
                    item {
                        Text(
                            text = currentLevel.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    when (currentLevel) {
                        is LevelData.CompleteTheTable -> {
                            item {
                                Text(
                                    text = "Completa la tabla para: ${currentLevel.question}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            item { GameTruthTable(table = currentLevel.partialTable) }
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.height(280.dp).padding(top = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(currentLevel.options) { option ->
                                        val isSelected = (uiState.selectedAnswer as? Answer.TableColumn)?.column == option
                                        AnswerCard(
                                            text = option.joinToString(" ") { if (it) "V" else "F" },
                                            isSelected = isSelected,
                                            isCorrect = if (isSelected) uiState.isAnswerCorrect else null,
                                            onCardSelected = { if (uiState.isAnswerCorrect != true) gameViewModel.onAnswerSelected(Answer.TableColumn(option)) }
                                        )
                                    }
                                }
                            }
                        }
                        is LevelData.IdentifyTheSymbol -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = componentColor),
                                    border = BorderStroke(1.dp, outlineColor),
                                    modifier = Modifier.padding(bottom = 24.dp)
                                ) {
                                    Text(
                                        text = currentLevel.definition,
                                        color = textColor,
                                        modifier = Modifier.padding(24.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.height(200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(currentLevel.options) { symbol ->
                                        val isSelected = (uiState.selectedAnswer as? Answer.Symbol)?.symbol == symbol
                                        AnswerCard(
                                            text = symbol,
                                            isSelected = isSelected,
                                            isCorrect = if (isSelected) uiState.isAnswerCorrect else null,
                                            onCardSelected = { if (uiState.isAnswerCorrect != true) gameViewModel.onAnswerSelected(Answer.Symbol(symbol)) }
                                        )
                                    }
                                }
                            }
                        }
                        is LevelData.ResultClassification -> {
                            item { GameTruthTable(table = currentLevel.table) }
                            item {
                                Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    currentLevel.options.forEach { classification ->
                                        val isSelected = (uiState.selectedAnswer as? Answer.Classification)?.type == classification
                                        AnswerCard(
                                            text = classification,
                                            isSelected = isSelected,
                                            isCorrect = if (isSelected) uiState.isAnswerCorrect else null,
                                            onCardSelected = { if (uiState.isAnswerCorrect != true) gameViewModel.onAnswerSelected(Answer.Classification(classification)) }
                                        )
                                    }
                                }
                            }
                        }
                        is LevelData.BuildExpression -> {
                            item {
                                val displayTable = TruthTable(
                                    header = currentLevel.targetTable.header + "Res",
                                    rows = currentLevel.targetTable.rows.mapIndexed { index, row -> row + currentLevel.targetResults[index] }
                                )
                                GameTruthTable(table = displayTable)
                            }
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = componentColor),
                                    border = BorderStroke(1.dp, outlineColor),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                                ) {
                                    Text(
                                        text = uiState.buildExpressionText.ifEmpty { "Escribe la fórmula..." },
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = if (uiState.buildExpressionText.isEmpty()) textColor.copy(0.5f) else textColor,
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            item {
                                val symbols = listOf("p", "q", "¬", "^", "v", "->", "<->", "(", ")")
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.height(240.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(symbols) { s ->
                                        Button(
                                            onClick = { if (uiState.isAnswerCorrect != true) gameViewModel.onSymbolClick(s) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = componentColor, contentColor = textColor),
                                            border = BorderStroke(1.dp, outlineColor)
                                        ) { Text(s, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                                    }
                                    item {
                                        Button(
                                            onClick = { gameViewModel.onExpressionChange("") },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = textColor),
                                            border = BorderStroke(1.dp, outlineColor)
                                        ) { Icon(Icons.Filled.Backspace, "", tint = textColor) }
                                    }
                                }
                            }
                        }
                        is LevelData.ClassifyExpression -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = componentColor),
                                    border = BorderStroke(2.dp, outlineColor),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                ) {
                                    Text(
                                        text = currentLevel.expression,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = textColor,
                                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    currentLevel.options.forEach { opt ->
                                        val isSelected = (uiState.selectedAnswer as? Answer.Classification)?.type == opt
                                        AnswerCard(
                                            text = opt,
                                            isSelected = isSelected,
                                            isCorrect = if (isSelected) uiState.isAnswerCorrect else null,
                                            onCardSelected = { if (uiState.isAnswerCorrect != true) gameViewModel.onAnswerSelected(Answer.Classification(opt)) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val isLast = currentLevel.levelNumber >= 5
                        val btnText = if (uiState.isAnswerCorrect == true) (if (!isLast) "SIGUIENTE NIVEL" else "¡COMPLETADO!") else "COMPROBAR"
                        
                        Button(
                            onClick = { 
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastClickTime > 500L) {
                                    lastClickTime = currentTime
                                    if (uiState.isAnswerCorrect == true) {
                                        if (!isLast) onNextLevel(currentLevel.levelNumber + 1) else onNavigateToLevels()
                                    } else gameViewModel.checkAnswer()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 24.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isAnswerCorrect == true) correctColor else textColor,
                                contentColor = Color.Black
                            ),
                            enabled = (uiState.selectedAnswer != null || uiState.buildExpressionText.isNotEmpty()),
                            elevation = ButtonDefaults.buttonElevation(12.dp)
                        ) {
                            Text(btnText, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                    
                    if (uiState.isAnswerCorrect != null) {
                        item {
                            Text(
                                text = if (uiState.isAnswerCorrect == true) "¡EXCELENTE TRABAJO!" else "INCORRECTO, INTENTA DE NUEVO",
                                color = if (uiState.isAnswerCorrect == true) correctColor else incorrectColor,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 24.dp),
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTruthTable(table: TruthTable) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, outlineColor, RoundedCornerShape(8.dp))
            .background(componentColor, RoundedCornerShape(8.dp))
    ) {
        // Headers
        Row(modifier = Modifier.background(headerColor).fillMaxWidth()) {
            table.header.forEach { h ->
                Box(
                    modifier = Modifier.weight(1f).border(1.dp, outlineColor).padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = h, fontWeight = FontWeight.Black, fontSize = 16.sp, color = textColor)
                }
            }
        }
        // Rows
        table.rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { v ->
                    Box(
                        modifier = Modifier.weight(1f).border(0.5.dp, outlineColor.copy(0.5f)).padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (v) "V" else "F",
                            color = if (v) Color(0xFF81C784) else Color(0xFFE57373),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerCard(text: String, isSelected: Boolean, isCorrect: Boolean?, onCardSelected: () -> Unit) {
    val bColor = when (isCorrect) {
        true -> correctColor
        false -> incorrectColor
        null -> if (isSelected) textColor else outlineColor
    }
    Card(
        modifier = Modifier.clickable(onClick = onCardSelected).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = componentColor),
        border = BorderStroke(if (isSelected) 4.dp else 1.dp, bColor)
    ) {
        Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
