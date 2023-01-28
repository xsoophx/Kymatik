package cc.suffro.fft

import cc.suffro.fft.bpmanalyzing.analyzers.CombFilterAnalyzer
import cc.suffro.fft.wav.WAVReader
import java.nio.file.Path
import java.nio.file.Paths

object Main {
    private fun analyzeWav(file: Path) {
        val wav = WAVReader.read(file)
        val bpmAnalyzer = CombFilterAnalyzer()
        val bpm = bpmAnalyzer.analyze(wav)
        println("$file has a BPM of $bpm.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        args.asSequence().drop(1).map(Paths::get).forEach(this::analyzeWav)
    }
}
