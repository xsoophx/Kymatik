package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import java.nio.file.Path

interface CacheAnalyzer<T, R> {
    fun analyze(data: T): R

    fun analyze(
        data: T,
        params: AnalyzerParams,
    ): R

    fun getPathAndAnalyze(path: String): R

    fun getPathAndAnalyze(path: Path): R
}
