package com.example.sonicwavejune23.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sonicwavejune23.databinding.FragmentHomeBinding

import android.os.Handler
import android.os.Looper
import android.widget.Button

import com.example.sonicwavejune23.R


class HomeFragment : Fragment() {

    private var frequency = 0
    private var intensity = 0
    private var time = 0
    private var isStarted = false // Track Start/Stop state
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private val longPressInterval = 100L // Update every 100ms during long press

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Frequency controls (Up, Down, Value Display)
        val btnFrequencyUp: Button = view.findViewById(R.id.btn_frequency_up)
        val btnFrequencyDown: Button = view.findViewById(R.id.btn_frequency_down)
        val tvFrequencyValue: TextView = view.findViewById(R.id.tv_frequency_value)

        // Intensity controls (Up, Down, Value Display)
        val btnIntensityUp: Button = view.findViewById(R.id.btn_intensity_up)
        val btnIntensityDown: Button = view.findViewById(R.id.btn_intensity_down)
        val tvIntensityValue: TextView = view.findViewById(R.id.tv_intensity_value)

        // Time controls (Up, Down, Value Display)
        val btnTimeUp: Button = view.findViewById(R.id.btn_time_up)
        val btnTimeDown: Button = view.findViewById(R.id.btn_time_down)
        val tvTimeValue: TextView = view.findViewById(R.id.tv_time_value)

        // Start/Stop control (Single Toggle Button)
        val btnStartStop: Button = view.findViewById(R.id.btn_start_stop)

        // Frequency button listeners
        btnFrequencyUp.setOnClickListener {
            frequency += 1
            updateFrequencyDisplay(tvFrequencyValue)
        }
        btnFrequencyDown.setOnClickListener {
            if (frequency > 0) frequency -= 1
            updateFrequencyDisplay(tvFrequencyValue)
        }
        setupLongPress(btnFrequencyUp, { frequency += 1 }, tvFrequencyValue, ::updateFrequencyDisplay)
        setupLongPress(btnFrequencyDown, { if (frequency > 0) frequency -= 1 }, tvFrequencyValue, ::updateFrequencyDisplay)

        // Intensity button listeners
        btnIntensityUp.setOnClickListener {
            intensity += 1
            updateIntensityDisplay(tvIntensityValue)
        }
        btnIntensityDown.setOnClickListener {
            if (intensity > 0) intensity -= 1
            updateIntensityDisplay(tvIntensityValue)
        }
        setupLongPress(btnIntensityUp, { intensity += 1 }, tvIntensityValue, ::updateIntensityDisplay)
        setupLongPress(btnIntensityDown, { if (intensity > 0) intensity -= 1 }, tvIntensityValue, ::updateIntensityDisplay)

        // Time button listeners
        btnTimeUp.setOnClickListener {
            time += 1
            updateTimeDisplay(tvTimeValue)
        }
        btnTimeDown.setOnClickListener {
            if (time > 0) time -= 1
            updateTimeDisplay(tvTimeValue)
        }
        setupLongPress(btnTimeUp, { time += 1 }, tvTimeValue, ::updateTimeDisplay)
        setupLongPress(btnTimeDown, { if (time > 0) time -= 1 }, tvTimeValue, ::updateTimeDisplay)

        // Start/Stop button listener
        btnStartStop.setOnClickListener {
            isStarted = !isStarted
            btnStartStop.text = if (isStarted) "Stop" else "Start"
        }

        return view
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
        handler.removeCallbacksAndMessages(null) // Clean up handler
    }
}