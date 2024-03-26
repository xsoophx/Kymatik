package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.data.TrackInfo
import org.koin.core.component.KoinComponent
import java.io.Closeable

interface BpmOperations : Closeable, KoinComponent {
    fun analyze(args: Array<String>): TrackInfo

    fun analyze(
        trackPath: String,
        databasePath: String,
    ): TrackInfo

    fun init()
}
