package cc.suffro.fft

import cc.suffro.fft.BpmAnalyzer.getBassFrequencyBins
import cc.suffro.fft.BpmAnalyzer.getDeviations
import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kotlinmath.complex

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BpmAnalyzerTest {

    // TODO: add mocking
    @Test
    fun `should get correct deviations`() {
        //val bpmAnalyzer = BpmAnalyzer.analyze(Path(path))

        val wav = WAVReader.read(Path(path))
        val windows = wav.getWindows(end = 1.0, interval = 0.01).map { window -> window.map { complex(it, 0) } }

        val bassFrequencyBins = FFTProcessor(windows).process(wav.sampleRate).getBassFrequencyBins()
        val deviations = bassFrequencyBins.toList().getDeviations()

        assertEquals(expected = bassFrequencyBins.count() - 1, actual = deviations.size)

    }

    companion object {
        private const val path = "src/test/resources/440.wav"
    }
}