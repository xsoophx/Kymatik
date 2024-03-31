package cc.suffro.bpmanalyzer.fft

import org.koin.dsl.module

val fftModule =
    module {
        single { FFTProcessor }
    }
