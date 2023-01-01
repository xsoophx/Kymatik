package cc.suffro.fft

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BpmAnalyzerTest {

    @Test
    fun `should detect correct BPM for plain kicks`() {
        val result = BpmAnalyzer.analyze(Path("src/test/resources/120bpmkick_60-140Hz.wav"))
        assertEquals(expected = 120.0, actual = result)
    }

    @ParameterizedTest
    @ValueSource(ints = [20, 10, 5])
    fun `should detect correct BPM for testTrack`(endTime: Int) {
        val result = BpmAnalyzer.analyze(path = Path("src/test/resources/testTrack.wav"), end = endTime.toDouble())
        assertNearlyEquals(expected = 135.0, actual = result)
    }
}