package com.cs4347.drumkit

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.SeekBar
import android.widget.Toast
import com.cs4347.drumkit.view.BeatsAdapter
import com.cs4347.drumkit.view.DrumKitInstrumentsAdapter
import com.cs4347.drumkit.view.RowSelectionListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_generate_track.*
import kotlinx.android.synthetic.main.view_drumkit_instruments_view.view.*
import kotlinx.android.synthetic.main.view_instrument_row.view.*
import java.sql.Time
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GenerateTrackActivity : Activity() {

    companion object {
        private val tempoRange = Pair(60, 120)
        private const val tempoStep = 10
        // currently an arbitrary value, ensure it is between 1000/(24 to 120Hz), standard refresh rate
        private const val seekBarUpdatePeriod = 16L
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

    private lateinit var instrumentsAdapter: DrumKitInstrumentsAdapter
    private val disposables: CompositeDisposable = CompositeDisposable()

    private var seekBarMovementDisposable: Disposable? = null


    private var tempo = tempoRange.first
    private var selectedInstrumentRow: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_track)

        instrumentsAdapter = DrumKitInstrumentsAdapter(instruments, object: RowSelectionListener {
            override fun onRowSelected(row: Int) {
                selectedInstrumentRow = row
                Toast.makeText(this@GenerateTrackActivity,
                        "Row ${selectedInstrumentRow} selected",
                        Toast.LENGTH_SHORT).show()
            }
        })

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

        play.setOnClickListener {
            play()
        }

        record.setOnClickListener {
            // TODO add ML recognizer here
            play()
        }

        pause.setOnClickListener {
            pause()
        }

        drumkit_instruments.setSeekBarOnChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar?) {
                pause()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        clear.setOnClickListener {
            clearBeatRow()
        }

        setTempoText()
        setButtons(false)
        drumkit_instruments.seekBar.max =
                60 * 1000 * tempo * DrumKitInstrumentsAdapter.COLUMNS * seekBarUpdatePeriod.toInt()
    }

    private fun play() {
        if (selectedInstrumentRow == null) {
            drumkit_instruments.instrumentsRecycler.getChildAt(0).performClick()
        }
        setButtons(true)
        startSeekBarMovement()
    }

    private fun pause() {
        // todo: stop ml if possible
        setButtons(false)
        stopSeekBarMovement()
    }

    private fun setBeatView(col: Int, activate: Boolean) {
        selectedInstrumentRow?.let {
            val beatRowRecycler: RecyclerView = drumkit_instruments.instrumentsRecycler.getChildAt(it).instrument_beats_rv
            val beatRowAdapter = beatRowRecycler.adapter as BeatsAdapter
            beatRowAdapter.setColumn(col, activate)
        }
    }

    private fun clearBeatRow() {
        selectedInstrumentRow?.let {
            val beatRowRecycler: RecyclerView = drumkit_instruments.instrumentsRecycler.getChildAt(it).instrument_beats_rv
            val beatRowAdapter = beatRowRecycler.adapter as BeatsAdapter
            beatRowAdapter.clearAll()
        }
    }

    private fun setButtons(playingBack: Boolean) {
        play.isEnabled = !playingBack
        record.isEnabled = !playingBack
        tempoUp.isEnabled = !playingBack
        tempoDown.isEnabled = !playingBack
        clear.isEnabled = !playingBack

        pause.isEnabled = playingBack
    }

    private fun stopSeekBarMovement() {
        seekBarMovementDisposable?.dispose()
    }

    private fun startSeekBarMovement() {
        val timePerBeatMs = 60*1000/tempo.toFloat()
        val totalDuration: Float = timePerBeatMs * DrumKitInstrumentsAdapter.COLUMNS
        val timePerPercentage: Float = totalDuration / drumkit_instruments.seekBar.max
        val percentagePerTime: Float = 1/timePerPercentage

        var prevPosition = drumkit_instruments.seekBar.progress

        var prevTime = System.currentTimeMillis()

        seekBarMovementDisposable =
                Observable.interval(seekBarUpdatePeriod, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            val newTime = System.currentTimeMillis()
                            val timePassedMs = newTime - prevTime
                            val newPosition =
                                    (prevPosition + percentagePerTime*timePassedMs).roundToInt() %
                                            (drumkit_instruments.seekBar.max + 1)
                            drumkit_instruments.seekBar.progress = newPosition

                            prevPosition = newPosition
                            prevTime = newTime
                        }

        seekBarMovementDisposable?.let {
            disposables.add(it)
        }
    }

    private fun setTempoText() {
        tempoText.text = resources.getString(R.string.tempo_display, tempo)
    }

}
