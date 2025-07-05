package fokin.aleksandr.application.ui

import kotlin.math.*

object Equation {
    // Функция для уравнения: √(x+a) - 1/x
    fun f(x: Double, aVal: Double): Double? {
        if (x <= 0 || x + aVal < 0) return null
        return sqrt(x + aVal) - 1.0 / x
    }

    // Функция для метода итераций: x = 1 / √(x+a)
    fun phi(x: Double, aVal: Double): Double? {
        if (x + aVal <= 0) return null
        return 1.0 / sqrt(x + aVal)
    }
}