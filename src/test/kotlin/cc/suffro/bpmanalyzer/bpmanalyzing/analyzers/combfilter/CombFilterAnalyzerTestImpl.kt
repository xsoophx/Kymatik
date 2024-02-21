package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilter
import cc.suffro.bpmanalyzer.wav.data.Wav

class CombFilterAnalyzerTestImpl : BpmAnalyzer<CombFilter> {
    override fun analyze(wav: Wav): Bpm {
        return 100.0
    }

    override fun analyze(
        wav: Wav,
        analyzerParams: AnalyzerParams<CombFilter>,
    ): Bpm {
        return 100.0
    }
}
