package com.smart.voxprompter.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.smart.voxprompter.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TeleprompterViewModel by viewModels()
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermissions()
        setupSpeechEngine()
        observeAppProperties()

        binding.btnStart.setOnClickListener {
            val textToRead = binding.tvTeleprompterText.text.toString()
            viewModel.startTeleprompter(textToRead)
            startVoiceListening()
            triggerImmersiveMode(true)
        }

        binding.btnStop.setOnClickListener {
            viewModel.stopTeleprompter()
            speechRecognizer?.stopListening()
            triggerImmersiveMode(false)
        }
    }

    private fun setupSpeechEngine() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        // إعادة التشغيل التلقائي للاستماع عند حدوث توقف مؤقت لضمان استمرارية الملقن
                        startVoiceListening()
                    }
                    override fun onResults(results: Bundle?) {
                        startVoiceListening()
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            viewModel.onPartialSpeechReceived(matches[0])
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun startVoiceListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun observeAppProperties() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is TeleprompterViewModel.UiState.Reading -> {
                        binding.btnStart.visibility = View.GONE
                        binding.btnStop.visibility = View.VISIBLE
                    }
                    is TeleprompterViewModel.UiState.Finished -> {
                        binding.btnStart.visibility = View.VISIBLE
                        binding.btnStop.visibility = View.GONE
                        state.audioFile?.let { openShareSheet(it) }
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.timerState.collectLatest { binding.tvTimer.text = it }
        }

        lifecycleScope.launch {
            viewModel.percentageProgress.collectLatest { binding.readingProgress.progress = it }
        }

        lifecycleScope.launch {
            viewModel.scrollProgress.collectLatest { wordIndex ->
                val totalWords = binding.tvTeleprompterText.text.split(" ").size
                if (totalWords > 0) {
                    val ratio = wordIndex.toFloat() / totalWords
                    val targetScrollY = (binding.tvTeleprompterText.height * ratio).toInt()
                    // التمرير الناعم لتبديد القفزات والحفاظ على الكلمة الحالية في منتصف الشاشة
                    binding.textScrollView.smoothScrollTo(0, targetScrollY - (binding.textScrollView.height / 2))
                }
            }
        }
    }

    private fun openShareSheet(file: File) {
        val fileUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/m4a"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(sendIntent, "مشاركة ملف القراءة الصوتي عبر:"))
    }

    private fun triggerImmersiveMode(activate: Boolean) {
        window.decorView.systemUiVisibility = if (activate) {
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY 
                    or View.SYSTEM_UI_FLAG_FULLSCREEN 
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun checkAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}

