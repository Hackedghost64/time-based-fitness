package com.timebasedfitness.app.notifications

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight, zero-asset audio cue helper using Android's system [ToneGenerator].
 * Provides crisp 3-2-1 countdown ticks and celebratory completion chimes without
 * bundling heavy audio files.
 */
@Singleton
class TimerAudioHelper @Inject constructor() {
    private var toneGenerator: ToneGenerator? = null

    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            runCatching {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            }
        }
        return toneGenerator
    }

    /** Short warning tick for 3, 2, 1 seconds countdown */
    fun playCountdownTick() {
        runCatching {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 75)
        }
    }

    /** Distinctive final completion tone when timer hits 0 */
    fun playTimerCompleteTone() {
        runCatching {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        }
    }

    /** Rest timer completion chime */
    fun playRestCompleteTone() {
        runCatching {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        }
    }

    fun release() {
        runCatching {
            toneGenerator?.release()
            toneGenerator = null
        }
    }
}
