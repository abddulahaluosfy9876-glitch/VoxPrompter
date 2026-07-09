package com.smart.voxprompter.engine

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingEngine(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    var currentOutputFile: File? = null
        private set

    /**
     * بدء تسجيل الصوت بجودة عالية وتوليد اسم الملف تلقائياً
     */
    fun startRecording() {
        // توليد اسم الملف بناءً على التاريخ والوقت الحالي مثل: Recording_2026-07-09_23-30.m4a
        val timeStamp = SimpleDateFormat("yyyy-MM-DD_HH-mm", Locale.getDefault()).format(Date())
        val fileName = "Recording_$timeStamp.m4a"
        
        // الحصول على مجلد الموسيقى الآمن للتطبيق (Scoped Storage المتوافق مع Android 11+)
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        currentOutputFile = File(musicDir, fileName)

        // تهيئة الـ MediaRecorder حسب إصدار الأندرويد
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000) // جودة صوت واضحة وممتازة لقراءة النصوص
            setAudioSamplingRate(44100)     // التردد القياسي للصوت النقي
            setOutputFile(currentOutputFile!!.absolutePath)
            prepare()
            start()
        }
    }

    /**
     * إيقاف التسجيل بأمان وإعادة الملف المحفوظ
     */
    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentOutputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

