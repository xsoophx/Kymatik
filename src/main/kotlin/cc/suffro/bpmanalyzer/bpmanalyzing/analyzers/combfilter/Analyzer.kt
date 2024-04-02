package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import java.nio.file.Path

interface Analyzer<T, R> {
    fun analyze(data: T): R

    fun analyze(
        data: T,
        params: AnalyzerParams,
    ): R

    fun getPathAndAnalyze(path: String): R

    fun getPathAndAnalyze(path: Path): R
}
