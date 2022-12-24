package de.tu_chemnitz.fft

import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.inputStream

// TODO: needs refactoring
interface FileReader<T> {
    fun read(path: Path): T
}

object WAVReader : FileReader<Wav> {
    private const val RIFF_HEADER_CHUNK_SIZE = 4
    private const val RIFF_SIGNATURE = "RIFF"
    private const val WAVE_SIGNATURE = "WAVE"
    private const val FMT_SIGNATURE = "fmt"
    private const val DATA_SIGNATURE = "data"

    private fun BufferedInputStream.readUInt(): UInt {
        val buffer = ByteBuffer.wrap(readNBytes(4))
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        return buffer.getInt(0).toUInt()
    }

    private fun BufferedInputStream.readUShort(): UShort {
        val buffer = ByteBuffer.wrap(readNBytes(2))
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        return buffer.getShort(0).toUShort()
    }

    private fun getChunkSize(input: BufferedInputStream): UInt {
        val riffSignature = String(input.readNBytes(RIFF_HEADER_CHUNK_SIZE), Charsets.US_ASCII)
        val fileSize = input.readUInt()
        val waveSignature = String(input.readNBytes(RIFF_HEADER_CHUNK_SIZE), Charsets.US_ASCII)

        checkChunkContent(riffSignature == RIFF_SIGNATURE) { "File is not a RIFF file." }
        checkChunkContent(waveSignature == WAVE_SIGNATURE) { "File is not a .wav file." }

        return fileSize
    }

    override fun read(path: Path): Wav {
        return path.inputStream().buffered().use { input ->
            val chunkSize = getChunkSize(input)

            val fmtSignature = String(input.readNBytes(4), Charsets.US_ASCII).trim()
            checkChunkContent(fmtSignature == FMT_SIGNATURE) { "File doesn't contain fmt signature." }

            // fmt
            val subChunkOneSize = input.readUInt()
            val audioFormat = AudioFormat.byValue.getValue(input.readUShort().toInt())
            val numChannels = input.readUShort()
            val sampleRate = input.readUInt()
            val byteRate = input.readUInt()
            val blockAlign = input.readUShort()
            val bitsPerSample = input.readUShort()

            // data
            val dataSignature = String(input.readNBytes(4), Charsets.US_ASCII)
            checkChunkContent(dataSignature == DATA_SIGNATURE) { "File contains invalid data." }
            val subChunkTwoSize = input.readUInt()
            val data = input.readNBytes(subChunkTwoSize.toInt())

            Wav(
                filePath = path,
                chunkSize = chunkSize,
                subChunkOneSize = subChunkOneSize,
                audioFormat = audioFormat,
                numChannels = numChannels,
                sampleRate = sampleRate,
                byteRate = byteRate,
                blockAlign = blockAlign,
                bitsPerSample = bitsPerSample,
                subChunkTwoSize = subChunkTwoSize,
            )
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun checkChunkContent(value: Boolean, lazyMessage: () -> Any) {
        contract {
            returns() implies value
        }
        if (!value) {
            val message = lazyMessage()
            throw RuntimeException(message.toString())
        }
    }
}

data class Wav(
    val filePath: Path,
    val chunkSize: UInt,
    val subChunkOneSize: UInt,
    val audioFormat: AudioFormat,
    val numChannels: UShort,
    val sampleRate: UInt,
    val byteRate: UInt,
    val blockAlign: UShort,
    val bitsPerSample: UShort,
    val subChunkTwoSize: UInt
)

enum class AudioFormat(val value: Int) {
    PCM(1);

    companion object {
        val byValue = values().associateBy(AudioFormat::value)
    }
}
