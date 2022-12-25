package de.tu_chemnitz.fft

import de.tu_chemnitz.fft.data.AudioFormat
import de.tu_chemnitz.fft.data.Error
import de.tu_chemnitz.fft.data.ErrorType
import de.tu_chemnitz.fft.data.FmtChunk
import de.tu_chemnitz.fft.data.Sample
import de.tu_chemnitz.fft.data.Wav
import java.io.BufferedInputStream
import java.io.InputStream
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
    private const val FMT_SIGNATURE = "fmt "
    private const val DATA_SIGNATURE = "data"

    fun Wav.readSamplesAt(index: Int, numSamples: Int): Sequence<Sample> {
        val buffer = dataChunk.let(ByteBuffer::wrap).apply { order(ByteOrder.LITTLE_ENDIAN) }
        val destination = ByteArray(numSamples * fmtChunk.bitsPerSample / 8)
        buffer.get(destination, index * fmtChunk.blockAlign, numSamples * fmtChunk.bitsPerSample / 8)

        return destination.readSamples(fmtChunk.bitsPerSample.toInt(), fmtChunk.numChannels.toInt()).asSequence()
    }

    private fun ByteArray.readSamples(bitsPerSample: Int, numChannels: Int): DoubleArray {
        val dataChunkSize = size
        val doubleSamples: DoubleArray
        val byteBuffer = ByteBuffer.wrap(this)

        when (bitsPerSample) {
            8 -> {
                doubleSamples = DoubleArray(dataChunkSize / numChannels)
                for (i in doubleSamples.indices) {
                    doubleSamples[i] = byteBuffer[i * numChannels].toDouble()
                }
            }

            16 -> {
                val shortBuffer = byteBuffer.asShortBuffer()
                doubleSamples = DoubleArray(dataChunkSize / (2 * numChannels))
                for (i in doubleSamples.indices) {
                    doubleSamples[i] = shortBuffer[i * numChannels].toDouble()
                }
            }

            24 -> {
                doubleSamples = DoubleArray(dataChunkSize / (3 * numChannels))
                for (i in doubleSamples.indices) {
                    val b1 = byteBuffer.get().toUByte()
                    val b2 = byteBuffer.get().toUByte()
                    val b3 = byteBuffer.get().toUByte()
                    val sample = b1.toUInt() or (b2.toUInt() shl 8) or (b3.toUInt() shl 16)
                    doubleSamples[i] = if (sample and 0x00800000U == 0x00800000U) {
                        (sample or 0xFF000000U).toDouble()
                    } else {
                        sample.toDouble()
                    }

                    // skip other channels
                    val skipper = ByteArray(3)
                    for (j in 1 until numChannels) {
                        byteBuffer.get(skipper)
                    }
                }
            }

            32 -> {
                val intBuffer = byteBuffer.asIntBuffer()
                doubleSamples = DoubleArray(dataChunkSize / (4 * numChannels))
                for (i in doubleSamples.indices) {
                    doubleSamples[i] = intBuffer[i * numChannels].toDouble()
                }
            }

            else -> throw IllegalArgumentException("Unsupported bits per sample: $bitsPerSample")
        }

        return doubleSamples
    }

    private fun InputStream.readBuffer(size: Int): ByteBuffer =
        readNBytes(size)
            ?.takeIf { it.size == size }
            ?.let(ByteBuffer::wrap)
            ?.apply { order(ByteOrder.LITTLE_ENDIAN) }
            ?: throw ParsingException(Error(ErrorType.UNEXPECTED_EOF))

    private fun InputStream.readAsShort() = readBuffer(Short.SIZE_BYTES).short

    private fun InputStream.readAsInt() = readBuffer(Int.SIZE_BYTES).int

    private fun getRiffChunkSize(input: BufferedInputStream): Int {
        val riffSignature = String(input.readNBytes(RIFF_HEADER_CHUNK_SIZE), Charsets.US_ASCII)
        val fileSize = input.readAsInt()
        val waveSignature = String(input.readNBytes(RIFF_HEADER_CHUNK_SIZE), Charsets.US_ASCII)

        check(riffSignature == RIFF_SIGNATURE, ErrorType.NOT_A_RIFF)
        check(waveSignature == WAVE_SIGNATURE, ErrorType.NOT_A_WAV)

        return fileSize
    }

    override fun read(path: Path): Wav {
        return path.inputStream().buffered().use { input ->
            val riffChunkSize = getRiffChunkSize(input)

            val fmtSignature = String(input.readNBytes(4), Charsets.US_ASCII)
            check(fmtSignature == FMT_SIGNATURE, ErrorType.UNEXPECTED_FMT_SIGNATURE)

            // fmt
            val fmtChunkSize = input.readAsInt()
            val audioFormat = AudioFormat.fromShort(input.readAsShort().toUShort())
            val numChannels = input.readAsShort()
            val sampleRate = input.readAsInt()
            val byteRate = input.readAsInt()
            val blockAlign = input.readAsShort()
            val bitsPerSample = input.readAsShort()

            // data
            val dataSignature = String(input.readNBytes(4), Charsets.US_ASCII)
            check(dataSignature == DATA_SIGNATURE, ErrorType.UNEXPECTED_DATA_SIGNATURE)
            val dataChunkSize = input.readAsInt()
            val data = input.readNBytes(dataChunkSize)
            check(data.size == dataChunkSize, ErrorType.WRONG_DATA_SIZE)

            Wav(
                filePath = path,
                fmtChunk = FmtChunk(
                    riffChunkSize = riffChunkSize,
                    fmtChunkSize = fmtChunkSize,
                    audioFormat = audioFormat,
                    numChannels = numChannels,
                    sampleRate = sampleRate,
                    byteRate = byteRate,
                    blockAlign = blockAlign,
                    bitsPerSample = bitsPerSample,
                    dataChunkSize = dataChunkSize
                ),
                dataChunk = data
            )
        }
    }

    private class ParsingException(error: Error) : RuntimeException(error.message)

    @OptIn(ExperimentalContracts::class)
    private inline fun check(value: Boolean, error: ErrorType, lazyMessage: () -> String = { "" }) {
        contract {
            returns() implies value
        }
        if (!value) {
            throw ParsingException(Error(error, lazyMessage()))
        }
    }
}
