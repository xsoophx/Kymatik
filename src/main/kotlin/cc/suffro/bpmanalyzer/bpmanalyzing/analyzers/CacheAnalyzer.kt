package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import java.io.Closeable
import java.nio.file.Path

interface CacheAnalyzer<T, R> : Closeable {
    fun analyze(data: T): R

    fun getAndAnalyze(path: String): R

    fun getAndAnalyze(path: Path): R
}
