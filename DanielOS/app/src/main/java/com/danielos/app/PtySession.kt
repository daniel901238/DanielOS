package com.danielos.app

import java.util.concurrent.ExecutorService

/**
 * v0.9: PTY 대응 준비 레이어(스켈레톤)
 *
 * 현재는 LocalShellSession으로 폴백하고,
 * 차후 JNI/PTMX 연결 시 이 클래스를 실제 PTY 구현으로 교체한다.
 */
class PtyShellSession(
    private val executor: ExecutorService
) : ShellSession {

    private val fallback = LocalShellSession(executor)

    override fun start(onLine: (String) -> Unit, onExit: (Int) -> Unit, onError: (String) -> Unit) {
        if (!NativePtyBridge.isSupported()) {
            onLine("[pty] not available, fallback to local shell")
            fallback.start(onLine, onExit, onError)
            return
        }

        // TODO(v1.0): JNI 기반 PTY start 구현
        onLine("[pty] bridge detected but not wired yet, fallback to local shell")
        fallback.start(onLine, onExit, onError)
    }

    override fun send(command: String): Result<Unit> {
        // TODO(v1.0): JNI write 구현
        return fallback.send(command)
    }

    override fun interrupt(): Result<Unit> {
        // TODO(v1.0): SIGINT (Ctrl+C) 전달 구현
        return fallback.interrupt()
    }

    override fun stop() {
        // TODO(v1.0): PTY close 구현
        fallback.stop()
    }
}

object NativePtyBridge {
    fun isSupported(): Boolean {
        // TODO(v1.0): System.loadLibrary("danielos_pty") + health check
        return false
    }
}
