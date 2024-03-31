package cc.suffro.bpmanalyzer.fft

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.Interval
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.WindowProcessingParams
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.inject
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpectralLeakageTest : BaseTest() {
    private val fftProcessor by inject<FFTProcessor>()

    @Test
    fun `should not detect frequencies between sine signals`() {
        val wav = WAVReader.read(SINE_ON_OFF)
        val sineInterval = 60.0 / 130.0
        val windowSize = 0.01
        val params = WindowProcessingParams(end = sineInterval * 4, interval = windowSize)
        val fftData = fftProcessor.processWav(wav, params)

        val silenceIntervals =
            (0 until 8).asSequence()
                .map { index -> (sineInterval / 2.0) * index }
                .chunked(2) {
                    val start = it[1]
                    Interval(lowerBound = start, upperBound = start + sineInterval / 2)
                }
                .toList()

        val silenceSamples =
            silenceIntervals.map { interval ->
                fftData.filter {
                    it.startingTime >= interval.lowerBound && it.startingTime + windowSize < interval.upperBound
                }.toList()
            }

        // TODO: change this value, 0.15 is too big
        silenceSamples.forEachIndexed { outerIndex, windows ->
            windows.forEachIndexed { windowIndex, window ->
                window.magnitudes.forEachIndexed { innerIndex, magnitude ->
                    assertTrue(
                        magnitudeErrorMessage(
                            innerIndex,
                            outerIndex,
                            windowIndex,
                            magnitude,
                        ),
                    ) { magnitude < 0.15 }
                }
            }
        }
    }

    private fun magnitudeErrorMessage(
        innerIndex: Int,
        outerIndex: Int,
        windowIndex: Int,
        magnitude: Double,
    ): String {
        return "Magnitude at index " +
            "$innerIndex (interval $outerIndex, windowIndex $windowIndex) with value $magnitude was too big."
    }

    companion object {
        // 60s / 130bpm = 0.46153846...s/bpm
        private const val SINE_ON_OFF = "src/test/resources/samples/C5_on_off.wav"
    }
}
