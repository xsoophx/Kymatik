package cc.suffro.fft

import cc.suffro.fft.data.AudioFormat
import cc.suffro.fft.data.FmtChunk
import cc.suffro.fft.data.Wav
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(FFT)
class WAVReaderTest {

    @ParameterizedTest
    @MethodSource("getWavDataWithFmt")
    fun `reads correct header and data`(path: String, fmtChunk: FmtChunk) {
        val actual = WAVReader.read(Path(path))

        assertEquals(
            expected = Wav(
                filePath = Path(path),
                fmtChunk = fmtChunk,
                dataChunk = actual.dataChunk
            ), actual = actual
        )
    }

    @Test
    fun `should throw parsing exception`() {
        // TODO: not yet implemented
    }

    @ParameterizedTest
    @MethodSource("getWavDataWithFrequency")
    fun `reads correct Samples from wav file`(path: String, frequency: Double) {
        val wav = WAVReader.read(Path(path))
        val samples = wav.getSamples(channel = 0, begin = 0, length = 1024)
        val fftData = FFTSequence(sequenceOf(samples)).process(samplingRate = wav.fmtChunk.sampleRate)
        val magnitudes = fftData.first().magnitudes

        assertEquals(
            expected = fftData.first().binIndexOf(frequency), actual = magnitudes.indexOf(magnitudes.max())
        )
    }

    companion object {
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
            Arguments.of(
                "src/test/resources/440.wav", 440.0
            ),
            Arguments.of(
                "src/test/resources/220.wav", 220.0
            )
        )
    }
}