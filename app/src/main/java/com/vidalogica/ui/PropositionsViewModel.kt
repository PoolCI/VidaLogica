package com.vidalogica.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack

class PropositionsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PropositionsUiState())
    val uiState = _uiState.asStateFlow()

    private val precedence = mapOf("¬" to 4, "^" to 3, "v" to 2, "->" to 1, "<->" to 0)
    private val associativity = mapOf("¬" to "Right", "^" to "Left", "v" to "Left", "->" to "Right", "<->" to "Left")

    fun onStatementChange(newStatement: String) {
        _uiState.update { it.copy(statement = newStatement, truthTable = null, result = "") }
    }

    fun onSymbolClick(symbol: String) {
        val currentStatement = _uiState.value.statement
        onStatementChange(currentStatement + symbol)
    }

    fun evaluateStatement() {
        val statement = _uiState.value.statement.trim()
        if (statement.isEmpty()) return

        val propositions = statement.filter { it.isLetter() && it.lowercaseChar() != 'v' }.toSet().sorted()

        if (propositions.isEmpty()) {
            _uiState.update { it.copy(result = "Introduce al menos una variable (p, q...)") }
            return
        }

        try {
            val mainRpn = toRPN(statement)
            val subExpressions = getSubExpressions(mainRpn).distinct()
            
            // Headers para el cálculo
            val calculationHeaders = (propositions.map { it.toString() } + subExpressions).distinct()

            val numPropositions = propositions.size
            val numRows = 1 shl numPropositions
            val fullTableRows = mutableListOf<List<Boolean>>()

            // Generar filas de V a F
            for (i in numRows - 1 downTo 0) {
                val propositionValues = propositions.associateWith { prop ->
                    val index = propositions.indexOf(prop)
                    (i and (1 shl (numPropositions - 1 - index))) != 0
                }

                val rowResults = mutableListOf<Boolean>()
                for (header in calculationHeaders) {
                    val result = if (header.length == 1 && header[0].isLetter() && header[0].lowercaseChar() != 'v') {
                        propositionValues[header[0]] ?: false
                    } else {
                        evaluateRPN(toRPN(header), propositionValues)
                    }
                    rowResults.add(result)
                }
                fullTableRows.add(rowResults)
            }

            // Crear los headers para la UI, reemplazando el último por "Resultado Final"
            val displayHeaders = calculationHeaders.toMutableList()
            if (subExpressions.isNotEmpty()) {
                val finalExpression = subExpressions.last()
                val finalIndex = displayHeaders.indexOf(finalExpression)
                if (finalIndex != -1) {
                    displayHeaders[finalIndex] = "Resultado Final"
                }
            }

            _uiState.update {
                it.copy(truthTable = TruthTable(header = displayHeaders, rows = fullTableRows), result = "")
            }

        } catch (e: Exception) {
            _uiState.update { it.copy(result = "Error: ${e.message ?: "Expresión inválida"}") }
        }
    }

    private fun getSubExpressions(rpn: List<String>): List<String> {
        val stack = Stack<String>()
        val subs = mutableListOf<String>()
        for (token in rpn) {
            if (precedence.containsKey(token)) {
                val expr = if (token == "¬") {
                    if (stack.isEmpty()) throw Exception("Error en negación")
                    "¬${stack.pop()}"
                } else {
                    if (stack.size < 2) throw Exception("Faltan operandos")
                    val b = stack.pop()
                    val a = stack.pop()
                    "($a $token $b)"
                }
                stack.push(expr)
                subs.add(expr)
            } else {
                stack.push(token)
            }
        }
        return subs
    }

    private fun toRPN(infix: String): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()
        val regex = "(<->|->|¬|\\^|v|[a-zA-Z]|\\(|\\))".toRegex()
        val tokens = regex.findAll(infix).map { it.value }.toList()

        for (token in tokens) {
            when {
                token.matches(Regex("[a-zA-Z]")) && token.lowercase() != "v" -> output.add(token)
                token == "v" && !stack.isEmpty() && stack.peek() != "(" && precedence.containsKey("v") -> handleOp("v", stack, output)
                token == "v" -> if (precedence.containsKey(token)) handleOp(token, stack, output) else output.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") output.add(stack.pop())
                    if (stack.isNotEmpty()) stack.pop() else throw Exception("Paréntesis disparejos")
                }
                precedence.containsKey(token) -> handleOp(token, stack, output)
                else -> output.add(token)
            }
        }
        while (stack.isNotEmpty()) {
            if (stack.peek() == "(") throw Exception("Paréntesis sin cerrar")
            output.add(stack.pop())
        }
        return output
    }

    private fun handleOp(op: String, stack: Stack<String>, output: MutableList<String>) {
        while (stack.isNotEmpty() && stack.peek() != "(") {
            val top = stack.peek()
            val pTop = precedence[top] ?: -1
            val pOp = precedence[op] ?: -1
            if (pTop > pOp || (pTop == pOp && associativity[top] == "Left")) {
                output.add(stack.pop())
            } else break
        }
        stack.push(op)
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
                else -> {
                    val char = token.firstOrNull() ?: ' '
                    stack.push(values[char] ?: false)
                }
            }
        }
        return if (stack.isNotEmpty()) stack.pop() else false
    }
}

data class TruthTable(val header: List<String>, val rows: List<List<Boolean>>)

data class PropositionsUiState(
    val statement: String = "",
    val propositions: List<String> = emptyList(),
    val truthTable: TruthTable? = null,
    val result: String = ""
)
