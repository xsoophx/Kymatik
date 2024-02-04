package cc.suffro.bpmanalyzer

import kotlinx.cli.ArgParser
import org.koin.dsl.module

val appModule = module {
    single { ArgParser("BPMAnalyzer") }
}
