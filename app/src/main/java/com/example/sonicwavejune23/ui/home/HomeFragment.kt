package com.example.sonicwavejune23.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.media.SoundPool
import android.media.AudioAttributes
import android.util.Log
import com.example.sonicwavejune23.R

class HomeFragment : Fragment() {

    private var frequency = 0
    private var intensity = 0
    private var time = 0
    private var isStarted = false // Track Start/Stop state
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private val longPressInterval = 150L // Increased to 150ms to reduce audio strain
    private val TAG = "HomeFragment" // For logging

    // SoundPool for playing sounds
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0
    private var fastSoundId: Int = 0
    private var isSoundPoolReady = false // Track SoundPool readiness

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize SoundPool
        initializeSoundPool()

        // Frequency controls (Up, Down, Value Display)
        val btnFrequencyUp: Button = view.findViewById(R.id.btn_frequency_up)
        val btnFrequencyDown: Button = view.findViewById(R.id.btn_frequency_down)
        val tvFrequency: TextView = view.findViewById(R.id.tv_frequency_value)

        // Intensity controls (Up, Down, Value Display)
        val btnIntensityUp: Button = view.findViewById(R.id.btn_intensity_up)
        val btnIntensityDown: Button = view.findViewById(R.id.btn_intensity_down)
        val tvIntensity: TextView = view.findViewById(R.id.tv_intensity_value)

        // Time controls (Up, Down, Value Display)
        val btnTimeUp: Button = view.findViewById(R.id.btn_time_up)
        val btnTimeDown: Button = view.findViewById(R.id.btn_time_down)
        val tvTime: TextView = view.findViewById(R.id.tv_time_value)

        // Start/Stop control (Single Toggle Button)
        val btnStartStop: Button = view.findViewById(R.id.btn_start_stop)

        // Frequency button listeners
        btnFrequencyUp.setOnClickListener {
            frequency += 1
            updateFrequencyDisplay(tvFrequency)
            playTapSound()
        }
        btnFrequencyDown.setOnClickListener {
            if (frequency > 0) frequency -= 1
            updateFrequencyDisplay(tvFrequency)
            playTapSound()
        }
        setupLongPress(btnFrequencyUp, { frequency += 1 }, tvFrequency, ::updateFrequencyDisplay)
        setupLongPress(btnFrequencyDown, { if (frequency > 0) frequency -= 1 }, tvFrequency, ::updateFrequencyDisplay)

        // Intensity button listeners
        btnIntensityUp.setOnClickListener {
            intensity += 1
            updateIntensityDisplay(tvIntensity)
            playTapSound()
        }
        btnIntensityDown.setOnClickListener {
            if (intensity > 0) intensity -= 1
            updateIntensityDisplay(tvIntensity)
            playTapSound()
        }
        setupLongPress(btnIntensityUp, { intensity += 1 }, tvIntensity, ::updateIntensityDisplay)
        setupLongPress(btnIntensityDown, { if (intensity > 0) intensity -= 1 }, tvIntensity, ::updateIntensityDisplay)

        // Time button listeners
        btnTimeUp.setOnClickListener {
            time += 1
            updateTimeDisplay(tvTime)
            playTapSound()
        }
        btnTimeDown.setOnClickListener {
            if (time > 0) time -= 1
            updateTimeDisplay(tvTime)
            playTapSound()
        }
        setupLongPress(btnTimeUp, { time += 1 }, tvTime, ::updateTimeDisplay)
        setupLongPress(btnTimeDown, { if (time > 0) time -= 1 }, tvTime, ::updateTimeDisplay)

        // Start/Stop button listener
        btnStartStop.setOnClickListener {
            isStarted = !isStarted
            btnStartStop.text = if (isStarted) "Stop" else "Start"
            playTapSound()
        }

        return view
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4) // Increased to 4 to handle rapid playback
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sound files
        tapSoundId = soundPool.load(context, R.raw.tap_sound, 1) // Fixed: Removed 'scad'
        fastSoundId = soundPool.load(context, R.raw.fast_sound, 1)

        // Verify sound loading
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d(TAG, "Sound loaded successfully: sampleId=$sampleId")
                if (sampleId == tapSoundId || sampleId == fastSoundId) {
                    isSoundPoolReady = true // Mark as ready when both sounds are loaded
                }
            } else {
                Log.e(TAG, "Failed to load sound: sampleId=$sampleId, status=$status")
            }
        }
    }

    private fun playTapSound() {
        if (isSoundPoolReady && tapSoundId != 0) {
            soundPool.play(tapSoundId, 1f, 1f, 1, 0, 1f)
            Log.d(TAG, "Playing tap sound")
        } else {
            Log.w(TAG, "Tap sound not ready: isSoundPoolReady=$isSoundPoolReady, tapSoundId=$tapSoundId")
        }
    }

    private fun playFastSound() {
        if (isSoundPoolReady && fastSoundId != 0) {
            soundPool.play(fastSoundId, 1f, 1f, 1, 0, 1f)
            Log.d(TAG, "Playing fast sound")
        } else {
            Log.w(TAG, "Fast sound not ready: isSoundPoolReady=$isSoundPoolReady, fastSoundId=$fastSoundId")
        }
    }

    private fun updateFrequencyDisplay(textView: TextView) {
        textView.text = "$frequency Hz"
    }

    private fun updateIntensityDisplay(textView: TextView) {
        textView.text = intensity.toString()
    }

    private fun updateTimeDisplay(textView: TextView) {
        textView.text = "$time s"
    }

    private fun setupLongPress(button: Button, action: () -> Unit, textView: TextView, updateDisplay: (TextView) -> Unit) {
        val longPressRunnable = object : Runnable {
            override fun run() {
                if (isLongPress) {
                    action() // Update number
                    updateDisplay(textView) // Update UI
                    playFastSound() // Play sound
                    Log.d(TAG, "Long press: Updated value and played sound")
                    handler.postDelayed(this, longPressInterval)
                }
            }
        }

        button.setOnLongClickListener {
            isLongPress = true
            handler.post(longPressRunnable)
            true
        }

        button.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                isLongPress = false
                handler.removeCallbacks(longPressRunnable)
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        soundPool.release() // Fixed: Corrected from 'MsoundPool'
        Log.d(TAG, "SoundPool released")
    }
}
