// Plotter.kt (исправленная версия)
package fokin.aleksandr.application.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import fokin.aleksandr.application.solver.Equation
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.scale.scaleColorManual
import org.jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.letsPlot.scale.scaleYContinuous
import androidx.compose.ui.Modifier
import org.jetbrains.letsPlot.skia.compose.PlotPanel
import kotlin.math.abs
import kotlin.math.max


object Plotter {
    @Composable
    fun PlotPanel(figure: Figure) {
        PlotPanel(
            figure = figure,
            modifier = Modifier.fillMaxSize(),
            preserveAspectRatio = false,
            computationMessagesHandler = { messages ->
                messages.forEach { println("Computation Message: $it") }
            }
        )
    }

    fun createPlot(
        xMin: Double,
        xMax: Double,
        aVal: Double,
        title: String,
        intervals: List<Pair<Double, Double>>?,
        initials: List<Double>?,
        roots: List<Double>,
        isZoomed: Boolean // Добавлен флаг увеличенного вида
    ): Figure {
        // Генерация точек графика
        val points = 1000 // Увеличено количество точек
        val xValues = mutableListOf<Double>()
        val yValues = mutableListOf<Double>()

        for (i in 0..points) {
            val x = xMin + i * (xMax - xMin) / points
            Equation.f(x, aVal)?.let {
                xValues.add(x)
                yValues.add(it)
            }
        }

        // Рассчет границ по Y
        val (yMin, yMax) = calculateYBounds(yValues, roots, initials, aVal, isZoomed)

        // Построение основного графика
        var plot = ggplot(mapOf("x" to xValues, "y" to yValues)) +
                geomLine(color = "darkblue", size = 1.0) { x = "x"; y = "y" } +
                geomHLine(yintercept = 0.0, color = "gray", linetype = "dashed") +
                geomVLine(xintercept = 0.0, color = "gray", linetype = "dashed") +
                ggtitle(title) +
                scaleXContinuous(
                    name = "x",
                    limits = xMin to xMax
                )

        // Для увеличенного графика используем расчетные границы, для общего - симметричные
        if (isZoomed) {
            plot = plot + scaleYContinuous(
                name = "f(x)",
                limits = yMin to yMax
            )
        } else {
            val yRange = maxOf(abs(yMin), abs(yMax), 0.1)
            plot = plot + scaleYContinuous(
                name = "f(x)",
                limits = -yRange to yRange
            )
        }

        // Добавление интервалов
        intervals?.forEach { (start, end) ->
            plot = plot +
                    geomVLine(xintercept = start, color = "orange", linetype = "dashed", alpha = 0.5) +
                    geomVLine(xintercept = end, color = "orange", linetype = "dashed", alpha = 0.5)
        }

        // Добавление особых точек
        val pointTypes = mutableListOf<String>()
        val pointX = mutableListOf<Double>()
        val pointY = mutableListOf<Double>()

        // Начальные приближения
        initials?.forEach { x0 ->
            Equation.f(x0, aVal)?.let { y0 ->
                pointX.add(x0)
                pointY.add(y0)
                pointTypes.add("Начальное приближение")
            }
        }

        // Корни
        roots.forEach { root ->
            Equation.f(root, aVal)?.let { yRoot ->
                pointX.add(root)
                pointY.add(yRoot)
                pointTypes.add("Корень")
            }
        }

        // Добавление точек на график
        if (pointX.isNotEmpty()) {
            val pointsData = mapOf(
                "x" to pointX,
                "y" to pointY,
                "type" to pointTypes
            )

            plot = plot + geomPoint(
                data = pointsData,
                size = 5.0,
                alpha = 0.8
            ) { x = "x"; y = "y"; color = "type" } +
                    scaleColorManual(
                        values = listOf("blue", "red"),
                        name = "Точки"
                    )
        }

        return plot
    }

    private fun calculateYBounds(
        yValues: List<Double>,
        roots: List<Double>,
        initials: List<Double>?,
        aVal: Double,
        isZoomed: Boolean
    ): Pair<Double, Double> {
        if (yValues.isEmpty()) return -1.0 to 1.0

        // Рассчитываем базовые границы
        var yMin = yValues.minOrNull() ?: -1.0
        var yMax = yValues.maxOrNull() ?: 1.0

        // Для увеличенного графика корректируем границы
        if (isZoomed) {
            // Фильтруем выбросы
            val filtered = yValues.filter { abs(it) < 1000 }
            if (filtered.isNotEmpty()) {
                yMin = filtered.minOrNull()!!
                yMax = filtered.maxOrNull()!!
            }

            // Учитываем значения в корнях и начальных приближениях
            val specialPoints = mutableListOf<Double>()

            roots.forEach { root ->
                Equation.f(root, aVal)?.let { specialPoints.add(it) }
            }

            initials?.forEach { init ->
                Equation.f(init, aVal)?.let { specialPoints.add(it) }
            }

            if (specialPoints.isNotEmpty()) {
                val minSpecial = specialPoints.minOrNull()!!
                val maxSpecial = specialPoints.maxOrNull()!!
                yMin = minOf(yMin, minSpecial)
                yMax = maxOf(yMax, maxSpecial)
            }

            // Добавляем 10% запаса
            val padding = max(0.1, (yMax - yMin) * 0.1)
            yMin -= padding
            yMax += padding
        }

        // Гарантируем минимальную высоту графика
        if (abs(yMax - yMin) < 0.1) {
            val center = (yMin + yMax) / 2
            yMin = center - 0.05
            yMax = center + 0.05
        }

        return yMin to yMax
    }
}