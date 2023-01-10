package cc.suffro.fft.bpm_analyzing

import cc.suffro.fft.assertNearlyEquals
import cc.suffro.fft.bpm_analzying.BpmAnalyzer
import cc.suffro.fft.wav.WAVReader
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BpmAnalyzerTest {

    private val wavReader = WAVReader

    @Test
    fun `should detect correct BPM for plain kicks`() {
        val wav = wavReader.read(Path("src/test/resources/120bpmkick_60-140Hz.wav"))
        val result = BpmAnalyzer().analyzeByPeakDistance(wav)
        assertEquals(expected = 120.0, actual = result)
    }

    @ParameterizedTest
    @MethodSource("getTracks")
    fun `should detect correct BPM for test tracks in 20 seconds`(trackName: String, bpm: Double) {
        val wav = wavReader.read(Path("src/test/resources/tracks/$trackName"))
        val result = BpmAnalyzer().analyzeByPeakDistance(wav, end = 20.0)
        assertNearlyEquals(expected = bpm, actual = result)
    }

    @ParameterizedTest
    @MethodSource("getTracks")
    fun `should detect correct BPM for test tracks in 10 seconds`(trackName: String, bpm: Double) {
        val wav = wavReader.read(Path("src/test/resources/tracks/$trackName"))
        val result = BpmAnalyzer().analyzeByPeakDistance(wav, end = 10.0)
        assertNearlyEquals(expected = bpm, actual = result)
    }

    @ParameterizedTest
    @MethodSource("getTracks")
    fun `should detect correct BPM for test tracks in 5 seconds`(trackName: String, bpm: Double) {
        val wav = wavReader.read(Path("src/test/resources/tracks/$trackName"))
        val result = BpmAnalyzer().analyzeByPeakDistance(wav, end = 5.0)
        assertNearlyEquals(expected = bpm, actual = result)
    }

    @ParameterizedTest
    @MethodSource("getTracks")
    fun `should detect correct bpm with Filterbank algorithm`(trackName: String, bpm: Double){
        val wav = wavReader.read(Path("src/test/resources/tracks/$trackName"))
        val result = BpmAnalyzer().analyzeByEnergyLevels(wav, end = 5.0)
        assertNearlyEquals(expected = bpm, actual = result)
    }

    companion object {
        @JvmStatic
        private fun getTracks() = Stream.of(
            Arguments.of("HXIST - Tier.wav", 147.0),
            Arguments.of("Jan Vercauteren - Dysfunction.wav", 149.0),
            Arguments.of("Lucinee, MRD - Bang Juice (MRD Remix).wav", 144.0),
            Arguments.of("Mark Terre - Gravity Zero.wav", 152.0),
            Arguments.of("Peter Van Hoesen - Vertical Vertigo.wav", 135.0),
        )
    }
}