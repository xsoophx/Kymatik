# BPM Analyzer 🎛️

This project can be used to analyze the tempo (BPM) of .wav music files.

Currently, the tempo can be determined by two different approaches.

## Approach One - [PeakDistanceAnalyzer](src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/analyzers/PeakDistanceAnalyzer.kt)

After executing a fast Fourier transform, this algorithm detects the peaks of bass frequencies in the frequency
domain. The distances between the peaks will be determined and the corresponding BPM is being calculated. This algorithm
can be done in "real-time", however it is not yielding very precise results yet (TODO: needs to be improved).

## Approach Two - [CombFilterAnalyzer](src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/analyzers/CombFilterAnalyzer.kt)

This Analyzer is making use of an algorithm, which was
proposed [on this page](https://www.clear.rice.edu/elec301/Projects01/beat_sync/beatalgo.html). After executing a
fast Fourier transform, the frequencies are being low passed and differentiated. A Comb Filter of three
pulses for each tempo, is being convolved with the final signal. By comparing all the resulting peaks, the tempo can
be determined very precisely. Currently, this is not possible in "real-time", some optimization still has to be made.
