package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.BaseTest
import cc.suffro.bpmanalyzer.wav.data.AudioFormat
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

    @Test
    fun `should create correct number of samples`() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
        val wav = Wav(
            filePath = Path.of("dummy.wav"),
            fmtChunk = FmtChunk(1, 1, AudioFormat.PCM, 1, 1, 1, 1, 1, 1),
            dataChunk = arrayOf(data)
        )

        val result = speedAdjuster.changeTo(wav, 120.0)
        assertEquals(8, result.size)
    }
}
