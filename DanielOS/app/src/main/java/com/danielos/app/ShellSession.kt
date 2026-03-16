package com.danielos.app

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.ExecutorService

interface ShellSession {
    fun start(onLine: (String) -> Unit, onExit: (Int) -> Unit, onError: (String) -> Unit)
    fun send(command: String): Result<Unit>
    fun interrupt(): Result<Unit>
    fun stop()
}

class LocalShellSession(
    private val executor: ExecutorService,
    private val shellCommand: String = "sh"
) : ShellSession {
    @Volatile
    private var process: Process? = null
    private var writer: BufferedWriter? = null

    override fun start(onLine: (String) -> Unit, onExit: (Int) -> Unit, onError: (String) -> Unit) {
        executor.execute {
            try {
                val proc = ProcessBuilder(shellCommand)
                    .redirectErrorStream(true)
                    .start()

                process = proc
                writer = BufferedWriter(OutputStreamWriter(proc.outputStream))
                onLine("[session] shell started")

                // Try Termux-style home first, then shell HOME fallback.
                writer?.apply {
                    write("if [ -d /data/data/com.termux/files/home ]; then cd /data/data/com.termux/files/home; elif [ -d \$HOME ]; then cd \$HOME; fi; pwd\n")
                    flush()
                }

                BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val safe = line ?: continue
                        onLine(TerminalOutputSanitizer.clean(safe))
                    }
                }

                onExit(proc.waitFor())
            } catch (e: Exception) {
                onError(e.message ?: "unknown error")
            }
        }
    }

    override fun send(command: String): Result<Unit> {
        return runCatching {
            val w = writer ?: error("shell not ready")
            w.write(command)
            w.newLine()
            w.flush()
        }
    }

    override fun interrupt(): Result<Unit> {
        return runCatching {
            process?.destroy()
            writer = null
            process = null
        }
    }

    override fun stop() {
        try {
            writer?.apply {
                write("exit\n")
                flush()
                close()
            }
        } catch (_: Exception) {
        }

        try {
            process?.destroy()
        } catch (_: Exception) {
        }

        writer = null
        process = null
    }
}

object TerminalOutputSanitizer {
    private val ansiRegex = Regex("\\u001B\\[[;\\d]*[ -/]*[@-~]")

    fun clean(raw: String): String {
        return raw
            .replace("\u0000", "")
            .replace("\r", "")
            .replace(ansiRegex, "")
    }
}
