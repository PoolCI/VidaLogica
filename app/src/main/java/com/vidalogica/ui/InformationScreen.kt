package com.vidalogica.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Paleta de colores de alto contraste en escala de grises
private val darkBackground = Color(0xFF1A1A1A)
private val cardColor = Color(0xFF2C2C2C)
private val outlineColor = Color(0xFF444444)
private val textColor = Color.White

private data class ExampleTable(val headers: List<String>, val rows: List<List<String>>)

private val operatorInfoList = listOf(
    OperatorInfoData("¬", "Negación (NO)", "Invierte el valor de verdad de una proposición.", 
        ExampleTable(
            headers = listOf("p", "¬p"), 
            rows = listOf(listOf("V", "F"), listOf("F", "V"))
        )
    ),
    OperatorInfoData("^", "Conjunción (Y)", "Es verdadera solo si ambas proposiciones son verdaderas.",
        ExampleTable(
            headers = listOf("p", "q", "p ^ q"),
            rows = listOf(listOf("V", "V", "V"), listOf("V", "F", "F"), listOf("F", "V", "F"), listOf("F", "F", "F"))
        )
    ),
    OperatorInfoData("v", "Disyunción (O)", "Es verdadera si al menos una de las proposiciones es verdadera.",
        ExampleTable(
            headers = listOf("p", "q", "p v q"),
            rows = listOf(listOf("V", "V", "V"), listOf("V", "F", "V"), listOf("F", "V", "V"), listOf("F", "F", "F"))
        )
    ),
    OperatorInfoData("->", "Implicación", "Es falsa solo si la primera proposición es verdadera y la segunda es falsa.",
        ExampleTable(
            headers = listOf("p", "q", "p -> q"),
            rows = listOf(listOf("V", "V", "V"), listOf("V", "F", "F"), listOf("F", "V", "V"), listOf("F", "F", "V"))
        )
    ),
    OperatorInfoData("<->", "Bicondicional", "Es verdadera solo si ambas proposiciones tienen el mismo valor de verdad.",
        ExampleTable(
            headers = listOf("p", "q", "p <-> q"),
            rows = listOf(listOf("V", "V", "V"), listOf("V", "F", "F"), listOf("F", "V", "F"), listOf("F", "F", "V"))
        )
    )
)

private data class OperatorInfoData(val symbol: String, val name: String, val explanation: String, val exampleTable: ExampleTable)

@Composable
fun InformationScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(darkBackground)) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text(
                    "Guía de Lógica Proposicional",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, // Título en negrita
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = textColor
                )
            }
            item {
                Text(
                    "Una proposición es una afirmación que puede ser verdadera o falsa. Se representan con letras minúsculas (p, q, r...). Los operadores lógicos combinan estas proposiciones para formar expresiones más complejas.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = textColor
                )
            }
            item {
                Text(
                    "Operadores Lógicos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = textColor
                )
            }
            items(operatorInfoList) { operatorInfo ->
                ExpandableOperatorCard(operatorInfo = operatorInfo)
            }
        }
    }
}

@Composable
private fun ExpandableOperatorCard(operatorInfo: OperatorInfoData, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, outlineColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = operatorInfo.symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(text = operatorInfo.name, style = MaterialTheme.typography.titleMedium, color = textColor)
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = textColor
                )
            }
            if (expanded) {
                Text(
                    text = operatorInfo.explanation,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    color = textColor
                )
                ExampleTruthTable(table = operatorInfo.exampleTable)
            }
        }
    }
}

@Composable
private fun ExampleTruthTable(table: ExampleTable) {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .border(1.dp, outlineColor.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            .background(outlineColor.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
    ) {
        // Header
        Row(
            Modifier
                .background(outlineColor.copy(alpha = 0.2f))
                .fillMaxWidth()
        ) {
            table.headers.forEach { headerText ->
                Text(
                    text = headerText,
                    modifier = Modifier.weight(1f).padding(6.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }
        }
        // Rows
        table.rows.forEach { rowData ->
            Row(Modifier.fillMaxWidth()) {
                rowData.forEach { value ->
                    Text(
                        text = value,
                        modifier = Modifier.weight(1f).padding(6.dp),
                        textAlign = TextAlign.Center,
                        color = textColor.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}