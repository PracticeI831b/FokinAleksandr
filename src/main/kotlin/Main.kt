import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fokin.aleksandr.application.ui.App

fun main() = application {
    System.setProperty("java.awt.headless", "false")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Решение уравнения √(x+a) = 1/x"
    ) {
        App()
    }
}