package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CombFilterAnalyzerTestImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val speedAdjusterTestModule = module {
    factoryOf(::SpeedAdjuster)
    singleOf(::CombFilterAnalyzerTestImpl) {
        bind<BpmAnalyzer>()
    }
}
