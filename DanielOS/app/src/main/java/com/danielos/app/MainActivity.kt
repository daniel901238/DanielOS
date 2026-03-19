package com.danielos.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
    private lateinit var statusText: TextView
    private lateinit var runButton: Button

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var shellSession: ShellSession

    private val prefs by lazy {
        getSharedPreferences("danielos_terminal", Context.MODE_PRIVATE)
    }

    private val commandHistory = ArrayList<String>()
    private val pendingCommands = ArrayList<String>()
    private var historyCursor = -1

    @Volatile
    private var shellReady = false
    private var currentDir = "?"

    companion object {
        private const val KEY_CONSOLE_LOG = "console_log"
        private const val KEY_DRAFT_COMMAND = "draft_command"
        private const val KEY_HISTORY = "command_history"
        private const val MAX_LOG_CHARS = 40_000
        private const val MAX_HISTORY = 30
        private const val MARK_EXIT = "__EXIT__"
        private const val MARK_PWD = "__PWD__"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        outputText = findViewById(R.id.outputText)
        outputScroll = findViewById(R.id.outputScroll)
        inputCommand = findViewById(R.id.inputCommand)
        statusText = findViewById(R.id.statusText)
        runButton = findViewById(R.id.runButton)

        restoreUiState()
        setShellReady(false)

        val appHome = filesDir.absolutePath
        shellSession = PtyShellSession(ioExecutor, appHome)

        runButton.setOnClickListener {
            submitCurrentCommand()
        }

        // Termux-like: tap terminal area to type immediately.
        outputText.setOnClickListener { focusInputField() }
        outputScroll.setOnClickListener { focusInputField() }

        inputCommand.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                submitCurrentCommand()
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            outputText.text = "DanielOS v1.2 (termux-style info + extra keys)"
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
            appendLine("사용 예시: pwd, ls -al, uname -a, whoami")
            appendLine("v1.2: Termux 스타일 정보/프롬프트 + 특수키 바")
            appendPrompt()
        }

        findViewById<Button>(R.id.exportLogButton).setOnClickListener {
            exportLogToFile()
        }

        findViewById<Button>(R.id.copyLogButton).setOnClickListener {
            copyLogToClipboard()
        }

        findViewById<Button>(R.id.keyEscButton).setOnClickListener { insertAtCursor("\u001B") }
        findViewById<Button>(R.id.keyTabButton).setOnClickListener { insertAtCursor("\t") }
        findViewById<Button>(R.id.keySlashButton).setOnClickListener { insertAtCursor("/") }
        findViewById<Button>(R.id.keyDashButton).setOnClickListener { insertAtCursor("-") }
        findViewById<Button>(R.id.keyPipeButton).setOnClickListener { insertAtCursor("|") }
        findViewById<Button>(R.id.keyTildeButton).setOnClickListener { insertAtCursor("~") }

        printWelcomeBanner()
        startShellSession()
    }

    private fun setShellReady(ready: Boolean) {
        shellReady = ready
        runButton.isEnabled = ready
        val state = if (ready) "ready" else "starting"
        statusText.text = "mode=local-fallback | shell=$state | cwd=$currentDir"
    }

    private fun updateCurrentDir(path: String) {
        currentDir = path
        val state = if (shellReady) "ready" else "starting"
        statusText.text = "mode=local-fallback | shell=$state | cwd=$currentDir"
    }

    private fun focusInputField() {
        inputCommand.requestFocus()
        inputCommand.setSelection(inputCommand.text?.length ?: 0)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputCommand, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun submitCurrentCommand() {
        val cmd = inputCommand.text.toString().trim()
        if (cmd.isBlank()) return
        pushHistory(cmd)
        inputCommand.setText("")

        when (cmd.lowercase(Locale.ROOT)) {
            "help" -> {
                printHelp()
                return
            }
            "info" -> {
                printInfo()
                return
            }
        }

        if (!shellReady) {
            pendingCommands.add(cmd)
            appendLine("[queue] shell 준비 중이라 대기열에 추가됨: $cmd")
            appendPrompt()
            return
        }

        sendToShell(cmd)
    }

    private fun flushPendingCommands() {
        if (pendingCommands.isEmpty()) return
        val queued = pendingCommands.toList()
        pendingCommands.clear()
        appendLine("[queue] ${queued.size}개 명령 자동 실행")
        queued.forEach { sendToShell(it) }
    }

    private fun insertAtCursor(text: String) {
        val start = inputCommand.selectionStart.coerceAtLeast(0)
        val end = inputCommand.selectionEnd.coerceAtLeast(0)
        inputCommand.text?.replace(minOf(start, end), maxOf(start, end), text, 0, text.length)
    }

    private fun printWelcomeBanner() {
        appendLine("Welcome to DanielOS")
        appendLine("Type 'help' for shortcuts, 'info' for system summary")
    }

    private fun printHelp() {
        appendLine("Commands: pwd, ls -al, uname -a, whoami, cd <dir>")
        appendLine("Built-ins: help, info")
        appendPrompt()
    }

    private fun printInfo() {
        val stat = StatFs(filesDir.absolutePath)
        val avail = stat.availableBytes / (1024 * 1024)
        val total = stat.totalBytes / (1024 * 1024)
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine("ABI: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"}")
        appendLine("AppHome: ${filesDir.absolutePath}")
        appendLine("Storage(app fs): ${avail}MB free / ${total}MB total")
        appendLine("Shell: ${if (shellReady) "ready" else "starting"}, cwd=$currentDir")
        appendPrompt()
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
        setShellReady(false)
        shellSession.start(
            onLine = { line ->
                runOnUiThread {
                    when {
                        line.startsWith(MARK_EXIT) -> {
                            val code = line.removePrefix(MARK_EXIT)
                            appendLine("[exit=$code]")
                        }

                        line.startsWith(MARK_PWD) -> {
                            updateCurrentDir(line.removePrefix(MARK_PWD))
                        }

                        else -> appendLine(line)
                    }

                    if (line.contains("[session] shell started")) {
                        setShellReady(true)
                        appendPrompt()
                        flushPendingCommands()
                    }
                }
            },
            onExit = { code ->
                runOnUiThread {
                    setShellReady(false)
                    appendLine("[session] shell exited (code=$code)")
                }
            },
            onError = { message ->
                runOnUiThread {
                    setShellReady(false)
                    appendLine("[error] shell start failed: $message")
                }
            }
        )
    }

    private fun restartShellSession() {
        ioExecutor.execute {
            shellSession.stop()
            runOnUiThread {
                setShellReady(false)
                appendLine("[session] restarting...")
            }
            startShellSession()
        }
    }

    private fun interruptCurrentSession() {
        shellSession.interrupt()
            .onSuccess {
                appendLine("[session] interrupted")
                setShellReady(false)
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
        val wrapped = "{ $command; _ec=\$?; printf '$MARK_EXIT%s\\n' \"\$_ec\"; printf '$MARK_PWD%s\\n' \"\$PWD\"; }"
        shellSession.send(wrapped)
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
            "DanielOS v1.2 (termux-style info + extra keys)"
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
