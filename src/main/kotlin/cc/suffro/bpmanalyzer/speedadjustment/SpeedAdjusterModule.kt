package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CombFilterCacheAnalyzer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val speedAdjusterModule = module {
    factoryOf(::SpeedAdjuster)
    singleOf(::CombFilterCacheAnalyzer) {
        bind<CacheAnalyzer>()
    }
}
