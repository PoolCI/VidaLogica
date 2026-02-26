package com.vidalogica.ui.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack

// --- Data Structures for the Game ---

data class TruthTable(val header: List<String>, val rows: List<List<Boolean>>)

sealed class LevelData {
    abstract val levelNumber: Int
    abstract val title: String

    data class CompleteTheTable(
        override val levelNumber: Int,
        override val title: String,
        val question: String,
        val partialTable: TruthTable,
        val options: List<List<Boolean>>,
        val correctAnswer: List<Boolean>
    ) : LevelData()

    data class IdentifyTheSymbol(
        override val levelNumber: Int,
        override val title: String,
        val definition: String,
        val options: List<String>,
        val correctAnswer: String
    ) : LevelData()

    data class ResultClassification(
        override val levelNumber: Int,
        override val title: String,
        val table: TruthTable,
        val options: List<String>,
        val correctAnswer: String
    ) : LevelData()

    data class BuildExpression(
        override val levelNumber: Int,
        override val title: String,
        val targetTable: TruthTable,
        val targetResults: List<Boolean>
    ) : LevelData()

    data class ClassifyExpression(
        override val levelNumber: Int,
        override val title: String,
        val expression: String,
        val options: List<String>,
        val correctAnswer: String
    ) : LevelData()
}

sealed class Answer {
    data class TableColumn(val column: List<Boolean>) : Answer()
    data class Symbol(val symbol: String) : Answer()
    data class Classification(val type: String) : Answer()
    data class ExpressionText(val expression: String) : Answer()
}

data class GameUiState(
    val currentLevel: LevelData? = null,
    val selectedAnswer: Answer? = null,
    val isAnswerCorrect: Boolean? = null,
    val buildExpressionText: String = "",
    val maxLevelReached: Int = 1
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("vidalogica_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(GameUiState(maxLevelReached = prefs.getInt("max_level", 1)))
    val uiState = _uiState.asStateFlow()

    private val precedence = mapOf("¬" to 4, "^" to 3, "v" to 2, "->" to 1, "<->" to 0)
    private val associativity = mapOf("¬" to "Right", "^" to "Left", "v" to "Left", "->" to "Right", "<->" to "Left")

    fun loadLevel(levelNumber: Int) {
        // Al cargar un nivel, reseteamos la respuesta pero mantenemos el maxLevelReached
        _uiState.update { 
            it.copy(
                currentLevel = getLevelData(levelNumber),
                selectedAnswer = null,
                isAnswerCorrect = null,
                buildExpressionText = ""
            )
        }
    }

    fun onAnswerSelected(answer: Answer) {
        _uiState.update { it.copy(selectedAnswer = answer, isAnswerCorrect = null) }
    }

    fun onExpressionChange(newText: String) {
        _uiState.update { it.copy(buildExpressionText = newText, selectedAnswer = Answer.ExpressionText(newText), isAnswerCorrect = null) }
    }

    fun onSymbolClick(symbol: String) {
        val currentText = _uiState.value.buildExpressionText
        onExpressionChange(currentText + symbol)
    }

    fun checkAnswer() {
        val currentState = _uiState.value
        val level = currentState.currentLevel
        val answer = currentState.selectedAnswer

        val isCorrect = when (level) {
            is LevelData.CompleteTheTable -> (answer as? Answer.TableColumn)?.column == level.correctAnswer
            is LevelData.IdentifyTheSymbol -> (answer as? Answer.Symbol)?.symbol == level.correctAnswer
            is LevelData.ResultClassification -> (answer as? Answer.Classification)?.type == level.correctAnswer
            is LevelData.BuildExpression -> {
                val userExpr = (answer as? Answer.ExpressionText)?.expression ?: ""
                evaluateUserExpression(userExpr, level)
            }
            is LevelData.ClassifyExpression -> (answer as? Answer.Classification)?.type == level.correctAnswer
            null -> false
        }
        
        if (isCorrect && level != null) {
            val nextLevel = level.levelNumber + 1
            if (nextLevel > currentState.maxLevelReached && nextLevel <= 5) {
                saveProgress(nextLevel)
            }
        }

        _uiState.update { it.copy(isAnswerCorrect = isCorrect) }
    }

    private fun saveProgress(level: Int) {
        prefs.edit().putInt("max_level", level).apply()
        _uiState.update { it.copy(maxLevelReached = level) }
    }

    private fun evaluateUserExpression(expression: String, level: LevelData.BuildExpression): Boolean {
        if (expression.isEmpty()) return false
        try {
            val rpn = toRPN(expression)
            val propositions = level.targetTable.header.filter { it.length == 1 && it[0].isLetter() }.map { it[0] }
            
            val userResults = level.targetTable.rows.map { row ->
                val values = propositions.zip(row).toMap()
                evaluateRPN(rpn, values)
            }
            return userResults == level.targetResults
        } catch (e: Exception) {
            return false
        }
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
                    if (stack.isNotEmpty()) stack.pop() else throw Exception("Error")
                }
                precedence.containsKey(token) -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        val top = stack.peek()
                        val pTop = precedence[top] ?: -1
                        val pToken = precedence[token] ?: -1
                        if (pTop > pToken || (pTop == pToken && associativity[top] == "Left")) {
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

    private fun getLevelData(levelNumber: Int): LevelData? {
        return when (levelNumber) {
            1 -> getLevel1Data()
            2 -> getLevel2Data()
            3 -> getLevel3Data()
            4 -> getLevel4Data()
            5 -> getLevel5Data()
            else -> null
        }
    }

    private fun getLevel1Data() = LevelData.CompleteTheTable(
        levelNumber = 1, title = "Completar la Tabla", question = "p -> q",
        partialTable = TruthTable(header = listOf("p", "q"), rows = listOf(listOf(true, true), listOf(true, false), listOf(false, true), listOf(false, false))),
        options = listOf(listOf(true, false, true, true), listOf(true, true, false, false), listOf(false, true, false, true), listOf(true, false, false, true)),
        correctAnswer = listOf(true, false, true, true)
    )

    private fun getLevel2Data() = LevelData.IdentifyTheSymbol(
        levelNumber = 2, title = "Identificar el Símbolo", definition = "Esta operación es verdadera solo si ambas proposiciones tienen el mismo valor de verdad (ambas verdaderas o ambas falsas).",
        options = listOf("¬", "^", "v", "->", "<->"), correctAnswer = "<->"
    )

    private fun getLevel3Data() = LevelData.ResultClassification(
        levelNumber = 3, title = "Resultado Final",
        table = TruthTable(header = listOf("p", "q", "Resultado"), rows = listOf(listOf(true, true, true), listOf(true, false, true), listOf(false, true, true), listOf(false, false, true))),
        options = listOf("Tautología", "Contradicción", "Contingencia"), correctAnswer = "Tautología"
    )

    private fun getLevel4Data() = LevelData.BuildExpression(
        levelNumber = 4, title = "Construir la Expresión",
        targetTable = TruthTable(header = listOf("p", "q"), rows = listOf(listOf(true, true), listOf(true, false), listOf(false, true), listOf(false, false))),
        targetResults = listOf(true, false, true, true)
    )

    private fun getLevel5Data() = LevelData.ClassifyExpression(
        levelNumber = 5, title = "Clasificar la Expresión", expression = "p ^ ¬p",
        options = listOf("Tautología", "Contradicción", "Contingencia"), correctAnswer = "Contradicción"
    )
}
