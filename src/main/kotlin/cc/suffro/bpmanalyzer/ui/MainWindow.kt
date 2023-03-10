package cc.suffro.bpmanalyzer.ui

import io.data2viz.color.Colors
import io.data2viz.scale.Scales
import io.data2viz.viz.JFxVizRenderer
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage

object MainWindow {
    private const val WIDTH = 700.0
    private const val HEIGHT = 1500.0
    private const val PADDING = 1.0

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

    fun show(root: Group, stage: Stage?, data: List<Double>) {
        stage?.apply {
            title = "BPM Analyzer"
            scene = Scene(root, WIDTH, HEIGHT)

            val canvas = Canvas(WIDTH, HEIGHT)
            root.children.add(canvas)

            val viz = createBarChart(data)
            renderVizOnCanvas(viz, canvas)
            show()
        }
    }
}
