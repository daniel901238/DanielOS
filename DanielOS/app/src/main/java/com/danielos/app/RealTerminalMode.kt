package com.danielos.app

/**
 * v1.3 real-terminal mode groundwork
 *
 * 목표:
 * - 출력/입력을 단일 terminal canvas에서 처리(SSH/Termux 유사 UX)
 * - PTY가 준비되면 해당 캔버스에 직접 바인딩
 */

enum class TerminalInputMode {
    SPLIT_INPUT,   // 현재 구조: 출력(TextView) + 입력(EditText)
    DIRECT_CANVAS  // 목표 구조: 단일 터미널 캔버스 직접 입력
}

object TerminalFeatureFlags {
    // 단계적 적용을 위해 플래그 기반으로 유지
    const val ENABLE_DIRECT_CANVAS_MODE = false
}

/**
 * Direct canvas 입력 모드에서 사용할 최소 계약.
 */
interface DirectTerminalController {
    fun appendOutput(text: String)
    fun submitCommand(command: String)
    fun setPrompt(prompt: String)
}
