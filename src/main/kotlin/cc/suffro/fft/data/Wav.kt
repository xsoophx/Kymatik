package cc.suffro.fft.data

import java.nio.file.Path

typealias DataChunk = Array<DoubleArray>

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

    private fun DoubleArray.get(begin: Int, length: Int): Sequence<Double> {
        if (begin < 0 || begin > begin + length || begin + length > this.size) {
            throw IndexOutOfBoundsException("begin $begin, end ${begin + length}, length ${this.size}")
        }
        return this.asSequence().drop(begin).take(length)
    }

    fun getSamples(channel: Int, begin: Int, length: Int): Sequence<Double> {
        require(channel >= 0) { "Selected Channel has to be greater than or equal to zero." }
        require(channel < dataChunk.size) { "Selected Channel has to be smaller than available channels (${dataChunk.size})." }
        require((length != 0) && length and (length - 1) == 0) { "Length has to be power of tow, but is $length." }

        return dataChunk[channel].get(begin, length)
    }
}

enum class AudioFormat(val value: UShort) {
    PCM(0x0001U);

    companion object {
        private val mapping = values().associateBy(AudioFormat::value)

        fun fromShort(key: UShort): AudioFormat = mapping.getValue(key)
    }
}
