package com.bjit.alarmmanager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var btnSetAlarm: Button
    lateinit var timePicker: TimePicker
    lateinit var recyclerViewAlarms: RecyclerView
    lateinit var alarmAdapter: AlarmAdapter
    private val alarms = mutableListOf<Alarm>()
    private var alarmId = 0
    private val PICK_AUDIO_REQUEST = 1
    private var selectedAudioUri: Uri? = null
    val popularSounds = listOf(
        R.raw.sound1,
        R.raw.sound2,
        R.raw.sound3
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnSelectAudio: Button = findViewById(R.id.buttonSelectAudio)
        btnSelectAudio.setOnClickListener {
            selectPopularSound()
        }
        title = "Alarm App"
        timePicker = findViewById(R.id.timePicker)
        btnSetAlarm = findViewById(R.id.buttonAlarm)
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms)

        recyclerViewAlarms.layoutManager = LinearLayoutManager(this)
        alarmAdapter = AlarmAdapter(alarms, this::editAlarm, this::deleteAlarm)
        recyclerViewAlarms.adapter = alarmAdapter

        btnSetAlarm.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            if (Build.VERSION.SDK_INT >= 23) {
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    timePicker.hour,
                    timePicker.minute,
                    0
                )
            } else {
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    timePicker.currentHour,
                    timePicker.currentMinute, 0
                )
            }
            val alarm = Alarm(alarmId++, calendar.timeInMillis, selectedAudioUri.toString())
            alarms.add(alarm)
            alarmAdapter.notifyDataSetChanged()
            setAlarm(alarm)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedAudioUri = data?.data
        }
    }
    private fun selectPopularSound() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_sound, null)

        val buttonPlaySound: Button = dialogView.findViewById(R.id.buttonPlaySound)
        val radioSound1: RadioButton = dialogView.findViewById(R.id.radioSound1)
        val radioSound2: RadioButton = dialogView.findViewById(R.id.radioSound2)
        val radioSound3: RadioButton = dialogView.findViewById(R.id.radioSound3)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = builder.create()

        // Set the Play Sound button listener
        buttonPlaySound.setOnClickListener {
            selectedAudioUri = when {
                radioSound1.isChecked -> Uri.parse("android.resource://${packageName}/${popularSounds[0]}")
                radioSound2.isChecked -> Uri.parse("android.resource://${packageName}/${popularSounds[1]}")
                radioSound3.isChecked -> Uri.parse("android.resource://${packageName}/${popularSounds[2]}")
                else -> null
            }

            if (selectedAudioUri != null) {
                playPreviewSound(selectedAudioUri!!)
            } else {
                Toast.makeText(this, "Please select a sound first", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun playPreviewSound(uri: Uri) {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(this, uri)  // Set the data source to the selected sound
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Alarm Preview", "Error playing the preview sound")
        } finally {
            mediaPlayer.setOnCompletionListener {
                it.release()  // Release the MediaPlayer after playback is complete
            }
        }
    }

    private fun setAlarm(alarm: Alarm) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MyAlarm::class.java).apply {
            putExtra("AUDIO_URI", alarm.audioUri)  // Pass the selected audio URI to the AlarmReceiver
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setRepeating(
            AlarmManager.RTC,
            alarm.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingInflatedId")
    private fun editAlarm(alarm: Alarm) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarm.timeInMillis

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_alarm, null)
        val timePicker: TimePicker = dialogView.findViewById(R.id.timePickerEdit)
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(Calendar.MINUTE)
        } else {
            timePicker.currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.currentMinute = calendar.get(Calendar.MINUTE)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Alarm")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (Build.VERSION.SDK_INT >= 23) {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    calendar.set(Calendar.MINUTE, timePicker.minute)
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.currentHour)
                    calendar.set(Calendar.MINUTE, timePicker.currentMinute)
                }
                alarm.timeInMillis = calendar.timeInMillis
                alarmAdapter.notifyDataSetChanged()
                setAlarm(alarm)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAlarm(alarm: Alarm) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MyAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        alarms.remove(alarm)
        alarmAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Alarm is deleted", Toast.LENGTH_SHORT).show()
    }

    class MyAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("Alarm Bell", "Alarm just fired")

            val audioUri = intent.getStringExtra("AUDIO_URI")  // Get the audio URI from the intent
            if (audioUri != null) {
                try {
                    val mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, Uri.parse(audioUri))  // Set the data source to the audio URI
                        prepare()  // Prepare the MediaPlayer
                        start()  // Start playing the audio
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("Alarm Bell", "Error playing the audio")
                }
            } else {
                Log.e("Alarm Bell", "Audio URI is null")
            }
        }
    }
}