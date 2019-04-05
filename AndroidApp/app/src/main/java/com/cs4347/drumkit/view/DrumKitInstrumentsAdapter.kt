package com.cs4347.drumkit.view

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cs4347.drumkit.R

class DrumKitRowViewHolder(viewItem: View): RecyclerView.ViewHolder(viewItem) {
    val nameTextView: TextView = viewItem.findViewById(R.id.instrument_name_textview)
    val beatsRecyclerView: RecyclerView = viewItem.findViewById(R.id.instrument_beats_view)
}

class DrumKitInstrumentsAdapter(private val instrumentNames: List<Pair<String, Int>>):
        RecyclerView.Adapter<DrumKitRowViewHolder>() {

    companion object {
        const val COLUMNS = 16
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrumKitRowViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val instrumentView = inflater.inflate(R.layout.view_instrument_row, parent, false)
        val vh = DrumKitRowViewHolder(instrumentView)

        vh.beatsRecyclerView.apply {
            this.adapter = BeatsAdapter(COLUMNS)
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        return vh
    }

    override fun getItemCount(): Int {
        return instrumentNames.size
    }

    override fun onBindViewHolder(holder: DrumKitRowViewHolder, position: Int) {
        val (name, colorId) = instrumentNames[position]
        holder.nameTextView.text = name
        holder.nameTextView.background = ContextCompat.getDrawable(holder.nameTextView.context, colorId)

        // todo: init the beats recycler view here
        // put a separator too
    }

    override fun getItemViewType(position: Int): Int {
        // hack to make all view items 'unique'
        // unique items are never recycled
        return position
    }

    fun setBeatView(row: Int, col: Int, activate: Boolean) {
        // TODO, implement
    }

    fun clearBeatViewRow(row: Int) {
        // TODO, implement
    }
}

