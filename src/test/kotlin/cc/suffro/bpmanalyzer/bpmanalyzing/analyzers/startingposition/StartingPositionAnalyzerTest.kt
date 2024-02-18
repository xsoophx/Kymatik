package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.assertNearlyEquals
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.inject
import java.util.stream.Stream

class StartingPositionAnalyzerTest : BaseTest() {
    private val wavReader by inject<FileReader<Wav>>()

    private val startingPositionAnalyzer by inject<StartingPositionAnalyzer>()

    @ParameterizedTest
    @MethodSource("getTracksWithBpm")
    fun `should detect correct start position for plain kicks`(
        path: String,
        firstSample: Int,
        startInSec: Double,
    ) {
        val wav = wavReader.read(path)
        val result = startingPositionAnalyzer.analyze(wav)
        assertNearlyEquals(
            expected = StartingPosition(firstSample = firstSample, startInSec = startInSec),
            actual = result,
            sampleDistance = 20,
            secondDistance = 0.01,
        )
    }

    companion object {
        @JvmStatic
        private fun getTracksWithBpm() =
            Stream.of(
                Arguments.of("src/test/resources/tracks/120bpm_140Hz.wav", 1014, 0.023),
                Arguments.of("src/test/resources/tracks/Jan Vercauteren - Dysfunction.wav", 200, 0.0045351473922902496),
                Arguments.of("src/test/resources/tracks/Mark Terre - Gravity Zero.wav", 500, 0.011337868480725623),
            )
    }
}
