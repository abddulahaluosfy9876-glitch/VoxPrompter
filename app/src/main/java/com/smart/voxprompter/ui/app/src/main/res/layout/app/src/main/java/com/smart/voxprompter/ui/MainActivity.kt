package com.smart.voxprompter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etUserScript: EditText
    private lateinit var btnPlayPause: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button
    private lateinit var btnTextIncrease: Button
    private lateinit var btnTextDecrease: Button
    private lateinit var btnReset: Button

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var scrollSpeed = 2 
    private var fontSize = 24f 

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                val scrollY = etUserScript.scrollY
                etUserScript.scrollTo(0, scrollY + scrollSpeed)
                handler.postDelayed(this, 30)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUserScript = findViewById(R.id.etUserScript)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnSpeedUp = findViewById(R.id.btnSpeedUp)
        btnSpeedDown = findViewById(R.id.btnSpeedDown)
        btnTextIncrease = findViewById(R.id.btnTextIncrease)
        btnTextDecrease = findViewById(R.id.btnTextDecrease)
        btnReset = findViewById(R.id.btnReset)

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                btnPlayPause.text = "تشغيل"
                etUserScript.isCursorVisible = true 
                handler.removeCallbacks(scrollRunnable)
            } else {
                isPlaying = true
                btnPlayPause.text = "إيقاف"
                etUserScript.isCursorVisible = false 
                hideKeyboard() 
                handler.post(scrollRunnable)
            }
        }

        // تفعيل زر الإعادة للبداية
        btnReset.setOnClickListener {
            isPlaying = false
            btnPlayPause.text = "تشغيل"
            etUserScript.isCursorVisible = true
            handler.removeCallbacks(scrollRunnable)
            etUserScript.scrollTo(0, 0) // إرجاع النص للأعلى تماماً
        }

        btnSpeedUp.setOnClickListener {
            if (scrollSpeed < 10) scrollSpeed += 1
        }

        btnSpeedDown.setOnClickListener {
            if (scrollSpeed > 1) scrollSpeed -= 1
        }

        btnTextIncrease.setOnClickListener {
            if (fontSize < 60f) {
                fontSize += 4f
                etUserScript.textSize = fontSize
            }
        }

        btnTextDecrease.setOnClickListener {
            if (fontSize > 18f) {
                fontSize -= 4f
                etUserScript.textSize = fontSize
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(scrollRunnable)
    }
}
