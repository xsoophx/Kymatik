package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.wav.data.Wav

interface BpmAnalyzer<T> {
    fun analyze(wav: Wav): Bpm

    fun analyze(
        wav: Wav,
        analyzerParams: AnalyzerParams<T>,
    ): Bpm
}
