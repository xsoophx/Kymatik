package cc.suffro.bpmanalyzer.wav.data

internal enum class ErrorType {
    INVALID_WAVE_FORMAT_EXTENSIBLE,
    INVALID_W_BITS_PER_SAMPLE,
    NOT_A_RIFF,
    NOT_A_WAV,
    UNEXPECTED_DATA_SIGNATURE,
    UNEXPECTED_EOF,
    UNEXPECTED_FACT_SIGNATURE,
    UNEXPECTED_FMT_SIGNATURE,
    WRONG_DATA_SIZE,
    ;

    override fun toString() =
        when (this) {
            INVALID_WAVE_FORMAT_EXTENSIBLE -> "Invalid WAVE format extensible. Expected 22 bytes."
            INVALID_W_BITS_PER_SAMPLE -> "Invalid bits per sample. Expected 8, 16, or 24."
            NOT_A_RIFF -> "No RIFF header detected."
            NOT_A_WAV -> "RIFF is not of type WAV."
            UNEXPECTED_DATA_SIGNATURE -> "File contains invalid data signature."
            UNEXPECTED_EOF -> "RIFF file ended unexpectedly."
            UNEXPECTED_FACT_SIGNATURE -> "File contains invalid fact signature."
            UNEXPECTED_FMT_SIGNATURE -> "File contains invalid fmt signature."
            WRONG_DATA_SIZE -> "Data Chunk doesn't have the required size."
        }
}

internal data class Error(
    val type: ErrorType,
    var message: String = "",
) {
    init {
        message = type.toString()
    }
}
