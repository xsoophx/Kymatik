package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerTestImpl
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPositionAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val analyzerTestModule =
    module {
        singleOf(::CombFilterAnalyzer) {
            bind<BpmAnalyzer>()
            named("ProdImpl")
        }
        singleOf(::CombFilterAnalyzerTestImpl) {
            bind<BpmAnalyzer>()
            named("TestImpl")
        }
        single<ParameterizedCacheAnalyzer<Wav, TrackInfo>>(named("ProdImpl")) {
            CombFilterCacheAnalyzer(
                get(named("ProdImpl")),
                get(),
            )
        }
        single<ParameterizedCacheAnalyzer<Wav, TrackInfo>>(named("TestImpl")) {
            CombFilterCacheAnalyzer(
                get(named("TestImpl")),
                get(),
            )
        }
        singleOf(::StartingPositionAnalyzer)
    }
