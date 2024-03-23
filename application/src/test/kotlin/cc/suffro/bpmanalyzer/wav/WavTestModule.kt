package cc.suffro.bpmanalyzer.wav

import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.dsl.module

val wavTestModule =
    module {
        single<FileReader<Wav>> { WAVReader }
        single<FileWriter<Wav>> { WavWriter }
    }
