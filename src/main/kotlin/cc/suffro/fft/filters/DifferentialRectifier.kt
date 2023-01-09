package cc.suffro.fft.filters

import kotlin.math.abs
import kotlin.math.max

object DifferentialRectifier {
    fun process(signals: Sequence<Signal>): Sequence<Signal> {
        return signals.map { signal ->
            signal.zipWithNext()
                .map { (current, next) -> next - current }
                .map { d -> max(0.0, abs(d)) }
        }
    }
}