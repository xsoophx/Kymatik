package cc.suffro.bpmanalyzer.data

import java.nio.file.Path

data class TrackInfo(
    val trackName: Path,
    val bpm: Double,
)
