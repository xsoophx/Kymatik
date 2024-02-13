package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo

interface DatabaseOperations {
    fun saveTrackInfo(trackName: String, bpm: Double): Int

    fun getTrackInfo(trackName: String): TrackInfo

    fun cleanUpDatabase(closeConnection: Boolean = false): Boolean

    fun closeConnection()
}
