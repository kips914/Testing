package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConsoleLog(
    val level: String, // "INFO", "WARN", "ERROR"
    val timestamp: String,
    val message: String
)

class SessionLogger {
    private val _logs = MutableStateFlow<List<ConsoleLog>>(emptyList())
    val logs: StateFlow<List<ConsoleLog>> = _logs.asStateFlow()

    fun log(level: String, message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logs.value = listOf(ConsoleLog(level, time, message)) + _logs.value
    }

    fun info(message: String) = log("INFO", message)
    fun warn(message: String) = log("WARN", message)
    fun error(message: String) = log("ERROR", message)
}
