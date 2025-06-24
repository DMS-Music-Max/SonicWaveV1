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
import android.media.AudioTrack
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.util.Log
import com.example.sonicwavejune23.R
import kotlin.math.sin
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var frequency = 0
    private var intensity = 0
    private var time = 0
    private var isStarted = false // Track Start/Stop state
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private val longPressInterval = 150L // Interval for long press
    private val TAG = "HomeFragment" // For logging

    // SoundPool for button sounds
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0
    private var fastSoundId: Int = 0
    private var isSoundPoolReady = false

    // AudioTrack for tone generation
    private var audioTrack: AudioTrack? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var stopPlayback = false // Flag to stop tone playback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize SoundPool and AudioManager
        initializeSoundPool()
        audioManager = context?.getSystemService(AudioManager::class.java)

        // Frequency controls
        val btnFrequencyUp: Button = view.findViewById(R.id.btn_frequency_up)
        val btnFrequencyDown: Button = view.findViewById(R.id.btn_frequency_down)
        val tvFrequency: TextView = view.findViewById(R.id.tv_frequency_value)

        // Intensity controls
        val btnIntensityUp: Button = view.findViewById(R.id.btn_intensity_up)
        val btnIntensityDown: Button = view.findViewById(R.id.btn_intensity_down)
        val tvIntensity: TextView = view.findViewById(R.id.tv_intensity_value)

        // Time controls
        val btnTimeUp: Button = view.findViewById(R.id.btn_time_up)
        val btnTimeDown: Button = view.findViewById(R.id.btn_time_down)
        val tvTime: TextView = view.findViewById(R.id.tv_time_value)

        // Start/Stop control
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
            if (isStarted) {
                startTonePlayback()
            } else {
                stopTonePlayback()
            }
        }

        return view
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        tapSoundId = soundPool.load(context, R.raw.tap_sound, 1)
        fastSoundId = soundPool.load(context, R.raw.fast_sound, 1)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d(TAG, "Sound loaded successfully: sampleId=$sampleId")
                isSoundPoolReady = true
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
                    action()
                    updateDisplay(textView)
                    playFastSound()
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

    private fun startTonePlayback() {
        if (frequency <= 0 || time <= 0) {
            Log.w(TAG, "Invalid parameters: frequency=$frequency, time=$time")
            isStarted = false
            view?.findViewById<Button>(R.id.btn_start_stop)?.text = "Start"
            return
        }

        // Request audio focus
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener { focusChange ->
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    stopTonePlayback()
                }
            }
            .build()
        audioFocusRequest = focusRequest
        if (audioManager?.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(TAG, "Audio focus request denied")
            isStarted = false
            view?.findViewById<Button>(R.id.btn_start_stop)?.text = "Start"
            return
        }

        stopPlayback = false
        val sampleRate = 44100 // Standard audio sample rate
        val volume = if (intensity > 100) 1f else intensity / 100f // Map intensity to 0-1
        val durationMs = time * 1000L // Convert seconds to milliseconds

        // Initialize AudioTrack
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            android.media.AudioFormat.CHANNEL_OUT_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                android.media.AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        Log.d(TAG, "Playing tone at $frequency Hz, volume $volume")

        // Generate and play tone in a separate thread
        thread {
            val numSamples = sampleRate / 10 // Generate 100ms chunks
            val buffer = ShortArray(numSamples)
            var samplesGenerated = 0L

            while (!stopPlayback && samplesGenerated < durationMs * sampleRate / 1000) {
                for (i in 0 until numSamples) {
                    val sample = (sin(2 * Math.PI * frequency * (samplesGenerated + i) / sampleRate) * Short.MAX_VALUE * volume)
                    buffer[i] = sample.toInt().toShort() // Fixed: Explicit conversion to Int then Short
                }
                audioTrack?.write(buffer, 0, numSamples)
                samplesGenerated += numSamples
            }

            // Stop playback if duration is reached
            if (!stopPlayback) {
                handler.post {
                    stopTonePlayback()
                    isStarted = false
                    view?.findViewById<Button>(R.id.btn_start_stop)?.text = "Start"
                }
            }
        }

        // Schedule stop after duration
        handler.postDelayed({
            if (isStarted) {
                stopTonePlayback()
                isStarted = false
                view?.findViewById<Button>(R.id.btn_start_stop)?.text = "Start"
            }
        }, durationMs)
    }

    private fun stopTonePlayback() {
        stopPlayback = true
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        audioFocusRequest?.let { focusRequest ->
            audioManager?.abandonAudioFocusRequest(focusRequest) // Fixed: Safe handling of nullable audioFocusRequest
        }
        audioFocusRequest = null
        handler.removeCallbacksAndMessages(null) // Clear any pending stop tasks
        Log.d(TAG, "Tone playback stopped")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
        stopTonePlayback() // Ensure tone playback stops
        Log.d(TAG, "SoundPool and AudioTrack released")
    }
}
