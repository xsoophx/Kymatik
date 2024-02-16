package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CombFilterAnalyzerTestImpl
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.WavWriter
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val speedAdjusterTestModule = module {
    factoryOf(::SpeedAdjuster)
    singleOf(::CombFilterAnalyzerTestImpl) {
        bind<BpmAnalyzer>()
    }
    single<FileReader<Wav>> { WAVReader }
    single<FileWriter<Wav>> { WavWriter }
}
