package cc.suffro.bpmanalyzer.wav.data

import java.nio.ByteBuffer

sealed class FmtChunk {
    abstract val riffChunkSize: Int
    abstract val fmtChunkSize: Int
    abstract val audioFormat: AudioFormat
    abstract val numChannels: Short
    abstract val sampleRate: Int
    abstract val byteRate: Int
    abstract val blockAlign: Short
    abstract val bitsPerSample: Short
}

data class PcmFmtChunk(
    override val riffChunkSize: Int,
    override val fmtChunkSize: Int,
    override val audioFormat: AudioFormat,
    override val numChannels: Short,
    override val sampleRate: Int,
    override val byteRate: Int,
    override val blockAlign: Short,
    override val bitsPerSample: Short,
) : FmtChunk()

data class WaveExtensibleFmtChunk(
    private val standardChunk: PcmFmtChunk,
    private val extensibleChunk: ExtensibleChunk,
) : FmtChunk() {

    override val riffChunkSize: Int get() = standardChunk.riffChunkSize
    override val fmtChunkSize: Int get() = standardChunk.fmtChunkSize
    override val audioFormat: AudioFormat get() = standardChunk.audioFormat
    override val numChannels: Short get() = standardChunk.numChannels
    override val sampleRate: Int get() = standardChunk.sampleRate
    override val byteRate: Int get() = standardChunk.byteRate
    override val blockAlign: Short get() = standardChunk.blockAlign
    override val bitsPerSample: Short get() = standardChunk.bitsPerSample
}

data class ExtensibleChunk(
    private val cbSize: Short,
    private val validBitsPerSample: Short,
    private val channelMask: Int,
    private val subFormat: ByteBuffer,
    private val factChunkSize: Int,
    private val factSampleLength: Int
)