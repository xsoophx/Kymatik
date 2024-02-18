package cc.suffro.bpmanalyzer.speedadjustment

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val speedAdjusterModule =
    module {
        singleOf(::SpeedAdjuster)
    }
