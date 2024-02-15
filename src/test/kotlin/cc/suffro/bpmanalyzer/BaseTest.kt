package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.speedadjustment.speedAdjusterTestModule
import cc.suffro.bpmanalyzer.wav.wavTestModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest

open class BaseTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(speedAdjusterTestModule, wavTestModule)
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }
}
