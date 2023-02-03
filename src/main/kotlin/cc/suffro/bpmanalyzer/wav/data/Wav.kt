package cc.suffro.bpmanalyzer.wav.data

import cc.suffro.bpmanalyzer.fft.data.Window
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
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
) {
    val trackLength: Double
        get() = dataChunkSize.toDouble() / (sampleRate * blockAlign)

    val indexLastSample: Int
        get() = ((dataChunkSize - blockAlign) / blockAlign)
}

data class Wav(
    val filePath: Path,
    val fmtChunk: FmtChunk,
    val dataChunk: DataChunk
) {
    override fun toString(): String = "Filepath:$filePath, fmtChunk:$fmtChunk."

    val trackLength: Double
        get() = fmtChunk.trackLength

    private val indexLastSample: Int
        get() = fmtChunk.indexLastSample

    val timestampLastSample: Double
        get() = indexLastSample.toDouble() / sampleRate

    val sampleRate: Int
        get() = fmtChunk.sampleRate

    private fun DoubleArray.get(begin: Int, length: Int): Sequence<Double> {
        if (begin < 0 || begin > begin + length || begin + length > this.size) {
            throw IndexOutOfBoundsException("begin $begin, end ${begin + length}, length ${this.size}")
        }
        return this.asSequence().drop(begin).take(length)
    }

    private fun checkRequirements(channel: Int, numSamples: Int) {
        checkChannelRequirements(channel)
        require((numSamples != 0) && numSamples and (numSamples - 1) == 0) { "Length has to be power of tow, but is $numSamples." }
    }

    private fun checkChannelRequirements(channel: Int) {
        require(channel >= 0) { "Selected Channel has to be greater than or equal to zero." }
        require(channel < dataChunk.size) { "Selected Channel has to be smaller than available channels (${dataChunk.size})." }
    }

    fun getWindowContent(channel: Int, begin: Int, numSamples: Int = DEFAULT_SAMPLE_NUMBER): Sequence<Double> {
        checkRequirements(channel, numSamples)
        return dataChunk[channel].get(begin, numSamples)
    }

    private fun samplesOf(number: Double): Int = (number * sampleRate).roundToInt()

    private fun checkOrCorrectEnd(end: Double): Double = if (end >= trackLength) timestampLastSample else end

    fun getWindows(
        start: Double = 0.0,
        end: Double = timestampLastSample,
        interval: Double,
        channel: Int = 0,
        numSamples: Int = DEFAULT_SAMPLE_NUMBER
    ): Sequence<Window> {
        checkRequirements(channel, numSamples)
        val correctedEnd = checkOrCorrectEnd(end)
        return getWindows(samplesOf(start), samplesOf(correctedEnd), interval, channel, numSamples)
    }

    private fun getWindows(start: Int, end: Int, interval: Double, channel: Int, numSamples: Int): Sequence<Window> {
        return getSamples(
            start,
            minOf(end, indexLastSample - numSamples),
            samplesOf(interval),
            channel,
            numSamples,
            interval
        )
    }

    fun getWindow(
        start: Double = 0.0,
        numSamples: Int,
        channel: Int = 0
    ): Window {
        checkRequirements(channel, numSamples)
        val interval = numSamples.toDouble() / sampleRate
        return getWindow(samplesOf(start), samplesOf(start) + numSamples, interval, channel)
    }

    private fun getWindow(start: Int, end: Int, interval: Double, channel: Int): Window {
        val numSamples = getHighestPowerOfTwo(end - start)
        val endSample = start + numSamples

        return getSamples(start, endSample, samplesOf(interval), channel, numSamples, interval).first()
    }

    private fun getSamples(
        startSample: Int,
        endSample: Int,
        sampleInterval: Int,
        channel: Int,
        numSamples: Int,
        interval: Double
    ): Sequence<Window> {
        val samples = (startSample until endSample)
            .step(sampleInterval)
            .asSequence()
            .takeWhile { it < endSample }
            .map { index -> dataChunk[channel].get(index, numSamples) }

        return samples.map { Window(it, interval) }
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
