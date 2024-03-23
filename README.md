# BPM Analyzer 🎛️

This project is designed to analyze the tempo (BPM) of .wav music files, utilizing various digital signal
processing techniques. The primary method used for tempo detection in this repository is implemented in the
CombFilterAnalyzer, which is giving high precision in determining BPM.

The Analyzer is making use of an algorithm, which was proposed
[on this page](https://www.clear.rice.edu/elec301/Projects01/beat_sync/beatalgo.html).

## [CombFilterAnalyzer](application/src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/analyzers/CombFilterAnalyzer.kt)

### Step 1: [Filter bank](application/src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/filters/Filterbank.kt)

The algorithm begins by dissecting the audio signal into distinct frequency bands, isolating different instrumental
ranges. This step is important, as it mitigates the potential for tempo detection errors caused by overlapping beats
from various instruments. By applying the
[Fast Fourier Transform](application/src/main/kotlin/cc/suffro/bpmanalyzer/fft/FFTProcessor.kt) (FFT) and segmenting the resultant
spectrum into predefined frequency ranges, each band captures a unique aspect of the music's profile
(0-200Hz to 3200Hz). This is ensuring a comprehensive analysis across the spectrum.

### Step 2: Smoothing

Each frequency band undergoes full-wave rectification followed by a convolution with an optional
[window function](application/src/main/kotlin/cc/suffro/bpmanalyzer/fft/data/WindowFunction.kt) (a process for smoothing out
the signal and accentuating the amplitudes). This smoothing helps to have a cleaner representation of the rhythmic
pulse.

### Step 3: Differential Rectification

The algorithm now [differentiates](application/src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/filters/DifferentialRectifier.kt) 
the signals to highlight sudden changes in amplitude.
By differentiating and then half-wave rectifying, the algorithm determines significant sound intensity increases, which
typically align with the beats in music.
This step transforms the smoothed envelopes into a form optimized for the final tempo analysis.

### Step 4: Comb Filter

Finally, the algorithm uses a
[comb filter](application/src/main/kotlin/cc/suffro/bpmanalyzer/bpmanalyzing/filters/CombFilter.kt) to scan through the
differentiated signals. This comb filter is convolved with the signal to determine the alignment between the signal's
rhythmic pattern and the filter's tempo. When the tempo of the comb filter resonates with the tempo of the music, the
convolution results in a signal with pronounced peaks, indicating a strong correlation. By examining the energy output
of these convolutions across a spectrum of tempos, the algorithm can accurately determine the music's tempo.
