package com.danielos.app

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var outputText: TextView
    private lateinit var outputScroll: ScrollView
    private lateinit var inputCommand: EditText

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @Volatile
    private var shellProcess: Process? = null
    private var shellWriter: BufferedWriter? = null

    private val prefs by lazy {
        getSharedPreferences("danielos_terminal", Context.MODE_PRIVATE)
    }

    private val commandHistory = ArrayList<String>()

    companion object {
        private const val KEY_CONSOLE_LOG = "console_log"
        private const val KEY_DRAFT_COMMAND = "draft_command"
        private const val KEY_HISTORY = "command_history"
        private const val MAX_LOG_CHARS = 40_000
        private const val MAX_HISTORY = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        outputText = findViewById(R.id.outputText)
        outputScroll = findViewById(R.id.outputScroll)
        inputCommand = findViewById(R.id.inputCommand)

        restoreUiState()

        findViewById<Button>(R.id.runButton).setOnClickListener {
            val cmd = inputCommand.text.toString().trim()
            if (cmd.isBlank()) return@setOnClickListener
            pushHistory(cmd)
            inputCommand.setText("")
            sendToShell(cmd)
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            outputText.text = "DanielOS v0.5 (interactive shell + persisted log/history)"
            appendPrompt()
            persistUiState()
        }

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            restartShellSession()
        }

        findViewById<Button>(R.id.helpButton).setOnClickListener {
            appendLine("사용 예시: pwd, ls, uname -a, whoami")
            appendLine("v0.5: 로그 복구 + 최근 명령 히스토리 + 로그 파일 저장")
            appendPrompt()
        }

        findViewById<Button>(R.id.exportLogButton).setOnClickListener {
            exportLogToFile()
        }

        startShellSession()
    }

    private fun startShellSession() {
        ioExecutor.execute {
            try {
                val proc = ProcessBuilder("sh")
                    .redirectErrorStream(true)
                    .start()

                shellProcess = proc
                shellWriter = BufferedWriter(OutputStreamWriter(proc.outputStream))

                runOnUiThread {
                    appendLine("[session] shell started")
                    appendPrompt()
                }

                BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val safeLine = line ?: continue
                        runOnUiThread { appendLine(safeLine) }
                    }
                }

                val code = proc.waitFor()
                runOnUiThread {
                    appendLine("[session] shell exited (code=$code)")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    appendLine("[error] shell start failed: ${e.message}")
                }
            }
        }
    }

    private fun restartShellSession() {
        ioExecutor.execute {
            try {
                shellWriter?.apply {
                    write("exit\n")
                    flush()
                }
            } catch (_: Exception) {
            }

            try {
                shellProcess?.destroy()
            } catch (_: Exception) {
            }

            shellWriter = null
            shellProcess = null

            runOnUiThread {
                appendLine("[session] restarting...")
            }

            startShellSession()
        }
    }

    private fun sendToShell(command: String) {
        appendLine("$ $command")
        ioExecutor.execute {
            try {
                val writer = shellWriter
                if (writer == null) {
                    runOnUiThread {
                        appendLine("[warn] shell not ready")
                        appendPrompt()
                    }
                    return@execute
                }

                writer.write(command)
                writer.newLine()
                writer.flush()

                runOnUiThread { appendPrompt() }
            } catch (e: Exception) {
                runOnUiThread {
                    appendLine("[error] send failed: ${e.message}")
                    appendPrompt()
                }
            }
        }
    }

    private fun exportLogToFile() {
        try {
            val now = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outFile = File(filesDir, "terminal-log-$now.txt")
            outFile.writeText(outputText.text?.toString() ?: "")
            appendLine("[log] saved: ${outFile.absolutePath}")
            appendPrompt()
        } catch (e: Exception) {
            appendLine("[error] log save failed: ${e.message}")
            appendPrompt()
        }
    }

    private fun pushHistory(cmd: String) {
        commandHistory.remove(cmd)
        commandHistory.add(0, cmd)
        if (commandHistory.size > MAX_HISTORY) {
            commandHistory.removeAt(commandHistory.lastIndex)
        }
        persistUiState()
    }

    private fun appendLine(text: String) {
        outputText.append("\n$text")
        trimLogIfNeeded()
        persistUiState()
        outputScroll.post { outputScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun appendPrompt() {
        outputText.append("\n$ ")
        trimLogIfNeeded()
        persistUiState()
        outputScroll.post { outputScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun trimLogIfNeeded() {
        val current = outputText.text?.toString() ?: return
        if (current.length <= MAX_LOG_CHARS) return

        val trimmed = current.takeLast(MAX_LOG_CHARS)
        outputText.text = "...[trimmed]\n$trimmed"
    }

    private fun restoreUiState() {
        val savedLog = prefs.getString(KEY_CONSOLE_LOG, null)
        val savedDraft = prefs.getString(KEY_DRAFT_COMMAND, "") ?: ""
        val savedHistoryRaw = prefs.getString(KEY_HISTORY, "") ?: ""

        commandHistory.clear()
        if (savedHistoryRaw.isNotBlank()) {
            commandHistory.addAll(savedHistoryRaw.split("\n").filter { it.isNotBlank() }.take(MAX_HISTORY))
        }

        outputText.text = if (savedLog.isNullOrBlank()) {
            "DanielOS v0.5 (interactive shell + persisted log/history)"
        } else {
            savedLog
        }

        inputCommand.setText(savedDraft)
        outputScroll.post { outputScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun persistUiState() {
        prefs.edit()
            .putString(KEY_CONSOLE_LOG, outputText.text?.toString() ?: "")
            .putString(KEY_DRAFT_COMMAND, inputCommand.text?.toString() ?: "")
            .putString(KEY_HISTORY, commandHistory.joinToString("\n"))
            .apply()
    }

    override fun onPause() {
        persistUiState()
        super.onPause()
    }

    override fun onDestroy() {
        persistUiState()

        try {
            shellWriter?.apply {
                write("exit\n")
                flush()
                close()
            }
        } catch (_: Exception) {
        }

        try {
            shellProcess?.destroy()
        } catch (_: Exception) {
        }

        ioExecutor.shutdownNow()
        super.onDestroy()
    }
}
