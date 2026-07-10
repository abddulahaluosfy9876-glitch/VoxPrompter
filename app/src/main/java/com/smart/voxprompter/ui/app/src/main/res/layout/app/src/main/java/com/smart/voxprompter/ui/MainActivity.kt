package com.smart.voxprompter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scriptScroll: ScrollView
    private lateinit var tvPrompterText: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button
    private lateinit var btnTextIncrease: Button
    private lateinit var btnTextDecrease: Button

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var scrollSpeed = 2 // السرعة الافتراضية للحركة
    private var fontSize = 28f // الحجم الافتراضي للخط بالـ SP

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                scriptScroll.smoothScrollBy(0, scrollSpeed)
                handler.postDelayed(this, 30)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط عناصر الواجهة
        scriptScroll = findViewById(R.id.scriptScroll)
        tvPrompterText = findViewById(R.id.tvPrompterText)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnSpeedUp = findViewById(R.id.btnSpeedUp)
        btnSpeedDown = findViewById(R.id.btnSpeedDown)
        btnTextIncrease = findViewById(R.id.btnTextIncrease)
        btnTextDecrease = findViewById(R.id.btnTextDecrease)

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

        // زيادة سرعة الحركة
        btnSpeedUp.setOnClickListener {
            if (scrollSpeed < 10) scrollSpeed += 1
        }

        // تقليل سرعة الحركة
        btnSpeedDown.setOnClickListener {
            if (scrollSpeed > 1) scrollSpeed -= 1
        }

        // تكبير حجم الخط
        btnTextIncrease.setOnClickListener {
            if (fontSize < 60f) { // حد أقصى للحجم من أجل التناسق
                fontSize += 4f
                tvPrompterText.textSize = fontSize
            }
        }

        // تصغير حجم الخط
        btnTextDecrease.setOnClickListener {
            if (fontSize > 18f) { // حد أدنى لضمان وضوح القراءة
                fontSize -= 4f
                tvPrompterText.textSize = fontSize
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(scrollRunnable)
    }
}
