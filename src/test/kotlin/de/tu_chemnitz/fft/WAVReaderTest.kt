package de.tu_chemnitz.fft

import de.tu_chemnitz.fft.WAVReader.readSamplesAt
import de.tu_chemnitz.fft.data.AudioFormat
import de.tu_chemnitz.fft.data.FmtChunk
import de.tu_chemnitz.fft.data.Wav
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WAVReaderTest {

    @ParameterizedTest
    @MethodSource("getWavData")
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

    @Test
    fun `reads correct Samples from wav file`() {
        val wav = WAVReader.read(Path("src/test/resources/440.wav"))
        val fftData = FFTSequence(sequenceOf(wav.readSamplesAt(45675, 1024))).process(samplingRate = wav.fmtChunk.sampleRate)
        val magnitudes = fftData.first().magnitudes
        val bin = fftData.first().binIndexOf(440.0)
        val maximumIndex = magnitudes.indexOf(magnitudes.max())

        assertEquals(
            expected = sequenceOf(0.0), actual = wav.readSamplesAt(0, 1024)
        )
    }

    companion object {
        @JvmStatic
        private fun getWavData() = Stream.of(
            Arguments.of(
                "src/test/resources/sinus.wav",
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
    }
}