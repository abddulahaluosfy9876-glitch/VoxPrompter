package com.smart.voxprompter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var scrollSpeed = 2 // السرعة الافتراضية للحركة
    private var fontSize = 24f // الحجم الافتراضي للخط بالـ SP

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                // تحريك النص داخل الـ EditText تلقائياً بناءً على قيمة scrollY الحالي
                val scrollY = etUserScript.scrollY
                etUserScript.scrollTo(0, scrollY + scrollSpeed)
                handler.postDelayed(this, 30) // تكرار الحركة كل 30 جزء من الثانية
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط عناصر الواجهة الجديدة
        etUserScript = findViewById(R.id.etUserScript)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnSpeedUp = findViewById(R.id.btnSpeedUp)
        btnSpeedDown = findViewById(R.id.btnSpeedDown)
        btnTextIncrease = findViewById(R.id.btnTextIncrease)
        btnTextDecrease = findViewById(R.id.btnTextDecrease)

        // برمجة زر التشغيل والإيقاف المؤقت للتحريك
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                btnPlayPause.text = "تشغيل"
                handler.removeCallbacks(scrollRunnable)
            } else {
                isPlaying = true
                btnPlayPause.text = "إيقاف"
                handler.post(scrollRunnable)
            }
        }

        // زيادة سرعة الحركة
        btnSpeedUp.setOnClickListener {
            if (scrollSpeed < 10) scrollSpeed += 1
        }

        // تقليل سرعة الحركة
        btnSpeedDown.setOnClickListener {
            if (scrollSpeed > 1) scrollSpeed -= 1
        }

        // تكبير حجم الخط داخل مربع النص
        btnTextIncrease.setOnClickListener {
            if (fontSize < 60f) {
                fontSize += 4f
                etUserScript.textSize = fontSize
            }
        }

        // تصغير حجم الخط داخل مربع النص
        btnTextDecrease.setOnClickListener {
            if (fontSize > 18f) {
                fontSize -= 4f
                etUserScript.textSize = fontSize
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(scrollRunnable)
    }
}
