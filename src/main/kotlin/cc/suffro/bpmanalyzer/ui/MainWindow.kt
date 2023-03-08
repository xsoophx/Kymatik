package cc.suffro.bpmanalyzer.ui

import io.data2viz.color.Colors
import io.data2viz.scale.Scales
import io.data2viz.viz.JFxVizRenderer
import io.data2viz.viz.TextHAlign
import io.data2viz.viz.TextVAlign
import io.data2viz.viz.viz
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage

object MainWindow {
    private const val WIDTH = 700.0
    private const val HEIGHT = 500.0
    private val data = listOf(1, 4, 14, 18, 34, 64)
    private const val barHeight = 14.0
    private const val padding = 2.0

    private fun xScale(value: Int) = xPosition(value.toDouble())

    val xPosition = Scales.Continuous.linear {
        domain = listOf(.0, data.max().toDouble())
        range = listOf(.0, WIDTH - 2 * padding)
    }

    fun show(root: Group, stage: Stage?) {
        stage?.apply {
            title = "BPM Analyzer"
            scene = Scene(root, WIDTH, HEIGHT)

            val canvas = Canvas(WIDTH, HEIGHT)
            root.children.add(canvas)

            val viz = viz {
                data.forEachIndexed { index, value ->
                    group {
                        transform {
                            translate(
                                x = padding,
                                y = padding + index * (padding + barHeight) + HEIGHT / 2
                            )
                        }
                        rect {
                            width = xScale(value)
                            height = barHeight
                            fill = Colors.Web.steelblue
                        }
                        text {
                            textContent = value.toString()
                            hAlign = TextHAlign.RIGHT
                            vAlign = TextVAlign.HANGING
                            x = xScale(value) - 2.0
                            y = 1.5
                            textColor = Colors.Web.white
                            fontSize = 10.0
                        }
                    }
                }
            }

            JFxVizRenderer(canvas, viz)
            viz.render()

            show()
        }
    }
}