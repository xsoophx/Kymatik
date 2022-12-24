package de.tu_chemnitz.fft

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WAVReaderTest {

    @Test
    fun `reads correct header`() {
        val actual = WAVReader.read(Path("src/test/resources/sinus.wav"))

        assertEquals(
            expected = Wav(
                filePath = Path("src/test/resources/sinus.wav"),
                chunkSize = 654006u,
                subChunkOneSize = 16u,
                audioFormat = AudioFormat.PCM,
                numChannels = 2u,
                sampleRate = 44100u,
                byteRate = 176400u,
                blockAlign = 4u,
                bitsPerSample = 16u,
                subChunkTwoSize = 653868u,
            ), actual = actual
        )
    }
}