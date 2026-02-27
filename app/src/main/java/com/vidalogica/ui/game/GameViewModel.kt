package com.vidalogica.ui.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack

// --- Data Structures ---
data class TruthTable(val header: List<String>, val rows: List<List<Boolean>>)

sealed class LevelData {
    abstract val levelNumber: Int
    abstract val title: String

    data class CompleteTheTable(override val levelNumber: Int, override val title: String, val question: String, val partialTable: TruthTable, val options: List<List<Boolean>>, val correctAnswer: List<Boolean>) : LevelData()
    data class IdentifyTheSymbol(override val levelNumber: Int, override val title: String, val definition: String, val options: List<String>, val correctAnswer: String) : LevelData()
    data class ResultClassification(override val levelNumber: Int, override val title: String, val question: String, val table: TruthTable, val options: List<String>, val correctAnswer: String) : LevelData()
    data class BuildExpression(override val levelNumber: Int, override val title: String, val targetTable: TruthTable, val targetResults: List<Boolean>) : LevelData()
    data class ClassifyExpression(override val levelNumber: Int, override val title: String, val expression: String, val options: List<String>, val correctAnswer: String) : LevelData()
    data class DirectEvaluation(override val levelNumber: Int, override val title: String, val expression: String, val values: Map<Char, Boolean>, val question: String, val options: List<String>, val correctAnswer: String) : LevelData()
    data class LogicEquivalence(override val levelNumber: Int, override val title: String, val expr1: String, val expr2: String, val question: String, val options: List<String>, val correctAnswer: String) : LevelData()
}

sealed class Answer {
    data class TableColumn(val column: List<Boolean>) : Answer()
    data class Symbol(val symbol: String) : Answer()
    data class Classification(val type: String) : Answer()
    data class ExpressionText(val expression: String) : Answer()
    data class BooleanValue(val value: Boolean) : Answer()
}

