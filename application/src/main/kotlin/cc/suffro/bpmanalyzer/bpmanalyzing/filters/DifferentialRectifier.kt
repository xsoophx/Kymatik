package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import kotlin.math.max

object DifferentialRectifier {
    fun process(signal: Signal): Signal {
        val differentials =
            signal.zipWithNext()
                .map { (current, next) -> next - current }
                .map { d -> max(0.0, d) }

        return differentials + 0.0
    }
}
