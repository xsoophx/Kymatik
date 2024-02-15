package cc.suffro.bpmanalyzer.wav

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.inject
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WavWriterTest : BaseTest() {

    private val wavReader by inject<FileReader<Wav>>()

    @AfterEach
    fun tearDown() {
        Path.of("src/test/resources/120bpm_140Hz_copy.wav").toFile().delete()
        Path.of("src/test/resources/220_copy.wav").toFile().delete()
        Path.of("src/test/resources/440_copy.wav").toFile().delete()
    }

    @ParameterizedTest
    @MethodSource("getWavData")
    fun `should write the same wav file as previously read`(path: String) {
        val wav = wavReader.read(Path.of("$path.wav"))
        val pathOfCopy = Path.of("${path}_copy.wav")
        WavWriter.write(pathOfCopy, wav)

        val actual = wavReader.read(pathOfCopy)

        assertEquals(wav.sampleRate, actual.sampleRate)
        assertEquals(wav.trackLength, actual.trackLength)
        assertEquals(wav.fmtChunk, actual.fmtChunk)
        assertEquals(wav.timestampLastSample, actual.timestampLastSample)

        for (i in 0 until wav.fmtChunk.numChannels) {
            assertTrue(wav.dataChunk[i].contentEquals(actual.dataChunk[i]))
        }

        logger.info { "expected : ${wav.dataChunk}" }
        logger.info { "actual : ${actual.dataChunk}" }
    }

    companion object {

        val logger = mu.KotlinLogging.logger {}

        @JvmStatic
        fun getWavData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("src/test/resources/120bpm_140Hz"),
                Arguments.of("src/test/resources/220"),
                Arguments.of("src/test/resources/440")
            )
        }
    }
}
