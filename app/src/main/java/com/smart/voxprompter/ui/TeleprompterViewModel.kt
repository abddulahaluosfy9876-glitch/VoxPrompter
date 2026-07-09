package com.smart.voxprompter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.smart.voxprompter.engine.RecordingEngine
import com.smart.voxprompter.engine.SmartMatchingEngine
import kotlinx.flow.MutableStateFlow
import kotlinx.flow.StateFlow
import kotlinx.flow.asStateFlow
import java.io.File
import java.util.Timer
import kotlin.concurrent.timerTask

class TeleprompterViewModel(application: Application) : AndroidViewModel(application) {

    private val recordingEngine = RecordingEngine(application)
    private var matchingEngine: SmartMatchingEngine? = null
    private var timer: Timer? = null
    private var elapsedSeconds = 0

    // إدارة حالة واجهة المستخدم
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // إدارة عداد وقت القراءة والتسجيل
    private val _timerState = MutableStateFlow("00:00")
    val timerState: StateFlow<String> = _timerState.asStateFlow()

    // إدارة مؤشر التمرير الحالي ومؤشر التقدم الإجمالي
    private val _scrollProgress = MutableStateFlow(0)
    val scrollProgress: StateFlow<Int> = _scrollProgress.asStateFlow()

    private val _percentageProgress = MutableStateFlow(0)
    val percentageProgress: StateFlow<Int> = _percentageProgress.asStateFlow()

    /**
     * بدء عملية التلقين والتسجيل الصوتي المتزامن
     */
    fun startTeleprompter(text: String) {
        matchingEngine = SmartMatchingEngine(text)
        recordingEngine.startRecording()
        _uiState.value = UiState.Reading
        startTimer()
    }

    /**
     * استقبال النصوص الجزئية من الميكروفون وتحديث مؤشرات التمرير والتقدم
     */
    fun onPartialSpeechReceived(partialText: String) {
        matchingEngine?.let { engine ->
            val bestIndex = engine.findBestMatchingIndex(partialText)
            _scrollProgress.value = bestIndex
            _percentageProgress.value = engine.getProgressPercentage()
        }
    }

    /**
     * إنهاء عملية التلقين وحفظ الملف الصوتي تلقائياً
     */
    fun stopTeleprompter() {
        stopTimer()
        val file = recordingEngine.stopRecording()
        _uiState.value = UiState.Finished(file)
    }

    private fun startTimer() {
        elapsedSeconds = 0
        _timerState.value = "00:00"
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask {
            elapsedSeconds++
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            _timerState.value = String.format("%02d:%02d", minutes, seconds)
        }, 1000, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    /**
     * الحالات البرمجية الخاصة بواجهة الملقن
     */
    sealed interface UiState {
        object Idle : UiState
        object Reading : UiState
        data class Finished(val audioFile: File?) : UiState
    }
}

