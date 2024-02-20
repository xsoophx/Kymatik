package cc.suffro.bpmanalyzer.trackmerge

import cc.suffro.bpmanalyzer.TrackMerger
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.ParameterizedCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val trackMergerModule =
    module {
        singleOf(::CombFilterCacheAnalyzer) {
            bind<ParameterizedCacheAnalyzer<Wav, TrackInfo>>()
            named("ProdImpl")
        }

        single {
            TrackMerger(
                get(named("ProdImpl")),
                get(named("ProdSpeedAdjuster")),
                get(),
            )
        }
    }
