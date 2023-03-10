package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.ui.MainWindow
import javafx.application.Application
import javafx.scene.Group
import javafx.stage.Stage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Main : Application() {
    override fun start(stage: Stage?) {
        val root = Group()
        val path = this.parameters.raw.first()
        MainWindow(path).show(root, stage)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            args.forEach { logger.info { it } }
            println("Starting application")
            launch(Main::class.java, args[0])
        }
    }
}
