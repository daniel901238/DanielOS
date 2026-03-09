package com.danielos.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "DanielOS v0.1\nTerminal engine scaffold ready."
            textSize = 18f
            setPadding(40, 120, 40, 40)
        }
        setContentView(tv)
    }
}
