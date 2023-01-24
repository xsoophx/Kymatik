package cc.suffro.fft.wav

import cc.suffro.fft.FFT
import cc.suffro.fft.fft.FFTProcessor
import cc.suffro.fft.wav.data.AudioFormat
import cc.suffro.fft.wav.data.FmtChunk
import cc.suffro.fft.wav.data.Wav
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(FFT)
class WAVReaderTest {

    private val wavReader = WAVReader

    @ParameterizedTest
    @MethodSource("getWavDataWithFmt")
    fun `should read correct header and data`(path: String, fmtChunk: FmtChunk) {
        val actual = wavReader.read(Path(path))

        assertEquals(
            expected = Wav(
                filePath = Path(path),
                fmtChunk = fmtChunk,
                dataChunk = actual.dataChunk
            ),
            actual = actual
        )
    }

    @Test
    fun `should throw parsing exception`() {
        // TODO: not implemented yet
    }

    @Test
    fun `should read correct Samples if 8-bit PCM`() {
        // TODO: not implemented yet
    }

    @ParameterizedTest
    @MethodSource("getWavDataWithFrequency")
    fun `should read correct Samples from wav file`(path: String, frequency: Double) {
        val wav = wavReader.read(Path(path))
        val samples = wav.getWindowContent(channel = 0, begin = 0)
        val fftData = FFTProcessor().process(sequenceOf(samples), samplingRate = wav.sampleRate)
        val magnitudes = fftData.first().magnitudes

        assertEquals(
            expected = fftData.first().binIndexOf(frequency),
            actual = magnitudes.indexOf(magnitudes.maxOf { it })
        )
    }

    @ParameterizedTest
    @CsvSource(
        "0.0, 1.0, 0.01, 100",
        "0.0, 4.0, 0.01, 400",
        "1.2, 3.9, 0.15, 18"
    )
    fun `should return correct amount of windows of samples`(
        start: Double,
        end: Double,
        interval: Double,
        expectedWindows: Int
    ) {
        val wav = wavReader.read(Path("src/test/resources/440.wav"))
        val actual = wav.getWindows(start = start, end = end, interval, 0, DEFAULT_SAMPLE_NUMBER)

        assertEquals(expected = expectedWindows, actual = actual.count())
    }

    @Test
    fun `should handle track length as end correctly`() {
        val wav = wavReader.read(Path("src/test/resources/440.wav"))
        val windowTime = DEFAULT_SAMPLE_NUMBER.toDouble() / wav.sampleRate
        val actual = wav.getWindows(start = wav.trackLength - windowTime, end = wav.trackLength, interval = windowTime)

        assertEquals(expected = 0, actual = actual.count())
    }

    companion object {
        private const val DEFAULT_SAMPLE_NUMBER = 1024

        @JvmStatic
        private fun getWavDataWithFmt() = Stream.of(
            Arguments.of(
                "src/test/resources/220.wav",
                FmtChunk(
                    riffChunkSize = 654006,
                    fmtChunkSize = 16,
                    audioFormat = AudioFormat.PCM,
                    numChannels = 2,
                    sampleRate = 44100,
                    byteRate = 176400,
                    blockAlign = 4,
                    bitsPerSample = 16,
                    dataChunkSize = 653868
                )
            ),
            Arguments.of(
                "src/test/resources/440.wav",
                FmtChunk(
                    riffChunkSize = 880110,
                    fmtChunkSize = 16,
                    audioFormat = AudioFormat.PCM,
                    numChannels = 1,
                    sampleRate = 44000,
                    byteRate = 88000,
                    blockAlign = 2,
                    bitsPerSample = 16,
                    dataChunkSize = 880000
                )
            )
        )

        @JvmStatic
        private fun getWavDataWithFrequency() = Stream.of(
            Arguments.of("src/test/resources/440.wav", 440.0),
            Arguments.of("src/test/resources/220.wav", 220.0)
        )
    }
}
