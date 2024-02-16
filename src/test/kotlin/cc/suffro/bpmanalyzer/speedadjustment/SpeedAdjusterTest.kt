package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.wav.data.AudioFormat
import cc.suffro.bpmanalyzer.wav.data.DataChunk
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.inject
import java.nio.file.Path
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpeedAdjusterTest : BaseTest() {

    private val speedAdjuster by inject<SpeedAdjuster>()

    private val wavReader by inject<FileReader<Wav>>()

    private val wavWriter by inject<FileWriter<Wav>>()

    @Test
    fun `should create correct number of samples`() {
        val data = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)
        val wav = Wav(
            filePath = Path.of("dummy.wav"),
            fmtChunk = FmtChunk(1, 1, AudioFormat.PCM, 1, 1, 1, 1, 1),
            dataChunk = DataChunk(0, arrayOf(data))
        )

        val result = speedAdjuster.changeTo(wav, 120.0)
        assertEquals(11, result.size)
    }

    @Test
    fun `should create correct pitched track`() {
        val wav = wavReader.read("src/test/resources/tracks/120bpm_140Hz.wav")

        val targetBpm = 100.0
        val result = speedAdjuster.changeTo(wav, targetBpm)
        assertEquals((wav.dataChunk.dataChunkSize * (targetBpm / 120.0)).toInt(), result.size)

        wavWriter.write(
            Path.of("src/test/resources/tracks/120bpm_140Hz_copy_test.wav"),
            wav.copy(
                dataChunk = DataChunk(
                    (result.first() * wav.fmtChunk.numChannels * wav.fmtChunk.bitsPerSample / 8).toInt(),
                    arrayOf(result, doubleArrayOf())
                )
            )
        )
    }
}
