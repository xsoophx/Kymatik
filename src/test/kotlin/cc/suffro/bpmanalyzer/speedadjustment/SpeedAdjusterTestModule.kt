package cc.suffro.bpmanalyzer.speedadjustment

import org.koin.core.qualifier.named
import org.koin.dsl.module

val speedAdjusterTestModule = module {
    single(named("ProdSpeedAdjuster")) { SpeedAdjuster(get(named("ProdImpl"))) }
    single(named("TestSpeedAdjuster")) { SpeedAdjuster(get(named("TestImpl"))) }
}
