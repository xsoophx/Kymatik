package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

interface ParameterizedCacheAnalyzer<T, R> : CacheAnalyzer<T, R> {
    fun analyze(
        data: T,
        params: CacheAnalyzerParams<R>,
    ): R
}
