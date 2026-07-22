package dev.garado.metronome

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Subdivision(val label: String) {
    QUARTER("Quarter"),
    EIGHTH("Eighth"),
    TRIPLET("Triplet"),
    SIXTEENTH("Sixteenth"),
    QUINTUPLET("Quintuplet"),
    SEXTUPLET("Sextuplet"),
}

data class MetronomeSetting(val label: String, val enabled: Boolean)

data class TimeSignature(val numerator: Int, val denominator: Int) {
    override fun toString() = "$numerator/$denominator"
}

private const val MIN_BPM = 20
private const val MAX_BPM = 300
private const val DEFAULT_BPM = 100
private val DEFAULT_TIME_SIGNATURE = TimeSignature(3, 4)

// Shared app state, read/written directly by every screen — mirrors the
// LightThemeController pattern the SDK itself uses for cross-screen state.
object MetronomeState {
    private val _bpm = MutableStateFlow(DEFAULT_BPM)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _subdivision = MutableStateFlow(Subdivision.SIXTEENTH)
    val subdivision: StateFlow<Subdivision> = _subdivision.asStateFlow()

    private val _timeSignature = MutableStateFlow(DEFAULT_TIME_SIGNATURE)
    val timeSignature: StateFlow<TimeSignature> = _timeSignature.asStateFlow()

    private val _settings = MutableStateFlow(
        listOf(
            MetronomeSetting("Invert Colors", enabled = false),
            MetronomeSetting("Haptic Feedback", enabled = true),
            MetronomeSetting("Downbeat Accent", enabled = true),
            MetronomeSetting("Keep Screen Awake", enabled = false),
        )
    )
    val settings: StateFlow<List<MetronomeSetting>> = _settings.asStateFlow()

    fun setBpm(value: Int) {
        _bpm.value = value.coerceIn(MIN_BPM, MAX_BPM)
    }

    fun incrementBpm() {
        setBpm(_bpm.value + 1)
    }

    fun decrementBpm() {
        setBpm(_bpm.value - 1)
    }

    fun togglePlaying() {
        _isPlaying.value = !_isPlaying.value
    }

    fun selectSubdivision(value: Subdivision) {
        _subdivision.value = value
    }

    fun setTimeSignature(value: TimeSignature) {
        _timeSignature.value = value
    }

    fun toggleSetting(label: String) {
        _settings.value = _settings.value.map {
            if (it.label == label) it.copy(enabled = !it.enabled) else it
        }
    }
}
