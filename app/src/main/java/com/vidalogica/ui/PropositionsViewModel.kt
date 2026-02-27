package com.vidalogica.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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

    fun onStatementChange(newValue: TextFieldValue) {
        _uiState.update { 
            if (it.statement.text != newValue.text) {
                // Si el texto cambió, limpiamos los resultados previos
                it.copy(statement = newValue, truthTable = null, result = "", steps = emptyList())
            } else {
                // Si solo cambió la selección (cursor), mantenemos los resultados
                it.copy(statement = newValue)
            }
        }
    }

    fun onSymbolClick(symbol: String) {
        val currentTextField = _uiState.value.statement
        val text = currentTextField.text
        val selection = currentTextField.selection

        val newText = text.substring(0, selection.start) + symbol + text.substring(selection.end)
        val newCursorPosition = selection.start + symbol.length

        onStatementChange(
            TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition)
            )
        )
    }

    fun onDeleteClick() {
        val currentTextField = _uiState.value.statement
        val text = currentTextField.text
        val selection = currentTextField.selection

        if (selection.start > 0 || selection.end > selection.start) {
            val newText: String
            val newCursorPosition: Int

            if (selection.end > selection.start) {
                // Borrar selección
                newText = text.substring(0, selection.start) + text.substring(selection.end)
                newCursorPosition = selection.start
            } else {
                // Borrar un caracter (o un operador como ->)
                val textBefore = text.substring(0, selection.start)
                val deletedLength = when {
                    textBefore.endsWith("<->") -> 3
                    textBefore.endsWith("->") -> 2
                    else -> 1
                }
                newText = text.substring(0, selection.start - deletedLength) + text.substring(selection.start)
                newCursorPosition = selection.start - deletedLength
            }

            onStatementChange(
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursorPosition)
                )
            )
        }
    }

    fun onClearAll() {
        onStatementChange(TextFieldValue(""))
    }

    fun evaluateStatement() {
        val statement = _uiState.value.statement.text.trim()
        if (statement.isEmpty()) return

        val propositions = statement.filter { it.isLetter() && it.lowercaseChar() != 'v' }.toSet().sorted()

        if (propositions.isEmpty()) {
            _uiState.update { it.copy(result = "Introduce al menos una variable") }
            return
        }

        try {
            val mainRpn = toRPN(statement)
            val subExpressions = getSubExpressions(mainRpn).distinct()
            val steps = generateSteps(mainRpn)
            
            val calculationHeaders = (propositions.map { it.toString() } + subExpressions).distinct()

            val numPropositions = propositions.size
            val numRows = 1 shl numPropositions
            val fullTableRows = mutableListOf<List<Boolean>>()

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

            val displayHeaders = calculationHeaders.toMutableList()
            if (subExpressions.isNotEmpty()) {
                val finalExpression = subExpressions.last()
                val finalIndex = displayHeaders.indexOf(finalExpression)
                if (finalIndex != -1) {
                    displayHeaders[finalIndex] = "Resultado Final"
                }
            }

            _uiState.update {
                it.copy(
                    truthTable = TruthTable(header = displayHeaders, rows = fullTableRows),
                    result = "",
                    steps = steps
                )
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

    private fun generateSteps(rpn: List<String>): List<LogicalStep> {
        val stack = Stack<String>()
        val steps = mutableListOf<LogicalStep>()
        var stepCount = 1

        for (token in rpn) {
            if (precedence.containsKey(token)) {
                val step = when (token) {
                    "¬" -> {
                        val a = stack.pop()
                        val expr = "¬$a"
                        stack.push(expr)
                        LogicalStep(stepCount++, expr, "Negación", "Invertimos el valor de '$a'. Si es V pasa a ser F, y viceversa.")
                    }
                    "^" -> {
                        val b = stack.pop(); val a = stack.pop()
                        val expr = "($a ^ $b)"
                        stack.push(expr)
                        LogicalStep(stepCount++, expr, "Conjunción (AND)", "Es Verdadero solo si '$a' y '$b' son ambos Verdaderos.")
                    }
                    "v" -> {
                        val b = stack.pop(); val a = stack.pop()
                        val expr = "($a v $b)"
                        stack.push(expr)
                        LogicalStep(stepCount++, expr, "Disyunción (OR)", "Es Falso solo si '$a' y '$b' son ambos Falsos.")
                    }
                    "->" -> {
                        val b = stack.pop(); val a = stack.pop()
                        val expr = "($a -> $b)"
                        stack.push(expr)
                        LogicalStep(stepCount++, expr, "Condicional", "Es Falso solo cuando el antecedente '$a' es V y el consecuente '$b' es F.")
                    }
                    "<->" -> {
                        val b = stack.pop(); val a = stack.pop()
                        val expr = "($a <-> $b)"
                        stack.push(expr)
                        LogicalStep(stepCount++, expr, "Bicondicional", "Es Verdadero si '$a' y '$b' tienen el mismo valor de verdad.")
                    }
                    else -> null
                }
                step?.let { steps.add(it) }
            } else {
                stack.push(token)
            }
        }
        return steps
    }

    private fun toRPN(infix: String): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()
        val regex = "(<->|->|¬|\\^|v|[a-zA-Z]|\\(|\\))".toRegex()
        val tokens = regex.findAll(infix).map { it.value }.toList()

        for (token in tokens) {
            when {
                token.matches(Regex("[a-zA-Z]")) && token.lowercase() != "v" -> output.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") output.add(stack.pop())
                    if (stack.isNotEmpty()) stack.pop() else throw Exception("Paréntesis disparejos")
                }
                precedence.containsKey(token) -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        val top = stack.peek()
                        val pTop = precedence[top] ?: -1
                        val pOp = precedence[token] ?: -1
                        if (pTop > pOp || (pTop == pOp && associativity[top] == "Left")) {
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

data class LogicalStep(val stepNumber: Int, val expression: String, val type: String, val description: String)

data class PropositionsUiState(
    val statement: TextFieldValue = TextFieldValue(""),
    val propositions: List<String> = emptyList(),
    val truthTable: TruthTable? = null,
    val result: String = "",
    val steps: List<LogicalStep> = emptyList()
)
