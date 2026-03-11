package com.danielos.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
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
    private var historyCursor = -1

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
            submitCurrentCommand()
        }

        inputCommand.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                submitCurrentCommand()
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            outputText.text = "DanielOS v0.7 (interactive shell + UX)"
            appendPrompt()
            persistUiState()
        }

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            restartShellSession()
        }

        findViewById<Button>(R.id.interruptButton).setOnClickListener {
            interruptCurrentSession()
        }

        findViewById<Button>(R.id.prevHistoryButton).setOnClickListener {
            browseHistory(previous = true)
        }

        findViewById<Button>(R.id.nextHistoryButton).setOnClickListener {
            browseHistory(previous = false)
        }

        findViewById<Button>(R.id.helpButton).setOnClickListener {
            appendLine("사용 예시: pwd, ls, uname -a, whoami")
            appendLine("v0.7: 엔터 전송/로그복사/입력 UX 개선")
            appendPrompt()
        }

        findViewById<Button>(R.id.exportLogButton).setOnClickListener {
            exportLogToFile()
        }

        findViewById<Button>(R.id.copyLogButton).setOnClickListener {
            copyLogToClipboard()
        }

        startShellSession()
    }

    private fun submitCurrentCommand() {
        val cmd = inputCommand.text.toString().trim()
        if (cmd.isBlank()) return
        pushHistory(cmd)
        inputCommand.setText("")
        sendToShell(cmd)
    }

    private fun copyLogToClipboard() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = outputText.text?.toString() ?: ""
            clipboard.setPrimaryClip(ClipData.newPlainText("DanielOS Log", text))
            appendLine("[log] copied to clipboard")
            appendPrompt()
        } catch (e: Exception) {
            appendLine("[error] copy failed: ${e.message}")
            appendPrompt()
        }
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
            gracefulExitShell()

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

    private fun interruptCurrentSession() {
        ioExecutor.execute {
            try {
                // MVP interrupt: 강제 종료 후 새 세션 시작
                shellProcess?.destroy()
                shellWriter = null
                shellProcess = null
                runOnUiThread {
                    appendLine("[session] interrupted")
                }
                startShellSession()
            } catch (e: Exception) {
                runOnUiThread {
                    appendLine("[error] interrupt failed: ${e.message}")
                }
            }
        }
    }

    private fun gracefulExitShell() {
        try {
            shellWriter?.apply {
                write("exit\n")
                flush()
            }
        } catch (_: Exception) {
        }
    }

    private fun browseHistory(previous: Boolean) {
        if (commandHistory.isEmpty()) return

        if (previous) {
            if (historyCursor < commandHistory.lastIndex) historyCursor++
        } else {
            if (historyCursor >= 0) historyCursor--
        }

        val value = if (historyCursor in commandHistory.indices) {
            commandHistory[historyCursor]
        } else {
            ""
        }

        inputCommand.setText(value)
        inputCommand.setSelection(value.length)
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
        historyCursor = -1
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
            "DanielOS v0.7 (interactive shell + UX)"
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

        gracefulExitShell()

        try {
            shellWriter?.close()
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
