package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams

data class StartingPositionCacheAnalyzerParams(
    val bpm: Double,
) : AnalyzerParams<StartingPosition>
