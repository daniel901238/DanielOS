package com.danielos.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var outputText: TextView
    private lateinit var outputScroll: ScrollView
    private lateinit var inputCommand: EditText

    private val ioExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        outputText = findViewById(R.id.outputText)
        outputScroll = findViewById(R.id.outputScroll)
        inputCommand = findViewById(R.id.inputCommand)

        findViewById<Button>(R.id.runButton).setOnClickListener {
            val cmd = inputCommand.text.toString().trim()
            if (cmd.isEmpty()) return@setOnClickListener
            appendLine("$ $cmd")
            inputCommand.setText("")
            runCommand(cmd)
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            outputText.text = "DanielOS v0.2\n$ "
        }

        findViewById<Button>(R.id.helpButton).setOnClickListener {
            appendLine("사용 예시: pwd, ls, uname -a, whoami")
            appendLine("현재는 MVP 단계로 1회 명령 실행 방식입니다.")
        }
    }

    private fun runCommand(command: String) {
        ioExecutor.execute {
            try {
                val process = ProcessBuilder("sh", "-c", command)
                    .redirectErrorStream(true)
                    .start()

                val out = StringBuilder()
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        out.appendLine(line)
                    }
                }

                val code = process.waitFor()
                runOnUiThread {
                    val text = out.toString().ifBlank { "(출력 없음)" }
                    appendLine(text.trimEnd())
                    appendLine("[exit=$code]")
                    appendLine("$ ")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    appendLine("오류: ${e.message}")
                    appendLine("$ ")
                }
            }
        }
    }

    private fun appendLine(text: String) {
        outputText.append("\n$text")
        outputScroll.post { outputScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        ioExecutor.shutdownNow()
        super.onDestroy()
    }
}
