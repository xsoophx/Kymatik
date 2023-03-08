package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.ui.MainWindow
import javafx.application.Application
import javafx.scene.Group
import javafx.stage.Stage

class Main : Application() {
    override fun start(stage: Stage?) {
        val root = Group()
        MainWindow.show(root, stage)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting application")
            launch(Main::class.java)
        }
    }
}
