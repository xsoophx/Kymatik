package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FftSampleSize
import cc.suffro.bpmanalyzer.fft.data.FrequencyDomainWindow
import cc.suffro.bpmanalyzer.fft.data.hanningFunction
import cc.suffro.bpmanalyzer.ui.MainWindow
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import cc.suffro.bpmanalyzer.wav.data.WindowProcessingParams
import javafx.application.Application
import javafx.scene.Group
import javafx.stage.Stage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Main : Application() {
    override fun start(stage: Stage?) {
        val path = this.parameters.raw.first()
        val wav = WAVReader.read(path)

        val params = WindowProcessingParams(end = 10.0, interval = 0.01, numSamples = FftSampleSize.TWO_THOUSAND)
        val data = wav.getFrequencies(params).scaleMagnitudes().interpolate()

        val root = Group()
        MainWindow().show(root, stage, data)
    }

    private fun Wav.getFrequencies(params: WindowProcessingParams): List<FrequencyDomainWindow> {
        val frequencyWindows = FFTProcessor().processWav(this, params, ::hanningFunction)
        return frequencyWindows.toList()
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
