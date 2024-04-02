package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.component.KoinComponent
import java.nio.file.Path

interface BpmAnalyzer : KoinComponent, Analyzer<Wav, Bpm> {
    override fun analyze(data: Wav): Bpm

    override fun analyze(
        data: Wav,
        params: AnalyzerParams,
    ): Bpm

    override fun getPathAndAnalyze(path: String): Bpm

    override fun getPathAndAnalyze(path: Path): Bpm

    fun analyze(
        path: String,
        analyzerParams: AnalyzerParams,
    ): Bpm
}
