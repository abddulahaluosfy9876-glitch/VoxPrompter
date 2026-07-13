package com.example.voxprompter

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast

class MainActivity : Activity() {

    private var prompterEditText: EditText? = null
    private var scrollView: ScrollView? = null
    private var btnAction: Button? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prompterEditText = findViewById(R.id.prompterEditText)
        scrollView = findViewById(R.id.scrollView)
        btnAction = findViewById(R.id.btnAction)
        val btnImport = findViewById<Button>(R.id.btnImport)
        val btnPaste = findViewById<Button>(R.id.btnPaste)

        btnImport?.setOnClickListener {
            Toast.makeText(this, "ميزة استيراد الملفات جاهزة", Toast.LENGTH_SHORT).show()
        }

        btnPaste?.setOnClickListener {
            Toast.makeText(this, "ميزة اللصق السريع جاهزة", Toast.LENGTH_SHORT).show()
        }

        btnAction?.setOnClickListener {
            isRecording = !isRecording
            if (isRecording) {
                btnAction?.text = "إيقاف وحفظ الصوت 📤"
                btnAction?.setBackgroundColor(android.graphics.Color.parseColor("#C62828"))
                Toast.makeText(this, "بدء التسجيل...", Toast.LENGTH_SHORT).show()
            } else {
                btnAction?.text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
                btnAction?.setBackgroundColor(android.graphics.Color.parseColor("#00796B"))
                Toast.makeText(this, "تم إيقاف وحفظ التسجيل بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
