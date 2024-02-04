package cc.suffro.bpmanalyzer.wav

import cc.suffro.bpmanalyzer.wav.data.AudioFormat
import cc.suffro.bpmanalyzer.wav.data.Error
import cc.suffro.bpmanalyzer.wav.data.ErrorType
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.Path
import kotlin.io.path.inputStream

// TODO: needs refactoring
fun interface FileReader<T> {
    fun read(path: Path): T
}

object WAVReader : FileReader<Wav> {
    private const val RIFF_HEADER_CHUNK_SIZE = 4
    private const val RIFF_SIGNATURE = "RIFF"
    private const val WAVE_SIGNATURE = "WAVE"
    private const val FMT_SIGNATURE = "fmt "
    private const val DATA_SIGNATURE = "data"

    private const val MAX_VALUE_24BIT = 0x7FFFFF

    fun read(path: String): Wav = read(Path(path))

    override fun read(path: Path): Wav = path.inputStream().buffered().use { input ->
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

        val fmtChunk = FmtChunk(
            riffChunkSize = riffChunkSize,
            fmtChunkSize = fmtChunkSize,
            audioFormat = audioFormat,
            numChannels = numChannels,
            sampleRate = sampleRate,
            byteRate = byteRate,
            blockAlign = blockAlign,
            bitsPerSample = bitsPerSample,
            dataChunkSize = dataChunkSize
        )

        Wav(
            filePath = path,
            fmtChunk = fmtChunk,
            dataChunk = data.readSamples(fmtChunk)
        )
    }

    private fun ByteArray.readSamples(fmtChunk: FmtChunk): Array<DoubleArray> {
        val dataChunkSize = size
        val byteBuffer = ByteBuffer.wrap(this).apply { order(ByteOrder.LITTLE_ENDIAN) }
        val sampleCount = dataChunkSize / fmtChunk.blockAlign
        val bytesPerChannel = fmtChunk.bitsPerSample / 8
        val samples = Array(fmtChunk.numChannels.toInt()) { DoubleArray(sampleCount) }

        return when (fmtChunk.bitsPerSample.toInt()) {
            // PCM 8-bit is unsigned with 1 at negative full scale, 255 at positive full scale and 128 at midpoint
            8 -> {
                for (sampleIndex in 0 until sampleCount) {
                    for (channel in 0 until fmtChunk.numChannels) {
                        val value = maxOf(1.0, byteBuffer[sampleIndex * fmtChunk.blockAlign + channel].toDouble())
                        samples[channel][sampleIndex] = ((value - 1.0) / 127.0) - 1
                    }
                }
                samples
            }

            16 -> {
                for (sampleIndex in 0 until sampleCount) {
                    for (channel in 0 until fmtChunk.numChannels) {
                        samples[channel][sampleIndex] = byteBuffer.getShort(
                            sampleIndex * fmtChunk.blockAlign + channel * bytesPerChannel
                        ).toDouble() / Short.MAX_VALUE
                    }
                }
                samples
            }

            24 -> {
                for (sampleIndex in 0 until sampleCount) {
                    for (channel in 0 until fmtChunk.numChannels) {
                        val b1 = byteBuffer[sampleIndex * fmtChunk.blockAlign + channel * bytesPerChannel].toUByte()
                        val b2 = byteBuffer[sampleIndex * fmtChunk.blockAlign + channel * bytesPerChannel + 1].toUByte()
                        val b3 = byteBuffer[sampleIndex * fmtChunk.blockAlign + channel * bytesPerChannel + 2].toUByte()
                        val sample = b1.toUInt() or (b2.toUInt() shl 8) or (b3.toUInt() shl 16)

                        samples[channel][sampleIndex] = if (sample and 0x00800000U == 0x00800000U) {
                            (sample or 0xFF000000U).toDouble() / MAX_VALUE_24BIT
                        } else {
                            sample.toDouble() / MAX_VALUE_24BIT
                        }
                    }
                }
                samples
            }

            32 -> {
                for (sampleIndex in 0 until sampleCount) {
                    for (channel in 0 until fmtChunk.numChannels) {
                        samples[channel][sampleIndex] =
                            (byteBuffer.getInt(sampleIndex * fmtChunk.blockAlign + channel * bytesPerChannel) / Int.MAX_VALUE).toDouble()
                    }
                }
                samples
            }

            else -> throw IllegalArgumentException("Unsupported bits per sample: ${fmtChunk.bitsPerSample}")
        }
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
