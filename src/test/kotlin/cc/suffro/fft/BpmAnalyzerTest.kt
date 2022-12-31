package cc.suffro.fft

import cc.suffro.fft.BpmAnalyzer.getBassFrequencyBins
import cc.suffro.fft.data.FFTData
import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BpmAnalyzerTest {

    // TODO: add mocking
    private val firstElement: FFTData by lazy {
        val wav = WAVReader.read(Path(path))
        val windows = wav.getWindows(start = 0.9, end = 1.0, interval = 0.01)
        val fftData = FFTProcessor(windows).process(wav.sampleRate)
        fftData.first()
    }

    // TODO: testing private function for now, delete later
    // 44000Hz/1024S = 42,967 Hz per bin
    @Test
    fun `should get correct bass frequency bins`() {
        val lowerFrequencyBin = firstElement.binIndexOf(LOWER_FREQUENCY_BOUND)
        val higherFrequencyBin = firstElement.binIndexOf(HIGHER_FREQUENCY_BOUND)

        assertEquals(
            expected = higherFrequencyBin - lowerFrequencyBin + 1,
            actual = sequenceOf(firstElement).getBassFrequencyBins(0.1).first().values.count()
        )
    }

    @Test
    fun `should detect correct frequency for plain kicks`() {
        val result = BpmAnalyzer.analyze(Path("src/test/resources/120bpmkick_60-140Hz.wav"))
        assertEquals(expected = 120.0, actual = result)
    }

    @Test
    fun `should detect correct frequency testTrack`() {
        val result = BpmAnalyzer.analyze(Path("src/test/resources/testTrack.wav"))
        assertEquals(expected = 135.0, actual = result)
    }

    companion object {
        private const val path = "src/test/resources/440.wav"

        private const val LOWER_FREQUENCY_BOUND = 40.0
        private const val HIGHER_FREQUENCY_BOUND = 120.0
    }
}