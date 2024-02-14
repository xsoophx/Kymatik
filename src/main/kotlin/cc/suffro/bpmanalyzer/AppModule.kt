package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.wav.FileReader
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlinx.cli.ArgParser
import org.koin.dsl.module

val appModule = module {
    single { ArgParser("BPMAnalyzer") }
    single<FileReader<Wav>> { WAVReader }
}
