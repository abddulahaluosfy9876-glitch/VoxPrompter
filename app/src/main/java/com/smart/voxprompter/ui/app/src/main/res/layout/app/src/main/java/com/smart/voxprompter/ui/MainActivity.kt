package com.smart.voxprompter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scriptScroll: ScrollView
    private lateinit var btnPlayPause: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var scrollSpeed = 2 // القيمة الافتراضية للسرعة

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                // تحريك الشاشة لأسفل تدريجياً بناءً على السرعة
                scriptScroll.smoothScrollBy(0, scrollSpeed)
                handler.postDelayed(this, 30) // تكرار الحركة كل 30 جزء من الثانية
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط العناصر البرمجية بالواجهة المرئية
        scriptScroll = findViewById(R.id.scriptScroll)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnSpeedUp = findViewById(R.id.btnSpeedUp)
        btnSpeedDown = findViewById(R.id.btnSpeedDown)

        // برمجة زر التشغيل والإيقاف المؤقت
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

        // زيادة السرعة
        btnSpeedUp.setOnClickListener {
            if (scrollSpeed < 10) scrollSpeed += 1
        }

        // تقليل السرعة
        btnSpeedDown.setOnClickListener {
            if (scrollSpeed > 1) scrollSpeed -= 1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(scrollRunnable) // إيقاف المحرك عند إغلاق التطبيق منعاً للمشاكل
    }
}
