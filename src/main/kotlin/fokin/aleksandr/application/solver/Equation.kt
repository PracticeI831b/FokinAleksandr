package fokin.aleksandr.application.solver

import kotlin.math.abs
import kotlin.math.sqrt

object Equation {
    fun f(x: Double, aVal: Double): Double? {
        if (x + aVal < 0) return null
        if (x == 0.0) return null

        return try {
            sqrt(x + aVal) - 1.0/x
        } catch (e: Exception) {
            null
        }
    }

    fun phi(x: Double, aVal: Double): Double? {
        // Разные формулы для положительных и отрицательных a
        return if (aVal >= 0) {
            if (x <= 0 || x + aVal < 0) null
            else 1.0 / sqrt(x + aVal)
        } else {
            // Для отрицательных a используем преобразованное уравнение
            if (x <= 0 || x + aVal < 0) null
            else 1.0 / (x * x) - aVal
        }
    }
}