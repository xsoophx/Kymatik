package cc.suffro.bpmanalyzer.trackmerge

import cc.suffro.bpmanalyzer.TrackMerger
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilter
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val trackMergerTestModule =
    module {
        singleOf(::CombFilterCacheAnalyzer) {
            bind<CacheAnalyzer<Wav, TrackInfo, CombFilter>>()
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
