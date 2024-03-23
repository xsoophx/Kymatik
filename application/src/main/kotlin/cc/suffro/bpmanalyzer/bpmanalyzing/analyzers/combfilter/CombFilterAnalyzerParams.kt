package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.fft.data.WindowFunction

data class CombFilterAnalyzerParams(
    val start: Double = 0.0,
    val windowFunction: WindowFunction? = null,
    val minimumBpm: Double = MINIMUM_BPM,
    val maximumBpm: Double = MAXIMUM_BPM,
    val stepSize: Double = STEP_SIZE,
    val refinementParams: RefinementParams? = RefinementParams(),
) : AnalyzerParams {
    companion object {
        const val MINIMUM_BPM = 60.0
        const val MAXIMUM_BPM = 220.0

        const val STEP_SIZE = 1.0
    }
}

data class RefinementParams(
    val deviationBpm: Double = 5.0,
    val stepSize: Double = 0.1,
)
