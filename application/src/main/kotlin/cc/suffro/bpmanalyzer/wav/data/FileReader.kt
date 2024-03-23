package cc.suffro.bpmanalyzer.wav.data

import java.nio.file.Path

interface FileReader<T> {
    fun read(path: Path): T

    fun read(path: String): T
}
