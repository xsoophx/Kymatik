package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.filters.DifferentialRectifier.process
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.math.pow
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DifferentialRectifierTest {
    @Test
    fun `should set negatives of sequence to zero`() {
        val signal = (0 until 10).asSequence().map { k -> (-1.0).pow(k) / (2 * k + 1) }
        val actual = process(signal)

        assertTrue(actual.none { it < 0 })
        assertTrue(actual.filterIndexed { index, _ -> index and 1 == 0 }.all { it == 0.0 })
    }
}
