package de.tu_chemnitz.fft.data

import java.nio.file.Path

typealias DataChunk = ByteArray

data class FmtChunk(
    val riffChunkSize: Int,
    val fmtChunkSize: Int,
    val audioFormat: AudioFormat,
    val numChannels: Short,
    val sampleRate: Int,
    val byteRate: Int,
    val blockAlign: Short,
    val bitsPerSample: Short,
    val dataChunkSize: Int
)


data class Wav(
    val filePath: Path,
    val fmtChunk: FmtChunk,
    val dataChunk: DataChunk
) {
    override fun toString(): String = "Filepath:$filePath, fmtChunk:$fmtChunk."
}

enum class AudioFormat(val value: UShort) {
    PCM(0x0001U);

    companion object {
        private val mapping = values().associateBy(AudioFormat::value)

        fun fromShort(key: UShort): AudioFormat = mapping.getValue(key)
    }
}
