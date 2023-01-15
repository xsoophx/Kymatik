package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.bpmanalyzing.data.Signal
import kotlin.math.max

object DifferentialRectifier {
    fun process(signals: Sequence<Signal>): Sequence<Signal> {
        return signals.map { signal ->
            val differentials = signal.zipWithNext()
                .map { (current, next) -> next - current }
                .map { d -> max(0.0, d) }
            differentials + 0.0
        }
    }
}
