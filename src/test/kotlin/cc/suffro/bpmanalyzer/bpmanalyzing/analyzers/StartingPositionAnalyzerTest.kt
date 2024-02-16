package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.wav.WAVReader
import org.junit.jupiter.api.Test

class StartingPositionAnalyzerTest {

    private val wavReader = WAVReader

    @Test
    fun `should detect correct start position for plain kicks`() {
        val wav = wavReader.read("src/test/resources/tracks/120bpm_140Hz.wav")
        val result = StartingPositionAnalyzer().analyze(wav)
        assertNearlyEquals(
            expected = StartingPosition(firstSample = 1014, startInSec = 0.023),
            actual = result,
            sampleDistance = 20,
            secondDistance = 0.01
        )
    }
}
