package com.smart.voxprompter

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var etUserScript: EditText
    private lateinit var tvAudioStatus: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button
    private lateinit var btnTextIncrease: Button
    private lateinit var btnTextDecrease: Button
    private lateinit var btnReset: Button
    private lateinit var btnRecordStart: Button
    private lateinit var btnRecordStop: Button

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var scrollSpeed = 2 
    private var fontSize = 24f 

    // متغيرات مسجل الصوت
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

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

        // ربط الواجهة
        etUserScript = findViewById(R.id.etUserScript)
        tvAudioStatus = findViewById(R.id.tvAudioStatus)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnSpeedUp = findViewById(R.id.btnSpeedUp)
        btnSpeedDown = findViewById(R.id.btnSpeedDown)
        btnTextIncrease = findViewById(R.id.btnTextIncrease)
        btnTextDecrease = findViewById(R.id.btnTextDecrease)
        btnReset = findViewById(R.id.btnReset)
        btnRecordStart = findViewById(R.id.btnRecordStart)
        btnRecordStop = findViewById(R.id.btnRecordStop)

        // أزرار التحكم بالحركة والنص
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

        btnReset.setOnClickListener {
            isPlaying = false
            btnPlayPause.text = "تشغيل"
            etUserScript.isCursorVisible = true
            handler.removeCallbacks(scrollRunnable)
            etUserScript.scrollTo(0, 0)
        }

        btnSpeedUp.setOnClickListener { if (scrollSpeed < 10) scrollSpeed += 1 }
        btnSpeedDown.setOnClickListener { if (scrollSpeed > 1) scrollSpeed -= 1 }
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

        // برمجة أزرار التسجيل الصوتي
        btnRecordStart.setOnClickListener {
            if (checkAudioPermission()) {
                startRecording()
            } else {
                requestAudioPermission()
            }
        }

        btnRecordStop.setOnClickListener {
            stopRecording()
        }
    }

    // بدء التسجيل وإعداد الترميز عالي الجودة
    private fun startRecording() {
        audioFile = File(externalCacheDir, "VoxAudio_${System.currentTimeMillis()}.m4a")
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000) // جودة بث عالية وخفيفة
            setAudioSamplingRate(44100) // استوديو قياسي
            setOutputFile(audioFile?.absolutePath)

            try {
                prepare()
                start()
                tvAudioStatus.text = "جاري التسجيل الصوتي الآن... 🔴"
                btnRecordStart.isEnabled = false
                btnRecordStop.isEnabled = true
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "فشل بدء التسجيل", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // إيقاف التسجيل وحفظ الملف
    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                // للتعامل مع الإيقاف السريع جداً
            }
        }
        mediaRecorder = null
        tvAudioStatus.text = "تم حفظ المقطع الصوتي بنجاح! 💾"
        btnRecordStart.isEnabled = true
        btnRecordStop.isEnabled = false
        
        Toast.makeText(this, "تم الحفظ في الكاش الخاص بالتطبيق", Toast.LENGTH_LONG).show()
    }

    // فحص صلاحية المايكروفون
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(this, "يجب الموافقة على صلاحية المايكروفون للتسجيل", Toast.LENGTH_SHORT).show()
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
        mediaRecorder?.release()
    }
}
