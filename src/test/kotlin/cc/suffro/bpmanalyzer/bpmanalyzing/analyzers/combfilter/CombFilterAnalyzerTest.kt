package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.wav.WAVReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CombFilterAnalyzerTest : BaseTest() {
    private val wavReader = WAVReader

    @Test
    fun `should detect correct BPM for plain kicks`() {
        val wav = wavReader.read("src/test/resources/samples/120bpm_140Hz.wav")
        val result = CombFilterAnalyzer().analyze(wav)
        assertEquals(expected = 120.0, actual = result)
    }

    @Test
    fun `should detect correct BPM for refined values`() {
        val wav = wavReader.read("src/test/resources/samples/120.5bpm_140Hz.wav")
        val analyzerParams = CombFilterAnalyzerParams(refinementParams = RefinementParams())
        val result = CombFilterAnalyzer().analyze(wav, analyzerParams)
        assertEquals(expected = 120.5, actual = result)
    }

    @ParameterizedTest
    @MethodSource("getTracksWithBpm")
    fun `should detect correct BPM for test tracks seconds`(
        trackPath: String,
        bpm: Double,
    ) {
        val wav = wavReader.read(trackPath)
        val result = CombFilterAnalyzer().analyze(wav)
        assertNearlyEquals(expected = bpm, actual = result)
    }
}
