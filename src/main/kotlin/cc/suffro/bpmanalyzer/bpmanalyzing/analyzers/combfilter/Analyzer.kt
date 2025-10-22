package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import org.koin.core.component.KoinComponent
import java.nio.file.Path

interface Analyzer<T, R> : KoinComponent {
    fun analyze(data: T): R

    fun analyze(
        data: T,
        params: AnalyzerParams,
    ): R

    fun getTransformedSamples(
        samples: List<Double>,
        sampleSizeToAnalyze: Int,
        data: T,
        startingSample: Int,
    ): List<Double>

    fun getPathAndAnalyze(path: String): R

    fun getPathAndAnalyze(path: Path): R
}
