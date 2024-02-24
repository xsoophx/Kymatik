package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val filterTestModule =
    module {
        singleOf(::CombFilter)
    }
