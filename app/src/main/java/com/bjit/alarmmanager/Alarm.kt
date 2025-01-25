package com.bjit.alarmmanager

data class Alarm(
    val id: Int,
    var timeInMillis: Long,
    var audioUri: String? = null  // Add audioUri property
)