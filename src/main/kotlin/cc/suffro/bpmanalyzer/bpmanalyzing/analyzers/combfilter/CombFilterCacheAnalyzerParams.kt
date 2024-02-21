package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.data.WindowFunction

data class CombFilterCacheAnalyzerParams(
    val start: Double,
    val windowFunction: WindowFunction?,
) : AnalyzerParams<TrackInfo>
