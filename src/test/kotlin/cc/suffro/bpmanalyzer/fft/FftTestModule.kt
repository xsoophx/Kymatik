package cc.suffro.bpmanalyzer.fft

import org.koin.dsl.module

val fftTestModule =
    module {
        single { FFTProcessor }
    }
