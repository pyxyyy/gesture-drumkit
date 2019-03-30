package com.cs4347.drumkit.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.cs4347.drumkit.R


class DrumKitSeekBar: RelativeLayout {

    private lateinit var seekBar: SeekBar
    private lateinit var seekLine: View

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {

        LayoutInflater.from(context).inflate(R.layout.view_drumkit_seekbar, this, true)

        seekLine = getChildAt(0) as View
        seekBar = getChildAt(1) as SeekBar

        // set a dummy listener so the seek line follows the seekbar thumb
        this.setSeekBarOnChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    fun setSeekBarOnChangeListener(listener: SeekBar.OnSeekBarChangeListener) {
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val width = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight
                val thumbPos = seekBar.paddingLeft + width * seekBar.progress.toFloat() / seekBar.max
                seekLine.x = thumbPos
                listener.onProgressChanged(p0, p1, p2)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                listener.onStartTrackingTouch(p0)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                listener.onStopTrackingTouch(p0)
            }
        })
    }

}