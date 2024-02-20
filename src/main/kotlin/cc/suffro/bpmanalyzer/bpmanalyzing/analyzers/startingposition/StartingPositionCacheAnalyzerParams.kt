package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzerParams

data class StartingPositionCacheAnalyzerParams(
    val bpm: Double,
) : CacheAnalyzerParams<StartingPosition>
