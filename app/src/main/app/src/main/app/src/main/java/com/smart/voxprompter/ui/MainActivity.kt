            private fun startStudioRecording() {
                try {
                    // التعديل الجوهري: استخدام VOICE_COMMUNICATION بدلاً من VOICE_RECOGNITION
                    // هذا المصدر يجبر نظام أندرويد على تفعيل فلاتر عزل الضوضاء المدمجة (AEC/NS) بشكل افتراضي
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                        sampleRate, channelConfig, audioFormat, bufferSize
                    )

                    // تفعيل عزل الضوضاء بشكل صريح إذا كانت المدخلات لا تزال تحتوي على ضجيج
                    if (NoiseSuppressor.isAvailable()) {
                        noiseSuppressor = NoiseSuppressor.create(audioRecord!!.audioSessionId)
                        noiseSuppressor?.enabled = true
                    }
                    
                    // تفعيل التحكم التلقائي في الكسب لمنع تضخم أصوات الخلفية
                    if (AutomaticGainControl.isAvailable()) {
                        gainControl = AutomaticGainControl.create(audioRecord!!.audioSessionId)
                        gainControl?.enabled = true
                    }

                    audioRecord?.startRecording()
                    isRecording = true
                    actionButton?.text = "إيقاف ومشاركة الصوت 📤"
                    actionButton?.setBackgroundColor(Color.parseColor("#C62828"))

                    textScrollView?.scrollTo(0, 0)
                    startSmartScrolling()

                    recordingThread = Thread({ writeAudioDataToFile() }, "AudioRecord Thread")
                    recordingThread?.start()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "فشل بدء التسجيل: تأكد من منح صلاحيات الميكروفون", Toast.LENGTH_SHORT).show()
                }
            }
            
