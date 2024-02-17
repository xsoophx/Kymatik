package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val analyzerTestModule = module {
    singleOf(::CombFilterAnalyzer) {
        bind<BpmAnalyzer>()
        named("ProdImpl")
    }
    singleOf(::CombFilterAnalyzerTestImpl) {
        bind<BpmAnalyzer>()
        named("TestImpl")
    }
    single<CacheAnalyzer>(named("ProdImpl")) { CombFilterCacheAnalyzer(get(named("ProdImpl")), get()) }
    single<CacheAnalyzer>(named("TestImpl")) { CombFilterCacheAnalyzer(get(named("TestImpl")), get()) }
}
