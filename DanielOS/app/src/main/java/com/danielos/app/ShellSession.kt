package com.danielos.app

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.File
import java.util.concurrent.ExecutorService

interface ShellSession {
    fun start(onLine: (String) -> Unit, onExit: (Int) -> Unit, onError: (String) -> Unit)
    fun send(command: String): Result<Unit>
    fun interrupt(): Result<Unit>
    fun stop()
}

class LocalShellSession(
    private val executor: ExecutorService,
    private val shellCommand: String = "sh",
    private val defaultHome: String
) : ShellSession {
    @Volatile
    private var process: Process? = null
    private var writer: BufferedWriter? = null

    override fun start(onLine: (String) -> Unit, onExit: (Int) -> Unit, onError: (String) -> Unit) {
        executor.execute {
            try {
                val pb = ProcessBuilder(shellCommand)
                    .redirectErrorStream(true)

                // Force a safe default working dir + HOME for Android app sandbox shells.
                val homeDir = File(defaultHome)
                if (homeDir.isDirectory) {
                    pb.directory(homeDir)
                }
                val env = pb.environment()
                env["HOME"] = if (homeDir.isDirectory) defaultHome else (env["HOME"] ?: "/")
                env["PWD"] = env["HOME"] ?: "/"
                env["TERM"] = env["TERM"] ?: "xterm-256color"

                val proc = pb.start()

                process = proc
                writer = BufferedWriter(OutputStreamWriter(proc.outputStream))
                onLine("[session] shell started")

                // Print current dir so users can verify startup location.
                writer?.apply {
                    write("pwd\n")
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
