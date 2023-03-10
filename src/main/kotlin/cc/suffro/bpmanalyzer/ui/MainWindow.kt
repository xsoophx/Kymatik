package cc.suffro.bpmanalyzer.ui

import cc.suffro.bpmanalyzer.fft.data.FrequencyDomainWindow
import io.data2viz.color.Colors
import io.data2viz.scale.Scales
import io.data2viz.viz.JFxVizRenderer
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

class MainWindow : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.JavaFx

    private fun xScale(value: Double, data: List<Double>) = xPosition(data)(value)

    private fun xPosition(data: List<Double>) = Scales.Continuous.linear {
        domain = listOf(.0, data.max())
        range = listOf(.0, WIDTH - 2 * PADDING)
    }

    private fun createBarChart(data: List<Double>): Viz {
        val barHeight = HEIGHT / data.size.toDouble()

        return viz {
            data.forEachIndexed { index, value ->
                group {
                    transform {
                        translate(
                            x = PADDING,
                            y = PADDING + index * (PADDING + barHeight)
                        )
                    }
                    rect {
                        width = xScale(value, data)
                        height = barHeight
                        fill = Colors.Web.steelblue
                    }
                }
            }
        }
    }

    private fun renderVizOnCanvas(viz: Viz, canvas: Canvas) {
        JFxVizRenderer(canvas, viz)
        viz.render()
    }

    private fun setup(canvas: Canvas, data: List<FrequencyDomainWindow>) =
        launch(Dispatchers.JavaFx) {
            val start = System.currentTimeMillis()
            repeat(data.size) { i ->
                delay(20)
                val timeSinceStart = System.currentTimeMillis() - start
                logger.info { "Time since start: $timeSinceStart." }

                if (timeSinceStart >= data[i].startingTime * 1000) {
                    val viz = createBarChart(data[i].magnitudes)
                    renderVizOnCanvas(viz, canvas)
                    logger.info { "Done with sample $i at $timeSinceStart." }
                }
            }
        }

    fun show(root: Group, stage: Stage?, data: List<FrequencyDomainWindow>) {
        stage?.apply {
            title = "BPM Analyzer"
            scene = Scene(root, WIDTH, HEIGHT)

            val canvas = Canvas(WIDTH, HEIGHT)
            root.children.add(canvas)
            show()
            setup(canvas, data)
        }
    }

    companion object {
        private const val WIDTH = 700.0
        private const val HEIGHT = 1500.0
        private const val PADDING = 1.0
    }
}
