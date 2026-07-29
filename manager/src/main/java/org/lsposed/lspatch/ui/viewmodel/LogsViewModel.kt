package org.lsposed.lspatch.ui.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.lspApp
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class LogLine(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String
)

class LogsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _logs = MutableStateFlow<List<LogLine>>(emptyList())
    val logs: StateFlow<List<LogLine>> = _logs.asStateFlow()

    private val _levelFilter = MutableStateFlow("All")
    val levelFilter: StateFlow<String> = _levelFilter.asStateFlow()

    private val levelOrder = listOf("V", "D", "I", "W", "E", "F")

    fun availableLevels(): List<String> = listOf("All", "V", "D", "I", "W", "E")

    val filteredLogs: List<LogLine>
        get() {
            val all = _logs.value
            val level = _levelFilter.value
            if (level == "All") return all
            val minIndex = levelOrder.indexOf(level).takeIf { it >= 0 } ?: return all
            return all.filter { line ->
                val idx = levelOrder.indexOf(line.level)
                idx >= 0 && idx >= minIndex
            }
        }

    fun updateLevelFilter(level: String) {
        _levelFilter.value = level
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _logs.value = withContext(Dispatchers.IO) { readLogcat() }
            _isLoading.value = false
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun buildShareIntent(): Intent {
        val text = buildText()
        val cacheDir: File = lspApp.cacheDir
        val file = File(cacheDir, "lspatch_logs_${System.currentTimeMillis()}.txt")
        file.writeText(text)
        val uri: Uri = runCatching {
            FileProvider.getUriForFile(
                lspApp,
                "${lspApp.packageName}.fileprovider",
                file
            )
        }.getOrElse { Uri.fromFile(file) }

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "LSPatch(ds) logs")
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun buildText(): String {
        val sb = StringBuilder()
        sb.appendLine("======== LSPatch(ds) Logs ========")
        sb.appendLine("Generated at: ${java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date())}")
        sb.appendLine()
        _logs.value.forEach { line ->
            sb.append("${line.timestamp} ${line.level}/${line.tag}: ${line.message}")
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun readLogcat(): List<LogLine> {
        val result = mutableListOf<LogLine>()
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "threadtime"))
            BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
                lines.forEach { raw ->
                    parseLogLine(raw)?.let { result.add(it) }
                }
            }
        }
        // Keep last ~4000 entries to avoid UI slowdowns
        return result.takeLast(4000)
    }

    // threadtime format: 05-20 12:34:56.789  1234  5678 D TagName : message
    private fun parseLogLine(raw: String): LogLine? {
        val line = raw.trim()
        if (line.isEmpty()) return null
        val regex = Regex("""^(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d+)\s+\d+\s+\d+\s+([VDIWEF])\s+([^:]+?)\s*:\s*(.*)$""")
        val match = regex.matchEntire(line) ?: run {
            // Fallback: prefix/space format I/Tag( xxx): message
            val fb = Regex("""^([VDIWEF])/([^(]+)\(\s*\d+\)\s*:\s*(.*)$""").matchEntire(line)
            if (fb != null) {
                return LogLine(
                    timestamp = "--:--:--",
                    level = fb.groupValues[1],
                    tag = fb.groupValues[2].trim(),
                    message = fb.groupValues[3]
                )
            }
            return null
        }
        return LogLine(
            timestamp = match.groupValues[1],
            level = match.groupValues[2],
            tag = match.groupValues[3].trim(),
            message = match.groupValues[4]
        )
    }
}
