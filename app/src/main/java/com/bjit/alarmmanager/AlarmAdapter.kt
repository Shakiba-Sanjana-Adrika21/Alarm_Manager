package com.bjit.alarmmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale

class AlarmAdapter(
    private val alarms: MutableList<Alarm>,
    private val onEdit: (Alarm) -> Unit,
    private val onDelete: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.textViewTime)
        val editButton: Button = view.findViewById(R.id.buttonEdit)
        val deleteButton: Button = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeTextView.text = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(
            Date(alarm.timeInMillis)
        )
        holder.editButton.setOnClickListener { onEdit(alarm) }
        holder.deleteButton.setOnClickListener { onDelete(alarm) }
    }

    override fun getItemCount() = alarms.size
}