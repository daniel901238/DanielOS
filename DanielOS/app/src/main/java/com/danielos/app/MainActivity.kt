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
import java.io.File
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

    private lateinit var shellSession: ShellSession

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
        // v0.9: PTY 세션 우선 시도(현재는 내부적으로 LocalShellSession 폴백)
        shellSession = PtyShellSession(ioExecutor)

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
            outputText.text = "DanielOS v0.9 (pty-ready scaffold + fallback)"
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
            appendLine("v0.9: PTY 브리지 스켈레톤 + 자동 폴백")
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
        shellSession.start(
            onLine = { line ->
                runOnUiThread {
                    appendLine(line)
                    if (line.contains("[session] shell started")) appendPrompt()
                }
            },
            onExit = { code ->
                runOnUiThread { appendLine("[session] shell exited (code=$code)") }
            },
            onError = { message ->
                runOnUiThread { appendLine("[error] shell start failed: $message") }
            }
        )
    }

    private fun restartShellSession() {
        ioExecutor.execute {
            shellSession.stop()
            runOnUiThread { appendLine("[session] restarting...") }
            startShellSession()
        }
    }

    private fun interruptCurrentSession() {
        shellSession.interrupt()
            .onSuccess {
                appendLine("[session] interrupted")
                startShellSession()
            }
            .onFailure { e ->
                appendLine("[error] interrupt failed: ${e.message}")
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
        shellSession.send(command)
            .onSuccess { appendPrompt() }
            .onFailure { e ->
                appendLine("[error] send failed: ${e.message}")
                appendPrompt()
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
            "DanielOS v0.9 (pty-ready scaffold + fallback)"
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
        shellSession.stop()
        ioExecutor.shutdownNow()
        super.onDestroy()
    }
}
