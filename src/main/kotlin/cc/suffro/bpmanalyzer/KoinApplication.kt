package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.database.databaseModule
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

open class KoinApplication : KoinComponent {
    init {
        startKoin {
            modules(appModule, databaseModule)
        }
    }
}
