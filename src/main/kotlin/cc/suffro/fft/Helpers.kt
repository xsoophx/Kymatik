package cc.suffro.fft

import org.kotlinmath.Complex
import org.kotlinmath.sqrt

fun abs(n: Complex): Complex = sqrt(n.re * n.re + n.im * n.im)
