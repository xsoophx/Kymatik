package cc.suffro.bpmanalyzer.wav

import cc.suffro.bpmanalyzer.wav.data.DataChunk
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

internal object WavWriter : FileWriter<Wav> {
    override fun write(
        path: String,
        data: Wav,
    ): Boolean {
        return write(Path.of(path), data)
    }

    override fun write(
        path: Path,
        data: Wav,
    ): Boolean {
        BufferedOutputStream(Files.newOutputStream(path)).use { output ->
            writeRiffHeader(output, data)
            writeFmtChunk(output, data.fmtChunk)
            writeDataChunk(output, data.fmtChunk, data.dataChunk)
        }
        return true
    }

    private fun writeRiffHeader(
        output: OutputStream,
        wav: Wav,
    ) {
        val fileSize = wav.fmtChunk.riffChunkSize
        output.write(RIFF_SIGNATURE.toByteArray(Charsets.US_ASCII))
        output.write(intToByteArray(fileSize))
        output.write(WAVE_SIGNATURE.toByteArray(Charsets.US_ASCII))
    }

    private fun writeFmtChunk(
        output: OutputStream,
        fmtChunk: FmtChunk,
    ) {
        output.write(FMT_SIGNATURE.toByteArray(Charsets.US_ASCII))
        output.write(intToByteArray(fmtChunk.fmtChunkSize))
        output.write(shortToByteArray(fmtChunk.audioFormat.value.toShort()))
        output.write(shortToByteArray(fmtChunk.numChannels))
        output.write(intToByteArray(fmtChunk.sampleRate))
        output.write(intToByteArray(fmtChunk.byteRate))
        output.write(shortToByteArray(fmtChunk.blockAlign))
        output.write(shortToByteArray(fmtChunk.bitsPerSample))
    }

    private fun writeDataChunk(
        output: OutputStream,
        fmtChunk: FmtChunk,
        dataChunk: DataChunk,
    ) {
        output.write(DATA_SIGNATURE.toByteArray(Charsets.US_ASCII))
        output.write(intToByteArray(dataChunk.dataChunkSize))
        val sampleCount = dataChunk.dataChunkSize / fmtChunk.blockAlign

        when (fmtChunk.bitsPerSample.toInt()) {
            16 -> {
                for (sampleIndex in 0 until sampleCount) {
                    for (channel in 0 until fmtChunk.numChannels) {
                        val shortValue = (dataChunk.data[channel][sampleIndex] * Short.MAX_VALUE).toInt().toShort()
                        val sampleBytes =
                            ByteBuffer.allocate(Short.SIZE_BYTES).apply {
                                order(ByteOrder.LITTLE_ENDIAN)
                                putShort(shortValue)
                            }.array()
                        output.write(sampleBytes)
                    }
                }
            }

            else -> throw IllegalArgumentException("Unsupported bits per sample: ${fmtChunk.bitsPerSample}")
        }
    }

    private fun intToByteArray(value: Int): ByteArray =
        ByteBuffer.allocate(Int.SIZE_BYTES).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putInt(value)
        }.array()

    private fun shortToByteArray(value: Short): ByteArray =
        ByteBuffer.allocate(Short.SIZE_BYTES).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putShort(value)
        }.array()

    private fun sampleToBytes(
        sample: Double,
        bitsPerSample: Int,
    ): ByteArray {
        return when (bitsPerSample) {
            8 -> {
                val byteValue = ((sample + 1.0) * 127.5).toInt().coerceIn(0, 255).toByte()
                byteArrayOf(byteValue)
            }

            16 -> {
                val shortValue = (sample * Short.MAX_VALUE).toInt().toShort()
                ByteBuffer.allocate(Short.SIZE_BYTES).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    putShort(shortValue)
                }.array()
            }

            24 -> {
                val intValue = (sample * 0x7FFFFF).toInt()
                byteArrayOf(
                    (intValue and 0xFF).toByte(),
                    (intValue shr 8 and 0xFF).toByte(),
                    (intValue shr 16 and 0xFF).toByte(),
                )
            }

            32 -> {
                val intValue = (sample * Int.MAX_VALUE).toInt()
                ByteBuffer.allocate(Int.SIZE_BYTES).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    putInt(intValue)
                }.array()
            }

            else -> throw IllegalArgumentException("Unsupported bits per sample: $bitsPerSample")
        }
    }
}
