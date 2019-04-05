package com.cs4347.drumkit

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.cs4347.drumkit.view.DrumKitInstrumentsAdapter
import kotlinx.android.synthetic.main.activity_generate_track.*
import kotlin.math.max
import kotlin.math.min

class GenerateTrackActivity : Activity() {

    companion object {
        private val tempoRange = Pair(60, 120)
        private const val tempoStep = 10
    }

    private val instruments = listOf(
            "Kick" to R.color.colorKick,
            "Cymbal" to R.color.colorFingerCymbal,
            "Clap" to R.color.colorClap,
            "Splash" to R.color.colorSplash,
            "HiHat" to R.color.colorHiHat,
            "Scratch" to R.color.colorScratch,
            "Rim" to R.color.colorRim
    )
    private val instrumentsAdapter = DrumKitInstrumentsAdapter(instruments)

    private var tempo = tempoRange.first
    private var selectedInstrumentRow = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_track)

        drumkit_instruments.instrumentsRecycler.apply {
            this.adapter = instrumentsAdapter
            this.layoutManager = LinearLayoutManager(this@GenerateTrackActivity)
        }

        tempoUp.setOnClickListener {
            tempo = min(tempoRange.second, tempo + tempoStep)
            setTempoText()
        }

        tempoDown.setOnClickListener {
            tempo = max(tempoRange.first, tempo - tempoStep)
            setTempoText()
        }

        setTempoText()

    }

    private fun setTempoText() {
        tempoText.text = resources.getString(R.string.tempo_display, tempo)
    }

}