data class GameUiState(
    val currentLevel: LevelData? = null,
    val selectedAnswer: Answer? = null,
    val isAnswerCorrect: Boolean? = null,
    val buildExpressionText: String = "",
    val maxLevelReached: Int = 1,
    val penaltyTimeRemaining: Long = 0L,
    val consecutiveErrors: Int = 0,
    val isHintUsed: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("vidalogica_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(GameUiState(maxLevelReached = prefs.getInt("max_level", 1)))
    val uiState = _uiState.asStateFlow()

    private val precedence = mapOf("¬" to 4, "^" to 3, "v" to 2, "->" to 1, "<->" to 0)
    private val associativity = mapOf("¬" to "Right", "^" to "Left", "v" to "Left", "->" to "Right", "<->" to "Left")

    init { startPenaltyTimer() }

    private fun startPenaltyTimer() {
        viewModelScope.launch {
            while (true) {
                val unlockTime = prefs.getLong("unlock_time", 0L)
                val currentTime = System.currentTimeMillis()
                if (unlockTime > currentTime) {
                    _uiState.update { it.copy(penaltyTimeRemaining = (unlockTime - currentTime) / 1000) }
                } else {
                    _uiState.update { it.copy(penaltyTimeRemaining = 0L) }
                }
                delay(1000)
            }
        }
    }

    fun loadLevel(levelNumber: Int) {
        _uiState.update { it.copy(currentLevel = getLevelData(levelNumber), selectedAnswer = null, isAnswerCorrect = null, buildExpressionText = "", consecutiveErrors = 0, isHintUsed = false) }
    }

    fun onAnswerSelected(answer: Answer) {
        if (_uiState.value.penaltyTimeRemaining > 0) return
        _uiState.update { it.copy(selectedAnswer = answer, isAnswerCorrect = null) }
    }

    fun onExpressionChange(newText: String) {
        if (_uiState.value.penaltyTimeRemaining > 0) return
        _uiState.update { it.copy(buildExpressionText = newText, selectedAnswer = Answer.ExpressionText(newText), isAnswerCorrect = null) }
    }

    fun onSymbolClick(symbol: String) {
        if (_uiState.value.penaltyTimeRemaining > 0) return
        onExpressionChange(_uiState.value.buildExpressionText + symbol)
    }

    fun markHintUsed() {
        _uiState.update { it.copy(isHintUsed = true) }
    }

    fun checkAnswer() {
        val currentState = _uiState.value
        if (currentState.penaltyTimeRemaining > 0) return
        val level = currentState.currentLevel ?: return
        val answer = currentState.selectedAnswer

        val isCorrect = when (level) {
            is LevelData.CompleteTheTable -> (answer as? Answer.TableColumn)?.column == level.correctAnswer
            is LevelData.IdentifyTheSymbol -> (answer as? Answer.Symbol)?.symbol == level.correctAnswer
            is LevelData.ResultClassification -> (answer as? Answer.Classification)?.type == level.correctAnswer
            is LevelData.BuildExpression -> evaluateUserExpression((answer as? Answer.ExpressionText)?.expression ?: "", level)
            is LevelData.ClassifyExpression -> (answer as? Answer.Classification)?.type == level.correctAnswer
            is LevelData.DirectEvaluation -> (answer as? Answer.Classification)?.type == level.correctAnswer
            is LevelData.LogicEquivalence -> (answer as? Answer.Classification)?.type == level.correctAnswer
        }

        if (isCorrect) {
            _uiState.update { it.copy(consecutiveErrors = 0, isAnswerCorrect = true) }
            val nextLevel = level.levelNumber + 1
            if (nextLevel > currentState.maxLevelReached && nextLevel <= 10 && !currentState.isHintUsed) saveProgress(nextLevel)
        } else {
            val newErrors = currentState.consecutiveErrors + 1
            if (newErrors >= 2) {
                applyPenalty()
                _uiState.update { it.copy(consecutiveErrors = 0, isAnswerCorrect = false) }
            } else {
                _uiState.update { it.copy(consecutiveErrors = newErrors, isAnswerCorrect = false) }
            }
        }
    }

    private fun applyPenalty() {
        val unlockTime = System.currentTimeMillis() + 60000
        prefs.edit().putLong("unlock_time", unlockTime).apply()
    }

    private fun saveProgress(level: Int) {
        prefs.edit().putInt("max_level", level).apply()
        _uiState.update { it.copy(maxLevelReached = level) }
    }

    private fun evaluateUserExpression(expression: String, level: LevelData.BuildExpression): Boolean {
        if (expression.isEmpty()) return false
        try {
            val rpn = toRPN(expression)
            val props = level.targetTable.header.filter { it.length == 1 && it[0].isLetter() }.map { it[0] }
            val results = level.targetTable.rows.map { row ->
                val vals = props.zip(row).toMap()
                evaluateRPN(rpn, vals)
            }
            return results == level.targetResults
        } catch (e: Exception) { return false }
    }

    private fun toRPN(infix: String): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()
        val regex = "(<->|->|¬|\\^|v|[a-zA-Z]|\\(|\\))".toRegex()
        val tokens = regex.findAll(infix).map { it.value }.toList()
        for (token in tokens) {
            when {
                token.matches(Regex("[a-zA-Z]")) -> output.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") output.add(stack.pop())
                    if (stack.isNotEmpty()) stack.pop()
                }
                precedence.containsKey(token) -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        val top = stack.peek()
                        if ((precedence[top] ?: -1) > (precedence[token] ?: -1) || 
                            ((precedence[top] ?: -1) == (precedence[token] ?: -1) && associativity[top] == "Left")) {
                            output.add(stack.pop())
                        } else break
                    }
                    stack.push(token)
                }
            }
        }
        while (stack.isNotEmpty()) output.add(stack.pop())
        return output
    }

    private fun evaluateRPN(rpn: List<String>, values: Map<Char, Boolean>): Boolean {
        val stack = Stack<Boolean>()
        for (token in rpn) {
            when (token) {
                "¬" -> stack.push(!stack.pop())
                "^" -> { val b = stack.pop(); val a = stack.pop(); stack.push(a && b) }
                "v" -> { val b = stack.pop(); val a = stack.pop(); stack.push(a || b) }
                "->" -> { val b = stack.pop(); val a = stack.pop(); stack.push(!a || b) }
                "<->" -> { val b = stack.pop(); val a = stack.pop(); stack.push(a == b) }
                else -> stack.push(values[token[0]] ?: false)
            }
        }
        return stack.pop()
    }

    private fun getLevelData(level: Int) = when (level) {
        1 -> getLevel1Data()
        2 -> getLevel2Data()
        3 -> getLevel3Data()
        4 -> getLevel4Data()
        5 -> getLevel5Data()
        6 -> getLevel6Data()
        7 -> getLevel7Data()
        8 -> getLevel8Data()
        9 -> getLevel9Data()
        10 -> getLevel10Data()
        else -> null
    }

    private fun getLevel1Data() = LevelData.CompleteTheTable(1, "Completar la Tabla", "p -> q", TruthTable(listOf("p", "q"), listOf(listOf(true, true), listOf(true, false), listOf(false, true), listOf(false, false))), listOf(listOf(true, false, true, true), listOf(true, true, false, false), listOf(false, true, false, true), listOf(true, false, false, true)), listOf(true, false, true, true))
    private fun getLevel2Data() = LevelData.IdentifyTheSymbol(2, "Identificar el Símbolo", "Es verdadera solo si ambas proposiciones tienen el mismo valor de verdad.", listOf("¬", "^", "v", "->", "<->"), "<->")
    private fun getLevel3Data() = LevelData.ResultClassification(3, "Resultado Final", "¿Cómo se clasifica esta tabla de verdad según su última columna?", TruthTable(listOf("p", "q", "Res"), listOf(listOf(true, true, true), listOf(true, false, true), listOf(false, true, true), listOf(false, false, true))), listOf("Tautología", "Contradicción", "Contingencia"), "Tautología")
    private fun getLevel4Data() = LevelData.BuildExpression(4, "Construir la Expresión", TruthTable(listOf("p", "q"), listOf(listOf(true, true), listOf(true, false), listOf(false, true), listOf(false, false))), listOf(true, false, true, true))
    private fun getLevel5Data() = LevelData.ClassifyExpression(5, "Clasificar la Expresión", "p ^ ¬p", listOf("Tautología", "Contradicción", "Contingencia"), "Contradicción")
    
    private fun getLevel6Data() = LevelData.DirectEvaluation(
        6, "Evaluación Directa", "(p ^ q) -> (r v p)", 
        mapOf('p' to true, 'q' to false, 'r' to false),
        "¿Cuáles son los valores de (p ^ q) y (r v p) respectivamente?",
        listOf("V y V", "V y F", "F y V", "F y F"), 
        "F y V"
    )
    
    private fun getLevel7Data() = LevelData.LogicEquivalence(
        7, "Equivalencia Lógica", "p -> q", "¬p v q",
        "¿Cuál es la relación lógica entre ambas expresiones?",
        listOf("Son equivalentes", "Solo equivalentes si p es F", "Nunca son equivalentes", "Son opuestas"),
        "Son equivalentes"
    )

    private fun getLevel8Data() = LevelData.CompleteTheTable(8, "Tabla de 3 Variables", "(p ^ q) -> r", TruthTable(listOf("p", "q", "r"), listOf(listOf(true,true,true), listOf(true,true,false), listOf(true,false,true), listOf(true,false,false), listOf(false,true,true), listOf(false,true,false), listOf(false,false,true), listOf(false,false,false))), listOf(listOf(true,false,true,true,true,true,true,true), listOf(true,true,true,true,true,true,true,true), listOf(false,false,false,false,false,false,false,false), listOf(true,false,false,true,true,false,true,true)), listOf(true,false,true,true,true,true,true,true))
    private fun getLevel9Data() = LevelData.ResultClassification(9, "Detectar el Tipo", "Observa la columna 'Res'. ¿A qué tipo de proposición compuesta corresponde este resultado?", TruthTable(listOf("p", "q", "r", "Res"), listOf(listOf(true,true,true,true), listOf(true,true,false,false), listOf(true,false,true,true), listOf(true,false,false,true), listOf(false,true,true,true), listOf(false,true,false,true), listOf(false,false,true,true), listOf(false,false,false,true))), listOf("Tautología", "Contradicción", "Contingencia"), "Contingencia")
    
    private fun getLevel10Data() = LevelData.ResultClassification(
        10, 
        "El Gran Maestro", 
        "¿Cuál de las siguientes fórmulas corresponde a la tabla de verdad mostrada?",
        TruthTable(
            listOf("p", "q", "r", "Res"), 
            listOf(
                listOf(true, true, true, true), 
                listOf(true, true, false, true), 
                listOf(true, false, true, false), 
                listOf(true, false, false, true), 
                listOf(false, true, true, false), 
                listOf(false, true, false, false), 
                listOf(false, false, true, false), 
                listOf(false, false, false, true)
            )
        ), 
        listOf("(p ^ q) <-> (q v r)", "(p v q) -> (q ^ r)", "¬(p ^ q) v r", "(p <-> q) ^ r"), 
        "(p ^ q) <-> (q v r)"
    )
}
