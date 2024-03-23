package cc.suffro.bpmanalyzer.wav.data

import java.nio.file.Path

interface FileWriter<T> {
    fun write(
        path: String,
        data: T,
    ): Boolean

    fun write(
        path: Path,
        data: T,
    ): Boolean
}
