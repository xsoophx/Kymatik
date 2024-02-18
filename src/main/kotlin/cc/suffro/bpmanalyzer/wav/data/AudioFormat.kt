package cc.suffro.bpmanalyzer.wav.data

enum class AudioFormat(val value: UShort) {
    PCM(0x0001U),
    ;

    companion object {
        private val mapping = entries.associateBy(AudioFormat::value)

        fun fromShort(key: UShort): AudioFormat = mapping.getValue(key)
    }
}
