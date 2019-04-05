package com.cs4347.drumkit

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.cs4347.drumkit.view.DrumKitInstrumentsAdapter
import kotlinx.android.synthetic.main.activity_generate_track.*
import kotlinx.android.synthetic.main.view_drumkit_instruments_view.*

class GenerateTrackActivity : Activity() {

    private var selectedInstrumentRow = 0
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_track)

        drumkit_instruments.instrumentsRecycler.apply {
            this.adapter = instrumentsAdapter
            this.layoutManager = LinearLayoutManager(this@GenerateTrackActivity)
        }

    }
}
