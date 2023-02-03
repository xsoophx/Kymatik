package cc.suffro.bpmanalyzer

import javafx.application.Application
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
        stage.apply {
            title = "BPM Analyzer"
            show()
        }
    }
}
