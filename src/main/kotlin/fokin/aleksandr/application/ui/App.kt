package fokin.aleksandr.application.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fokin.aleksandr.application.solver.RootResult
import fokin.aleksandr.application.solver.computeRoots
import org.jetbrains.letsPlot.Figure


@Composable
fun App() {
    var a by remember { mutableStateOf("1.0") }
    var results by remember { mutableStateOf<RootResult?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(true) }

    MaterialTheme(colors = lightColors(
        primary = Color.Black,
        secondary = Color.Gray,
        background = Color.White,
        surface = Color(0xFFF5F5F5)
    )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Решение уравнения √(x+a) = 1/x",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            )

            Text(
                text = "Точность вычисления: 0.001",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )

            InputPanel(
                a = a,
                isLoading = isLoading,
                onAChanged = { a = it },
                onCalculate = {
                    isLoading = true
                    errorMessage = ""
                    results = null
                    computeRoots(a) { result, error ->
                        results = result
                        errorMessage = error
                        isLoading = false
                    }
                }
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Измененная секция: результаты и график в одной строке
            if (results != null || isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Панель результатов (левая часть)
                    ResultsSection(
                        results = results,
                        modifier = Modifier.weight(1f)
                    )

                    // Панель графика (правая часть)
                    GraphSection(
                        results = results,
                        isZoomed = isZoomed,
                        isLoading = isLoading,
                        onZoomToggle = { isZoomed = !isZoomed },
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    }
}

// Новый компонент для секции результатов
@Composable
private fun ResultsSection(
    results: RootResult?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (results != null) {
            ResultsCard(
                rootResult = results
            )
        }
    }
}

// Новый компонент для секции графика
@Composable
private fun GraphSection(
    results: RootResult?,
    isZoomed: Boolean,
    isLoading: Boolean,
    onZoomToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        GraphControls(
            isZoomed = isZoomed,
            onZoomToggle = onZoomToggle
        )
        Spacer(modifier = Modifier.height(8.dp))
        GraphArea(
            results = results,
            isZoomed = isZoomed,
            isLoading = isLoading
        )
    }
}

// Остальной код без изменений...
@Composable
private fun InputPanel(
    a: String,
    isLoading: Boolean,
    onAChanged: (String) -> Unit,
    onCalculate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = a,
            onValueChange = onAChanged,
            label = { Text("Параметр a") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Пример: 1.0 или -0.5") },
            enabled = !isLoading,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.secondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCalculate,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(8.dp)
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("Найти корни", fontSize = 16.sp)
        }
    }
}

// App.kt (ключевые изменения)
@Composable
private fun ResultsCard(
    rootResult: RootResult
) {
    Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Результаты:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Text("• Корень: ${"%.5f".format(rootResult.root)}")
            Text("• Итераций: ${rootResult.iterations}")
            Text("• Начальное приближение: ${"%.5f".format(rootResult.initialGuess)}")
            Text("• Значение функции: ${"%.7f".format(rootResult.fValue)}")
        }
    }
}

@Composable
private fun GraphControls(
    isZoomed: Boolean,
    onZoomToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Вид графика:",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (isZoomed) "Увеличенный" else "Общий",
            color = MaterialTheme.colors.primary
        )

        IconButton(onClick = onZoomToggle) {
            Icon(
                imageVector = if (isZoomed)
                    Icons.Default.KeyboardArrowDown else
                    Icons.Default.KeyboardArrowUp,
                contentDescription = "Переключить вид",
                tint = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
private fun GraphArea(
    results: RootResult?,
    isZoomed: Boolean,
    isLoading: Boolean
) {
    Card(
        elevation = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> LoadingIndicator()
            results == null -> Placeholder("Введите параметры и нажмите 'Вычислить'")
            else -> PlotDisplay(
                plot = if (isZoomed) results.zoomedPlot else results.fullPlot,
                rootResult = results
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            fontSize = 18.sp
        )
    }
}

@Composable
private fun PlotDisplay(plot: Figure?, rootResult: RootResult?) {
    if (plot == null) {
        Placeholder("График не доступен")
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Plotter.PlotPanel(figure = plot)
        }
    }
}