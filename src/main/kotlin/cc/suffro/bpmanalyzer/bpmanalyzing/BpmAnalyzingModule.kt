package cc.suffro.bpmanalyzer.bpmanalyzing

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CombFilterAnalyzer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val bpmAnalyzingModule = module {
    singleOf(::CombFilterAnalyzer) {
        bind<BpmAnalyzer>()
    }
}
