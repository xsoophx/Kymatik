package cc.suffro.bpmanalyzer

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting application")
            launch(Main::class.java)
        }
    }

    override fun start(stage: Stage) {
        val scene = Scene(Group())
        stage.apply {
            setScene(scene)
            isMaximized = true
            title = "BPM Analyzer"
            show()
        }
    }
}
