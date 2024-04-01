package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.component.KoinComponent

interface BpmAnalyzer : KoinComponent {
    fun analyze(wav: Wav): Bpm

    fun analyze(
        wav: Wav,
        analyzerParams: AnalyzerParams,
    ): Bpm

    fun analyze(path: String): Bpm

    fun analyze(
        path: String,
        analyzerParams: AnalyzerParams,
    ): Bpm
}
