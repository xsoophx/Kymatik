package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val filterModule =
    module {
        singleOf(::CombFilter)
        singleOf(::CombFilterOperationsImpl) {
            bind<CombFilterOperations>()
        }
    }
