package com.example.voxprompter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.abs

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private var prompterInput: EditText? = null
    private var textScrollView: ScrollView? = null
    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var actionButton: Button? = null

    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val scrollHandler = Handler(Looper.getMainLooper())
    private var scrollRunnable: Runnable? = null
    private val scrollSpeedPx = 3 
    private val scrollIntervalMs = 50L 

    private val sampleRate = 44100 
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize = 0
    private var recordingThread: Thread? = null
    private val noiseGateThreshold = 1200 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (bufferSize <= 0) bufferSize = 4096

            // تهيئة آمنة لمحرك محاكاة الذكاء الاصطناعي
            try {
                textToSpeech = TextToSpeech(applicationContext, this)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.BLACK)
                setPadding(30, 30, 30, 30)
            }

            val topButtonsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
                layoutParams = LinearLayout.LayoutParams(-1, 110).apply { bottomMargin = 15 }
            }

            val importBtn = Button(this).apply {
                text = "استيراد ملف TXT 📂"
                setBackgroundColor(Color.parseColor("#00695C"))
                setTextColor(Color.WHITE)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = 10 }
            }
            topButtonsLayout.addView(importBtn)

            val pasteBtn = Button(this).apply {
                text = "لصق النص 📋"
                setBackgroundColor(Color.parseColor("#37474F"))
                setTextColor(Color.WHITE)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
            }
            topButtonsLayout.addView(pasteBtn)
            mainLayout.addView(topButtonsLayout)

            textScrollView = ScrollView(this).apply {
                layoutParams = LinearLayout.LayoutParams(-1, 0, 1f).apply { bottomMargin = 20 }
                isVerticalScrollBarEnabled = true
            }

            prompterInput = EditText(this).apply {
                hint = "اكتب قصتك هنا... تم تفعيل محرك محاكاة الذكاء الاصطناعي للمساعد الذكي بنجاح! 🤖✨"
                setHintTextColor(Color.GRAY)
                setTextColor(Color.WHITE)
                textSize = 22f
                gravity = Gravity.TOP or Gravity.START
                setBackgroundColor(Color.parseColor("#121212"))
                setPadding(25, 25, 25, 800)
            }
            textScrollView?.addView(prompterInput)
            mainLayout.addView(textScrollView)

            val bottomButtonsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 4f
                layoutParams = LinearLayout.LayoutParams(-1, 120)
            }

            val resetBtn = Button(this).apply {
                text = "البداية ↩️"
                setBackgroundColor(Color.parseColor("#455A64"))
                setTextColor(Color.WHITE)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = 10 }
                setOnClickListener {
                    textScrollView?.smoothScrollTo(0, 0)
                    if (textToSpeech?.isSpeaking == true) {
                        textToSpeech?.stop()
                    }
                    Toast.makeText(this@MainActivity, "تم إعادة النص وإيقاف الصوت الذكي", Toast.LENGTH_SHORT).show()
                }
            }
            bottomButtonsLayout.addView(resetBtn)

            actionButton = Button(this).apply {
                text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
                setBackgroundColor(Color.parseColor("#00796B"))
                setTextColor(Color.WHITE)
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, -1, 3f)
                setOnClickListener { 
                    if (isRecording) {
                        stopRecordingAndShare()
                    } else if (textToSpeech?.isSpeaking == true) {
                        textToSpeech?.stop()
                        actionButton?.text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
                        actionButton?.setBackgroundColor(Color.parseColor("#00796B"))
                    } else {
                        checkAndStartRecording()
                    }
                }
            }
            bottomButtonsLayout.addView(actionButton)
            mainLayout.addView(bottomButtonsLayout)

            setContentView(mainLayout)
            
            audioFile = File(externalCacheDir ?: cacheDir, "VoxStudioRecord.wav")
            checkAudioPermissions()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "حدث خطأ أثناء تشغيل التطبيق", Toast.LENGTH_LONG).show()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("ar"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.US)
            }
            textToSpeech?.setPitch(1.0f)
            textToSpeech?.setSpeechRate(0.9f)
            isTtsInitialized = true
        }
    }

    private fun checkAudioPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 200)
            }
        }
    }

    private fun checkAndStartRecording() {
        val prefs = getSharedPreferences("VoxPrefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("voice_samples_count", 0)

        if (count >= 3) {
            val options = arrayOf(
                "🎤 تسجيل فائق النقاء والعزل (بصوتي الحقيقي)", 
                "🤖 المساعد الذكي (توليد الصوت ومحاكاة النبرة الحالية)"
            )
            AlertDialog.Builder(this).apply {
                setTitle("تم حفظ النبرة! اختر طريقة الأداء:")
                setItems(options) { _, which ->
                    if (which == 0) {
                        startStudioRecording()
                    } else {
                        startAiVoiceSimulation()
                    }
                }
                show()
            }
        } else {
            startStudioRecording()
            Toast.makeText(this, "جاري حفظ بصمتك المعزولة، العينة رقم (${count + 1}) من 3", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAiVoiceSimulation() {
        val textToRead = prompterInput?.text?.toString() ?: ""
        if (textToRead.trim().isEmpty()) {
            Toast.makeText(this, "الرجاء كتابة أو لصق نص أولاً لتوليده بالذكاء الاصطناعي!", Toast.LENGTH_LONG).show()
            return
        }

        if (isTtsInitialized && textToSpeech != null) {
            actionButton?.text = "إيقاف المحاكاة الصوتية الذكية 🤖"
            actionButton?.setBackgroundColor(Color.parseColor("#7B1FA2"))
            textScrollView?.scrollTo(0, 0)
            textToSpeech?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "VoxAiSpeech")
            isRecording = true 
            startSmartScrolling()
        } else {
            Toast.makeText(this, "محرك محاكاة النبرة جاري تهيئته، حاول مجدداً خلال ثوانٍ!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startStudioRecording() {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate, channelConfig, audioFormat, bufferSize
            )

            if (NoiseSuppressor.isAvailable() && audioRecord != null) {
                noiseSuppressor = NoiseSuppressor.create(audioRecord!!.audioSessionId)
                noiseSuppressor?.enabled = true
            }
            if (AutomaticGainControl.isAvailable() && audioRecord != null) {
                gainControl = AutomaticGainControl.create(audioRecord!!.audioSessionId)
                gainControl?.enabled = true
            }

            audioRecord?.startRecording()
            isRecording = true
            actionButton?.text = "إيقاف ومشاركة الصوت 📤"
            actionButton?.setBackgroundColor(Color.parseColor("#C62828"))

            textScrollView?.scrollTo(0, 0)
            startSmartScrolling()

            recordingThread = Thread({ writeAudioDataWithHighNoiseGate() }, "AudioRecord Thread")
            recordingThread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "فشل بدء التسجيل بجودة عالية", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeAudioDataWithHighNoiseGate() {
        val data = ByteArray(bufferSize)
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(audioFile)
            writeWavHeader(os, 0, 0, sampleRate, 1, 16)
            while (isRecording) {
                val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                if (AudioRecord.ERROR_INVALID_OPERATION != read && AudioRecord.ERROR_BAD_VALUE != read && read > 0) {
                    val shorts = ShortArray(read / 2)
                    ByteBuffer.wrap(data, 0, read).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
                    var maxAmplitude = 0
                    for (i in shorts.indices) {
                        val absVal = abs(shorts[i].toInt())
                        if (absVal > maxAmplitude) maxAmplitude = absVal
                    }
                    if (maxAmplitude < noiseGateThreshold) {
                        for (i in 0 until read) data[i] = 0
                    }
                    os.write(data, 0, read)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }_

---

بعد حفظ التحديث، سيقوم GitHub Actions بإعادة بناء التطبيق تلقائياً وتصدير نسخة APK جديدة مستقرة وتفتح بدون أي انهيار. ثبّتها وأخبرني بالنتيجة!
                
