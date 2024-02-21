package cc.suffro.bpmanalyzer.bpmanalyzing

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.ParameterizedCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPositionAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilter
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val bpmAnalyzingModule =
    module {
        singleOf(::CombFilterCacheAnalyzer) {
            bind<ParameterizedCacheAnalyzer<Wav, TrackInfo>>()
            named("ProdImpl")
        }
        singleOf(::CombFilterAnalyzer) {
            bind<BpmAnalyzer<CombFilter>>()
        }
        singleOf(::StartingPositionAnalyzer) {
            bind<ParameterizedCacheAnalyzer<Wav, StartingPosition>>()
        }
    }
