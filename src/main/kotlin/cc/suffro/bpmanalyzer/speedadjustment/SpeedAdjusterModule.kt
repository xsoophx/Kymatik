package cc.suffro.bpmanalyzer.speedadjustment

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val speedAdjusterModule = module {
    factoryOf(::SpeedAdjuster)
}
