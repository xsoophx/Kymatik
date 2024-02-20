package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.analyzerTestModule
import cc.suffro.bpmanalyzer.database.databaseTestModule
import cc.suffro.bpmanalyzer.fft.fftTestModule
import cc.suffro.bpmanalyzer.speedadjustment.speedAdjusterTestModule
import cc.suffro.bpmanalyzer.trackmerge.trackMergerModule
import cc.suffro.bpmanalyzer.wav.wavTestModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.logger.Level
import org.koin.test.KoinTest

open class BaseTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                printLogger(Level.DEBUG)
                modules(
                    databaseTestModule,
                    speedAdjusterTestModule,
                    wavTestModule,
                    fftTestModule,
                    analyzerTestModule,
                    trackMergerModule,
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }
}
