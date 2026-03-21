package com.danielos.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.graphics.Rect
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var terminalEdit: EditText
    private lateinit var statusText: TextView
    private lateinit var bottomControls: View

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var shellSession: ShellSession

    @Volatile
    private var shellReady = false
    private var currentDir = "?"
    private var inputStart = 0
    private var internalEdit = false

    companion object {
        private const val MARK_PWD = "__PWD__"
        private const val PROMPT = "$ "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        terminalEdit = findViewById(R.id.terminalEdit)
        statusText = findViewById(R.id.statusText)
        bottomControls = findViewById(R.id.bottomControls)

        val appHome = filesDir.absolutePath
        shellSession = PtyShellSession(ioExecutor, appHome)

        terminalEdit.setOnClickListener { forceCursorToEnd() }
        terminalEdit.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) forceCursorToEnd() }

        terminalEdit.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    submitCurrentCommandFromTerminal()
                    true
                }
                KeyEvent.KEYCODE_DEL -> {
                    if (terminalEdit.selectionStart <= inputStart) true else false
                }
                else -> false
            }
        }

        terminalEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (internalEdit) return
                if (terminalEdit.selectionStart < inputStart) {
                    forceCursorToEnd()
                }
            }
        })

        findViewById<Button>(R.id.restartButton).setOnClickListener { restartShellSession() }
        findViewById<Button>(R.id.interruptButton).setOnClickListener { interruptCurrentSession() }
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            setTerminalText("DanielOS direct terminal mode\n")
            appendPrompt()
        }
        findViewById<Button>(R.id.helpButton).setOnClickListener {
            appendOutput("Built-ins: help, info")
            appendOutput("Linux cmds: pwd, ls -al, uname -a, whoami")
            appendPrompt()
        }

        // Extra-key buttons
        findViewById<Button>(R.id.keyEscButton).setOnClickListener { insertAtCursor("\u001B") }
        findViewById<Button>(R.id.keyTabButton).setOnClickListener { insertAtCursor("\t") }
        findViewById<Button>(R.id.keyCtrlButton).setOnClickListener { insertAtCursor("^") }
        findViewById<Button>(R.id.keyAltButton).setOnClickListener { insertAtCursor("ALT+") }
        findViewById<Button>(R.id.keySlashButton).setOnClickListener { insertAtCursor("/") }
        findViewById<Button>(R.id.keyDashButton).setOnClickListener { insertAtCursor("-") }
        findViewById<Button>(R.id.keyPipeButton).setOnClickListener { insertAtCursor("|") }
        findViewById<Button>(R.id.keyTildeButton).setOnClickListener { insertAtCursor("~") }

        findViewById<Button>(R.id.keyHomeButton).setOnClickListener { moveCursorToCommandStart() }
        findViewById<Button>(R.id.keyEndButton).setOnClickListener { moveCursorToEnd() }
        findViewById<Button>(R.id.keyLeftButton).setOnClickListener { moveCursorBy(-1) }
        findViewById<Button>(R.id.keyRightButton).setOnClickListener { moveCursorBy(1) }
        findViewById<Button>(R.id.keyUpButton).setOnClickListener { moveCursorToCommandStart() }
        findViewById<Button>(R.id.keyDownButton).setOnClickListener { moveCursorToEnd() }
        findViewById<Button>(R.id.keyPgUpButton).setOnClickListener { insertAtCursor(" --help") }
        findViewById<Button>(R.id.keyPgDnButton).setOnClickListener { insertAtCursor(" | less") }

        setupKeyboardAwareControls()

        setTerminalText("Welcome to DanielOS\n\nDocs:      https://danielos-temp.github.io\nCommunity: https://github.com/daniel901238/DanielOS\n\nUse: help, info, ls -al\n")
        startShellSession()
    }

    private fun setupKeyboardAwareControls() {
        val root = findViewById<View>(android.R.id.content)
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                root.getWindowVisibleDisplayFrame(r)
                val screenHeight = root.rootView.height
                if (screenHeight <= 0) return
                val keypadHeight = screenHeight - r.bottom
                val keyboardVisible = keypadHeight > screenHeight * 0.15
                bottomControls.visibility = if (keyboardVisible) View.VISIBLE else View.GONE
            }
        })
    }

    private fun setShellReady(ready: Boolean) {
        shellReady = ready
        val state = if (ready) "ready" else "starting"
        statusText.text = "mode=direct-terminal | shell=$state | cwd=$currentDir"
    }

    private fun setTerminalText(text: String) {
        internalEdit = true
        terminalEdit.setText(text)
        terminalEdit.setSelection(terminalEdit.text?.length ?: 0)
        internalEdit = false
        inputStart = terminalEdit.text?.length ?: 0
    }

    private fun appendOutput(text: String) {
        internalEdit = true
        val old = terminalEdit.text?.toString().orEmpty()
        val next = old + text + "\n"
        terminalEdit.setText(next)
        terminalEdit.setSelection(terminalEdit.text?.length ?: 0)
        internalEdit = false
        inputStart = terminalEdit.text?.length ?: 0
    }

    private fun appendPrompt() {
        internalEdit = true
        val old = terminalEdit.text?.toString().orEmpty()
        val next = old + PROMPT
        terminalEdit.setText(next)
        terminalEdit.setSelection(terminalEdit.text?.length ?: 0)
        internalEdit = false
        inputStart = terminalEdit.text?.length ?: 0
        forceCursorToEnd()
    }

    private fun insertAtCursor(text: String) {
        val editable = terminalEdit.text ?: return
        val cur = terminalEdit.selectionStart.coerceAtLeast(inputStart)
        editable.insert(cur, text)
        terminalEdit.setSelection((cur + text.length).coerceAtMost(editable.length))
    }

    private fun moveCursorToCommandStart() {
        terminalEdit.setSelection(inputStart.coerceAtMost(terminalEdit.text?.length ?: 0))
    }

    private fun moveCursorToEnd() {
        terminalEdit.setSelection(terminalEdit.text?.length ?: 0)
    }

    private fun moveCursorBy(delta: Int) {
        val len = terminalEdit.text?.length ?: 0
        val cur = terminalEdit.selectionStart.coerceAtLeast(inputStart)
        val next = (cur + delta).coerceIn(inputStart, len)
        terminalEdit.setSelection(next)
    }

    private fun forceCursorToEnd() {
        terminalEdit.post {
            val end = terminalEdit.text?.length ?: 0
            terminalEdit.setSelection(end)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(terminalEdit, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun submitCurrentCommandFromTerminal() {
        val full = terminalEdit.text?.toString().orEmpty()
        if (full.length < inputStart) return
        val cmd = full.substring(inputStart).trim()

        if (cmd.isBlank()) {
            appendOutput("")
            appendPrompt()
            return
        }

        // Move output to next line after user-entered command.
        internalEdit = true
        val withNewline = terminalEdit.text?.toString().orEmpty() + "\n"
        terminalEdit.setText(withNewline)
        terminalEdit.setSelection(terminalEdit.text?.length ?: 0)
        internalEdit = false

        when (cmd.lowercase(Locale.ROOT)) {
            "help" -> {
                appendOutput("Built-ins: help, info")
                appendPrompt()
                return
            }
            "info" -> {
                appendOutput("AppHome: ${filesDir.absolutePath}")
                appendOutput("Shell: ${if (shellReady) "ready" else "starting"}, cwd=$currentDir")
                appendPrompt()
                return
            }
        }

        if (!shellReady) {
            appendOutput("[wait] shell not ready")
            appendPrompt()
            return
        }

        val wrapped = "{ $cmd; printf '$MARK_PWD%s\\n' \"\$PWD\"; }"
        shellSession.send(wrapped).onFailure {
            appendOutput("[error] send failed: ${it.message}")
            appendPrompt()
        }
    }

    private fun startShellSession() {
        setShellReady(false)
        shellSession.start(
            onLine = { line ->
                runOnUiThread {
                    when {
                        line.startsWith(MARK_PWD) -> {
                            currentDir = line.removePrefix(MARK_PWD)
                            setShellReady(shellReady)
                            appendPrompt()
                        }
                        else -> appendOutput(line)
                    }
                    if (line.contains("[session] shell started")) {
                        setShellReady(true)
                        appendPrompt()
                    }
                }
            },
            onExit = { code -> runOnUiThread { appendOutput("[session] shell exited (code=$code)") } },
            onError = { msg -> runOnUiThread { appendOutput("[error] shell start failed: $msg") } }
        )
    }

    private fun restartShellSession() {
        ioExecutor.execute {
            shellSession.stop()
            runOnUiThread { appendOutput("[session] restarting...") }
            startShellSession()
        }
    }

    private fun interruptCurrentSession() {
        shellSession.interrupt()
            .onSuccess {
                appendOutput("[session] interrupted")
                startShellSession()
            }
            .onFailure { appendOutput("[error] interrupt failed: ${it.message}") }
    }

    override fun onDestroy() {
        shellSession.stop()
        ioExecutor.shutdownNow()
        super.onDestroy()
    }
}
