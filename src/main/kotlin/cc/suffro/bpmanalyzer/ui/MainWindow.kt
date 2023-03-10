package cc.suffro.bpmanalyzer.ui

import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.wav.WAVReader
import io.data2viz.color.Colors
import io.data2viz.scale.Scales
import io.data2viz.viz.JFxVizRenderer
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage

class MainWindow(path: String) {
    private val wav = WAVReader.read(path)
    private val data = getFrequencies()
    private val barHeight = HEIGHT / data.size.toDouble()

    private fun getFrequencies(channel: Int = 0, begin: Int = 0): List<Double> {
        return FFTProcessor().process(wav, channel, begin).magnitudes
    }

    private fun xScale(value: Double, data: List<Double> = this.data) = xPosition(data)(value)

    private fun xPosition(data: List<Double>) = Scales.Continuous.linear {
        domain = listOf(.0, data.max())
        range = listOf(.0, WIDTH - 2 * PADDING)
    }

    private fun createBarChart(data: List<Double>): Viz {
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

    fun show(root: Group, stage: Stage?) {
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

    companion object {
        private const val WIDTH = 700.0
        private const val HEIGHT = 1500.0
        private const val PADDING = 1.0
    }
}
