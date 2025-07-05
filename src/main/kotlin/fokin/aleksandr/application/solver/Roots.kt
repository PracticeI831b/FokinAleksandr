package fokin.aleksandr.application.solver

import fokin.aleksandr.application.ui.Equation.f
import fokin.aleksandr.application.ui.Equation.phi
import kotlin.math.*
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.scale.scaleColorManual

data class RootResult(
    val root: Double?,
    val iterations: Int,
    val fValue: Double?,
    val initialApproximation: Double,
    val interval: Pair<Double, Double>?,
    val plot: Figure?
)

fun computeRoots(
    a: String,
    callback: (RootResult?, String) -> Unit
) {
    try {
        val normalizedA = a.replace(',', '.')
        val aVal = normalizedA.toDoubleOrNull()

        if (aVal == null) {
            callback(null, "Ошибка: параметр 'a' должен быть числом")
            return
        }

        // Поиск интервала с корнем
        val interval = findRootInterval(aVal)
        if (interval == null) {
            callback(null, "Не удалось найти корень в допустимой области")
            return
        }

        // Начальное приближение
        val initial = (interval.first + interval.second) / 2

        // Применение метода итераций
        val (root, iterations) = iterationMethod(initial, aVal, 0.001, 1000)

        if (root == null) {
            callback(null, "Метод итераций не сошелся")
            return
        }

        // Построение графика
        val plot = createPlot(aVal, interval, root)

        callback(RootResult(
            root = root,
            iterations = iterations,
            fValue = f(root, aVal),
            initialApproximation = initial,
            interval = interval,
            plot = plot
        ), "")
    } catch (e: Exception) {
        callback(null, "Ошибка: ${e.message}")
    }
}

private fun findRootInterval(aVal: Double): Pair<Double, Double>? {
    val step = 0.1
    var x = 0.1
    var prevY = f(x, aVal) ?: return null

    // Ищем интервал, где функция меняет знак
    while (x <= 10.0) {
        val y = f(x, aVal) ?: return null
        if (prevY * y <= 0) {
            return (x - step) to x
        }
        prevY = y
        x += step
    }
    return null
}

private fun iterationMethod(
    x0: Double,
    aVal: Double,
    eps: Double,
    maxIterations: Int
): Pair<Double?, Int> {
    var x = x0
    var iterations = 0
    var prevX: Double

    do {
        prevX = x
        x = phi(prevX, aVal) ?: return null to 0
        iterations++
    } while (abs(x - prevX) > eps && iterations < maxIterations)

    return x to iterations
}

private fun createPlot(
    aVal: Double,
    interval: Pair<Double, Double>,
    root: Double
): Figure? {
    // Границы графика с расширением
    val padding = 0.5
    val xMin = max(0.1, interval.first - padding)
    val xMax = interval.second + padding
    val points = 200

    val xValues = mutableListOf<Double>()
    val yValues = mutableListOf<Double>()

    for (i in 0..points) {
        val x = xMin + i * (xMax - xMin) / points
        val y = f(x, aVal)
        if (y != null) {
            xValues.add(x)
            yValues.add(y)
        }
    }

    // Точка корня
    val rootY = f(root, aVal)
    if (rootY == null) return null

    val rootData = mapOf(
        "x" to listOf(root),
        "y" to listOf(rootY),
        "label" to listOf("Корень")
    )

    return ggplot() +
            geomLine(
                data = mapOf("x" to xValues, "y" to yValues),
                color = "#2962FF",
                size = 1.5
            ) { x = "x"; y = "y" } +
            geomPoint(
                data = rootData,
                color = "#00E5FF",
                size = 5.0
            ) { x = "x"; y = "y" } +
            geomHLine(yintercept = 0.0, color = "white", linetype = "dashed") +
            ggtitle("f(x) = √(x + $aVal) - 1/x") +
            ggsize(800, 500)
}