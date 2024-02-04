package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo

interface DatabaseOperations {
    fun saveTrackInfo(trackName: String, bpm: Double)

    fun getTrackInfo(trackName: String): TrackInfo
}
