package com.smart.voxprompter.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import com.smart.voxprompter.R

class MainActivity : Activity() {

    private var prompterEditText: EditText? = null
    private var scrollView: ScrollView? = null
    private var btnAction: Button? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط العناصر بشكل آمن وصحيح باستخدام R التابع للمشروع
        prompterEditText = findViewById(R.id.prompterEditText)
        scrollView = findViewById(R.id.scrollView)
        btnAction = findViewById(R.id.btnAction)
        
        val btnImport = findViewById<Button>(R.id.btnImport)
        val btnPaste = findViewById<Button>(R.id.btnPaste)

        btnImport?.setOnClickListener {
            Toast.makeText(this, "ميزة استيراد الملفات جاهزة", Toast.LENGTH_SHORT).show()
        }

        btnPaste?.setOnClickListener {
            Toast.makeText(this, "ميزة لصق النصوص جاهزة", Toast.LENGTH_SHORT).show()
        }

        btnAction?.setOnClickListener {
            if (isRecording) {
                isRecording = false
                btnAction?.text = "ابدأ"
                Toast.makeText(this, "تم إيقاف التسجيل", Toast.LENGTH_SHORT).show()
            } else {
                isRecording = true
                btnAction?.text = "إيقاف"
                Toast.makeText(this, "بدء التسجيل...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
