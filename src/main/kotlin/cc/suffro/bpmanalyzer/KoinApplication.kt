package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.bpmAnalyzingModule
import cc.suffro.bpmanalyzer.database.databaseModule
import cc.suffro.bpmanalyzer.fft.fftModule
import cc.suffro.bpmanalyzer.speedadjustment.speedAdjusterModule
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

open class KoinApplication : KoinComponent {
    init {
        startKoin {
            modules(appModule, databaseModule, speedAdjusterModule, fftModule, bpmAnalyzingModule)
        }
    }
}
