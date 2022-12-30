package cc.suffro.fft.data

import java.nio.file.Path
import kotlin.math.roundToInt

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

    val trackLength: Double
        get() = fmtChunk.dataChunkSize.toDouble() / (sampleRate * fmtChunk.blockAlign)

    val sampleRate: Int
        get() = fmtChunk.sampleRate

    private fun DoubleArray.get(begin: Int, length: Int): Sequence<Double> {
        if (begin < 0 || begin > begin + length || begin + length > this.size) {
            throw IndexOutOfBoundsException("begin $begin, end ${begin + length}, length ${this.size}")
        }
        return this.asSequence().drop(begin).take(length)
    }

    private fun checkRequirements(channel: Int, numSamples: Int) {
        require(channel >= 0) { "Selected Channel has to be greater than or equal to zero." }
        require(channel < dataChunk.size) { "Selected Channel has to be smaller than available channels (${dataChunk.size})." }
        require((numSamples != 0) && numSamples and (numSamples - 1) == 0) { "Length has to be power of tow, but is $numSamples." }
    }

    fun getWindow(channel: Int, begin: Int, numSamples: Int = DEFAULT_SAMPLE_NUMBER): Window<Double> {
        checkRequirements(channel, numSamples)
        return dataChunk[channel].get(begin, numSamples)
    }

    fun getWindows(
        start: Double = 0.0,
        end: Double = trackLength,
        interval: Double,
        channel: Int = 0,
        numSamples: Int = DEFAULT_SAMPLE_NUMBER
    ): Sequence<Window<Double>> {
        checkRequirements(channel, numSamples)
        require(end <= trackLength) { "Selected end time of $end seconds exceeds actual end of track ($trackLength seconds)." }

        val sampleInterval = (interval * sampleRate).roundToInt()
        val startSample = (sampleRate * start).roundToInt()
        val endSample =
            minOf((sampleRate * end).roundToInt(), (fmtChunk.dataChunkSize / fmtChunk.blockAlign) - numSamples)

        return (startSample until endSample)
            .step(sampleInterval)
            .asSequence()
            .takeWhile { it < endSample }
            .map { index -> dataChunk[channel].get(index, numSamples) }
    }

    companion object {
        private const val DEFAULT_SAMPLE_NUMBER = 1024
    }
}

enum class AudioFormat(val value: UShort) {
    PCM(0x0001U);

    companion object {
        private val mapping = values().associateBy(AudioFormat::value)

        fun fromShort(key: UShort): AudioFormat = mapping.getValue(key)
    }
}
