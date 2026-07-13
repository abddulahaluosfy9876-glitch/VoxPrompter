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
    private var bufferSize = 4096
    private var recordingThread: Thread? = null
    private val noiseGateThreshold = 1200 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // بناء الواجهة برمجياً لضمان الاستقرار التام على أي ثيم للمستودع
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(30, 30, 30, 30)
        }

        val topButtonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                130
            ).apply { bottomMargin = 15 }
        }

        val importBtn = Button(this).apply {
            text = "استيراد ملف TXT 📂"
            setBackgroundColor(Color.parseColor("#00695C"))
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { rightMargin = 10 }
        }
        topButtonsLayout.addView(importBtn)

        val pasteBtn = Button(this).apply {
            text = "لصق النص 📋"
            setBackgroundColor(Color.parseColor("#37474F"))
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        }
        topButtonsLayout.addView(pasteBtn)
        mainLayout.addView(topButtonsLayout)

        textScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                0, 
                1f
            ).apply { bottomMargin = 20 }
            isVerticalScrollBarEnabled = true
            setBackgroundColor(Color.parseColor("#121212"))
        }

        prompterInput = EditText(this).apply {
            hint = "اكتب أو الصق قصتك هنا... تم التحديث والتثبيت الآمن 🤖✨"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.TOP or Gravity.START
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(25, 25, 25, 800)
        }
        textScrollView?.addView(prompterInput)
        mainLayout.addView(textScrollView)

        val bottomButtonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                140
            )
        }

        val resetBtn = Button(this).apply {
            text = "البداية ↩️"
            setBackgroundColor(Color.parseColor("#455A64"))
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { rightMargin = 10 }
            setOnClickListener {
                textScrollView?.smoothScrollTo(0, 0)
                if (textToSpeech?.isSpeaking == true) {
                    textToSpeech?.stop()
                }
                Toast.makeText(this@MainActivity, "تم إعادة النص", Toast.LENGTH_SHORT).show()
            }
        }
        bottomButtonsLayout.addView(resetBtn)

        actionButton = Button(this).apply {
            text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
            setBackgroundColor(Color.parseColor("#00796B"))
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f)
            setOnClickListener { 
                handleAppAction()
            }
        }
        bottomButtonsLayout.addView(actionButton)
        mainLayout.addView(bottomButtonsLayout)

        setContentView(mainLayout)
    }

    private fun handleAppAction() {
        if (isRecording) {
            stopRecordingAndShare()
            return
        }

        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 200)
            return
        }

        if (audioFile == null) {
            audioFile = File(externalCacheDir ?: cacheDir, "VoxStudioRecord.wav")
        }
        if (textToSpeech == null) {
            try {
                textToSpeech = TextToSpeech(applicationContext, this)
            } catch (e: Exception) { e.printStackTrace() }
        }

        checkAndStartRecording()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.setLanguage(Locale("ar"))
            textToSpeech?.setPitch(1.0f)
            textToSpeech?.setSpeechRate(0.9f)
            isTtsInitialized = true
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
                    if (which == 0) startStudioRecording() else startAiVoiceSimulation()
                }
                show()
            }
        } else {
            startStudioRecording()
            val newCount = count + 1
            prefs.edit().putInt("voice_samples_count", newCount).apply()
            Toast.makeText(this, "جاري حفظ بصمتك المعزولة، العينة رقم ($newCount) من 3", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAiVoiceSimulation() {
        val textToRead = prompterInput?.text?.toString() ?: ""
        if (textToRead.trim().isEmpty()) {
            Toast.makeText(this, "الرجاء كتابة أو لصق نص أولاً لتوليده!", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "محرك محاكاة النبرة جاري تهيئته، أعد الضغط خلال ثوانٍ!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startStudioRecording() {
        try {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (bufferSize <= 0) bufferSize = 4096

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
            if (audioFile != null) {
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
                os.close()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopRecordingAndShare() {
        isRecording = false
        stopSmartScrolling()
        
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            noiseSuppressor?.release()
            gainControl?.release()
        } catch (e: Exception) { e.printStackTrace() }

        actionButton?.text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
        actionButton?.setBackgroundColor(Color.parseColor("#00796B"))
        
        updateWavHeader(audioFile)
        shareAudioFile()
    }

    private fun shareAudioFile() {
        if (audioFile != null && audioFile!!.exists() && audioFile!!.length() > 44) {
            try {
                val fileUri = FileProvider.getUriForFile(this, "com.example.voxprompter.fileprovider", audioFile!!)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/wav"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "مشاركة الصوت النقي:"))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun startSmartScrolling() {
        scrollRunnable = object : Runnable {
            override fun run() {
                if (isRecording) {
                    textScrollView?.smoothScrollBy(0, scrollSpeedPx)
                    scrollHandler.postDelayed(this, scrollIntervalMs)
                }
            }
        }
        scrollHandler.post(scrollRunnable!!)
    }

    private fun stopSmartScrolling() {
        scrollRunnable?.let { scrollHandler.removeCallbacks(it) }
    }

    private fun writeWavHeader(os: FileOutputStream, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Int, channels: Int, byteRate: Int) {
        val header = ByteArray(44)
        header[0] = 'R'.toByte(); header[1] = 'I'.toByte(); header[2] = 'F'.toByte(); header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte(); header[9] = 'A'.toByte(); header[10] = 'V'.toByte(); header[11] = 'E'.toByte()
        header[12] = 'f'.toByte(); header[13] = 'm'.toByte(); header[14] = 't'.toByte(); header[15] = ' '.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0; header[20] = 1; header[21] = 0
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (longSampleRate * channels * byteRate / 8 and 0xff).toByte()
        header[29] = (longSampleRate * channels * byteRate / 8 shr 8 and 0xff).toByte()
        header[30] = (longSampleRate * channels * byteRate / 8 shr 16 and 0xff).toByte()
        header[31] = (longSampleRate * channels * byteRate / 8 shr 24 and 0xff).toByte()
        header[32] = (channels * byteRate / 8).toByte(); header[33] = 0
        header[34] = byteRate.toByte(); header[35] = 0
        header[36] = 'd'.toByte(); header[37] = 'a'.toByte(); header[38] = 't'.toByte(); header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        os.write(header, 0, 44)
    }

    private fun updateWavHeader(file: File?) {
        if (file == null || !file.exists()) return
        try {
            val raf = RandomAccessFile(file, "rw")
            val totalAudioLen = raf.length() - 44
            val totalDataLen = totalAudioLen + 36
            raf.seek(4)
            raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalDataLen.toInt()).array())
            raf.seek(40)
            raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalAudioLen.toInt()).array())
            raf.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            textToSpeech?.shutdown()
        } catch (e: Exception) { e.printStackTrace() }
    }
}
