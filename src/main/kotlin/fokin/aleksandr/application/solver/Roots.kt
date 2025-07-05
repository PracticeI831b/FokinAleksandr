// Roots.kt (исправленная версия)
package fokin.aleksandr.application.solver

import fokin.aleksandr.application.ui.Plotter.createPlot
import org.jetbrains.letsPlot.Figure
import kotlin.math.*

data class RootResult(
    val root: Double,
    val iterations: Int,
    val initialGuess: Double,
    val fValue: Double,
    val fullPlot: Figure,
    val zoomedPlot: Figure
)

private const val EPS = 0.001
private const val MAX_ITERATIONS = 5000

fun computeRoots(
    a: String,
    callback: (RootResult?, String) -> Unit
) {
    try {
        val aVal = a.replace(',', '.').toDouble()
        val lowBound = max(0.0, -aVal) + 1e-5

        // Поиск интервала с корнем
        val (interval, initialGuess) = findRootInterval(lowBound, aVal)
            ?: return callback(null, "Корень не найден на интервале [${"%.2f".format(lowBound)}, 100]")

        // Уточнение корня методом итераций
        val (root, iterations) = iterationMethod(initialGuess, aVal)
            ?: return callback(null, "Метод не сошелся для начального приближения ${"%.4f".format(initialGuess)}")

        val fValue = Equation.f(root, aVal) ?: 0.0

        // Динамические границы для общего графика
        val preXLimit = max(abs(root) * 1.5, abs(initialGuess) * 1.5)
        val xLimit = max(preXLimit, 10.0)
        val fullPlot = createPlot(
            -xLimit,
            xLimit,
            aVal,
            "√(x+$aVal) = 1/x",
            intervals = listOf(interval),
            initials = listOf(initialGuess),
            roots = listOf(root),
            isZoomed = false
        )
        val zoomedPlot = createZoomedPlot(root, aVal, lowBound, interval, initialGuess, root)

        callback(RootResult(root, iterations, initialGuess, fValue, fullPlot, zoomedPlot), "")
    } catch (e: Exception) {
        callback(null, "Ошибка: ${e.message}")
    }
}


private fun findRootInterval(low: Double, aVal: Double): Pair<Pair<Double, Double>, Double>? {
    // Учитываем отрицательные значения параметра
    var x1 = if (aVal < 0) abs(aVal) + 1e-5 else low
    var x2 = x1 + 1.0
    var f1 = Equation.f(x1, aVal)
    var f2 = Equation.f(x2, aVal)

    // Постепенно расширяем интервал
    while (f1 != null && f2 != null && f1.sign == f2.sign && x2 < 100.0) {
        x1 = x2
        x2 += 1.0
        f1 = f2
        f2 = Equation.f(x2, aVal)
    }

    if (f1 == null || f2 == null || f1.sign == f2.sign) return null

    // Начальное приближение - середина интервала
    val guess = (x1 + x2) / 2
    return (x1 to x2) to guess
}

private fun iterationMethod(x0: Double, aVal: Double): Pair<Double, Int>? {
    var x = x0
    var iterations = 0

    while (iterations < MAX_ITERATIONS) {
        val nextX = Equation.phi(x, aVal) ?: return null
        if (abs(nextX - x) < EPS) return nextX to iterations

        x = nextX
        iterations++
    }
    return null
}

private fun createZoomedPlot(
    root: Double,
    aVal: Double,
    lowBound: Double,
    interval: Pair<Double, Double>,
    initial: Double,
    iterRoot: Double
): Figure {
    val padding = 0.5
    val viewMinX = max(lowBound, root - padding)
    val viewMaxX = root + padding

    return createPlot(
        viewMinX, viewMaxX, aVal,
        "√(x+$aVal) = 1/x (увеличенный вид)",
        listOf(interval),
        listOf(initial),
        listOf(iterRoot),
        isZoomed = true
    )
}