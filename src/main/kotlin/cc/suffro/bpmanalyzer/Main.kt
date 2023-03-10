package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FftSampleSize
import cc.suffro.bpmanalyzer.ui.MainWindow
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import cc.suffro.bpmanalyzer.wav.data.WindowProcessingParams
import javafx.application.Application
import javafx.scene.Group
import javafx.stage.Stage
import mu.KotlinLogging
import java.util.Timer
import kotlin.concurrent.schedule

private val logger = KotlinLogging.logger {}

class Main : Application() {
    override fun start(stage: Stage?) {
        val path = this.parameters.raw.first()
        val wav = WAVReader.read(path)

        // assuming 44100Hz sampling frequency, 2048Samples per fft -> 46.44 ms
        // 40ms window steps -> two windows cover 86.44ms
        val params = WindowProcessingParams(end = 10.0, interval = 0.04, numSamples = FftSampleSize.TWO_THOUSAND)
        val data = wav.getFrequencies(params)

        var count = 0
        Timer("ShowData").schedule(500) {
            showData(stage, data.toList()[count++])
        }
    }

    private fun showData(stage: Stage?, data: List<Double>) {
        val root = Group()
        MainWindow.show(root, stage, data)
    }

    private fun Wav.getFrequencies(params: WindowProcessingParams): List<List<Double>> {
        val fftData = FFTProcessor().process(this, params)
        return fftData.map { it.magnitudes }.toList()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            args.forEach { logger.info { it } }
            println("Starting application")

            try {
                launch(Main::class.java, args.first())
            } catch (e: NoSuchElementException) {
                println("Please provide a path to your audio file.")
            }
        }
    }
}
